# ModuleFS library

The ModuleFS library provides a simple file system implementation to access the contents of Java modules in a unified way.

## Motivation

The [Path](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Path.html) and
[FileSystem](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/FileSystem.html) APIs
introduced in Java 7 with NIO.2 makes working with files much more pleasant.
One hurdle when trying to use these APIs with bundled application resources is that modules can be stored in different formats:
- Exploded module directory, stored as directory tree (Used during development)
- Module artifacts, mostly stored as jars (Used for both development and production)
- Modules contained in a jlink image, which are stored in a proprietary jimage format (Only used for production)

While there are FileSystem implementations like `jdk.zipfs` for jar files and the internal `jrtfs` for jlink images,
there is no unified interface, which results in you having to take care of FileSystem specific differences.
For example, the first challenge is to reliably find out the storage format of a module.
Then, you also have to adapt your code to handle unique properties of the storage format.
For example, every format requires you to close the underlying file
system differently and causes exceptions when not being done properly.

As a result, how to access files in modules using FileSystems heavily depends on how the modules are stored
and can therefore vary between development and production environments.
The current solution to reliably access resources in a package of a jar, or now a module, is to use the old methods
[getResource()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html#getResource(java.lang.String)) and
[getResourceAsStream()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html#getResourceAsStream(java.lang.String)).
These methods are however way more tedious to work with.



## Features

ModuleFS allows you to create a FileSystem using the `module` URI scheme.
The created FileSystem instance conforms to standard FileSystem behaviours
with some limitations on writability as modules are read-only.
A simple file reading example could look like this:
````
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

````
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

## Installation

Note that as this library is relatively new and is primarily used for internal projects, it might not be production ready for other purposes.
If you are still interested in trying it out, you can use it as follows:

#### Gradle

    repositories {
        maven { url "https://jitpack.io" }
    }
    
    dependencies {
        implementation 'com.github.xpipe-io:modulefs:master-SNAPSHOT'
    }

#### Maven

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>

	<dependency>
	    <groupId>com.github.xpipe-io</groupId>
	    <artifactId>modulefs</artifactId>
	    <version>master-SNAPSHOT</version>
	</dependency>

## Development

Testing is made more difficult by the fact that we also have to run all tests in a jlink image to achieve good coverage.
Furthermore, junit uses the exploded module structure so we also have to separately run tests in a module jar file.
Therefore, the tests are defined in the `tests` subproject in the main source directory, which are then called from
the three different environments, exploded module, module jar, and jlink image.

To run all tests, we need to run the following commands:
- `gradle :tests:test` (Exploded module)
- `gradle :tests:run` (Module jar)
- `gradle :tests:createImage` and then run `jlink_tests.bat` (jlink image)
