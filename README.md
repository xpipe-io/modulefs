[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.xpipe/modulefs/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.xpipe/modulefs)
[![javadoc](https://javadoc.io/badge2/io.xpipe/modulefs/javadoc.svg)](https://javadoc.io/doc/io.xpipe/modulefs)
[![Build Status](https://github.com/xpipe-io/modulefs/actions/workflows/publish.yml/badge.svg)](https://github.com/xpipe-io/modulefs/actions/workflows/publish.yml)

# ModuleFS library

The ModuleFS library provides a simple file system implementation to access the contents of Java modules in a unified way.
It also comes with a variety of neat features that will make working with modules more enjoyable for you.
You can get the library through [maven central](https://search.maven.org/artifact/io.xpipe/modulefs).
Note that at least Java 17 is required as it is the first LTS release that includes all necessary bug fixes for the internal module file systems.

## Installation

To use ModuleFS with Maven you have to add it as a dependency:

    <dependency>
      <groupId>io.xpipe</groupId>
      <artifactId>modulefs</artifactId>
      <version>0.1.6</version>
    </dependency>

For gradle, add the following entries to your build.gradle file:

    dependencies {
        implementation group: 'io.xpipe', name: 'modulefs', version: '0.1.6'
    }

Add the library to your project's module-info like this:

    requires io.xpipe.modulefs;

Note that ModuleFS requires your project to be modularized.

## Motivation

The [Path](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Path.html) and
[FileSystem](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/FileSystem.html) APIs
introduced in Java 7 with NIO.2 makes working with files much more pleasant.
One hurdle when trying to use these APIs with bundled application resources is that modules can be stored in different formats:
- Exploded module directory, stored as directory tree (Used during development)
- Module artifacts, mostly stored as jars (Used for both development and production)
- Modules contained in a jlink image, which are stored in a proprietary jimage format (Only used for production)

#### Implementation Differences

While there are FileSystem implementations like `jdk.zipfs` for jar files and the internal `jrtfs` for jlink images,
there is no unified interface, which results in you having to take care of FileSystem specific differences.
For example, the first challenge is to reliably find out the storage format of a module.
Then, you also have to adapt your code to handle unique properties of the storage format.
For example, every format requires you to use different schemes and approaches to closing the underlying file
system, which causes exceptions when not being done properly.
As a result, how to access files in modules using FileSystems heavily depends on how the modules are stored
and can therefore vary between development and production environments.
The current solution to reliably access resources in a package of a jar, or now a module, is to use the old methods
[getResource()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html#getResource(java.lang.String)) and
[getResourceAsStream()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html#getResourceAsStream(java.lang.String)).
These methods are however way more tedious to work with as it is lacking the many features of the Path API.

#### Modules vs Classpaths

The main difference between the traditional classpaths and
modules is that a module is located at exactly one location, e.g. a jar archive or a directory.
It is not possible to combine multiple different locations into
one module as you could do with for example multiple directories and classpaths.
This makes working with FileSystems on modules much easier as there is only one root.


## Features

ModuleFS allows you to create a FileSystem using the `module` URI scheme.
The created FileSystem instance conforms to standard FileSystem behaviours
with some limitations on writability as modules are intended to be read-only.
A simple file reading example could look like this:

````java
try (var fs = FileSystems.newFileSystem(
        URI.create("module:/com.myorg.mymodule"), Map.of())) {
    // The file system paths start from the root of the module,
    // so you have to include packages in your paths!
    var filePath = fs.getPath("com/myorg/mymodule/test_resource.txt");
    var fileContent = Files.readString(filePath);
}
````

The Path API allows for more complex applications than just parsing the contents of a single file.
For example, we can also easily recursively iterative over all files in a directory that exists inside your module:

````java
try (var fs = FileSystems.newFileSystem(
        URI.create("module:/com.myorg.mymodule"), Map.of())) {
    
    var filePath = fs.getPath("com/myorg/mymodule/assets");
    Files.walkFileTree(filePath, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // Do anything you want with the file
            return FileVisitResult.CONTINUE;
        }
    });
}
````

Basically, you can make use of any method in the
[Files](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html) class.


### Module Layers

In case you are using custom [ModuleLayers](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ModuleLayer.html),
you will have to pass this information to the file system to correctly locate all modules of this layer:

````java
ModuleLayer customLayer = ... ;
try (var fs = FileSystems.newFileSystem(
        URI.create("module:/com.myorg.mymodule"), Map.of("layer", customLayer))) {
    ...
}
````

### Using URLs

Many other Java methods take [URLs](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URL.html) as an input.
One problem with many libraries, even standard libraries, is that they hardcode/expect
URLs with a certain protocol, e.g. `file:` or `jar:`.
To adapt ModuleFS to this limitation, the ModuleFS library does not come with custom
[URLStreamHandler](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URLStreamHandler.html)
implementations required to make use of `module: ...` URLs.

The easiest way of obtaining a usable URL is to access the wrapped internal Path and access its URL:

````java
try (ModuleFileSystem fs = ModuleFileSystem.create("module:/com.myorg.mymodule")) {
    ModulePath modulePath = fs.getPath("com/myorg/mymodule/test_resource.txt");
    // Get the internal path of the module path
    Path internalPath = modulePath.getWrappedPath();
    URL usableUrl = internalPath.toUri().toURL();
}
````

In the above example, we explicitly use the ModuleFileSystem class to automatically get ModulePath instances when creating paths.
This allows us to use the `getWrappedPath()` method to obtain the internal path, which returns an `file:`, `jar:`, or `jrt:` URL.
You can then use this URL to access any resources of the module in a normal fashion by passing the URL.
Note that this requires had a file system to be created through the `ModuleFileSystem` class, not the `FileSystem` class.

### Bypassing Encapsulation

One common problem you might encounter when working with modules our permission issues.
Unless a package is open by a certain module, you are not allowed to access the contained resources.
However, this mechanism is implemented on a very high level,
i.e. you can easily bypass it by accessing the underlying file system instead methods like
[getResource()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html#getResource(java.lang.String)).
As ModuleFS does only work through the underlying file systems,
you will not run into any permission issues when using ModuleFS, i.e.
you can even access resources from modules that are not open at all.

### Module References

In case you are loading modules at runtime and want to access the file system of a module before a proper module layer is created,
you can also create a module file system for a
[ModuleReference](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ModuleReference.html) like this:

````java
Path path = ...;
var finder = ModuleFinder.of(path);
var moduleReference = finder.find("myorg.mymodule")
        .orElseThrow(() -> new IllegalArgumentException("Module not found"));
try (var fs = ModuleFileSystem.create(moduleReference)) {
    ...
}
````


## Development

Testing is made more difficult by the fact that we also have to run all tests in a jlink image to achieve good coverage.
Furthermore, junit uses the exploded module structure, so we also have to separately run tests in a module jar file.
Therefore, the tests are defined in the `tests` subproject in the main source directory, which are then called from
the three different environments, exploded module, module jar, and jlink image.

To run all tests, we need to run the following commands:
- `gradle :tests:test` (Exploded module)
- `gradle :tests:run` (Module jar)
- `gradle :tests:createImage` and then run `jlink_tests.bat` (jlink image)

Some features and use cases have not been tested yet.
For example, an application that uses a mix of full modules,
automatic modules, and non-modular jars has not been tested yet.
