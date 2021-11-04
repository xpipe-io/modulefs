# ModuleFS library

This library provides a simple file system to access the contents modules in a unified way.

## Motivation

The [Path](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html) and
[FileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html) APIs
introduced in Java 1.7 with NIO.2 makes working with files much more pleasant.
One hurdle when trying to use these APIs with bundled application resources is that modules can be stored in different formats:
- Exploded module directory, stored as directory tree (Used during development)
- Module artifacts, mostly stored as jars (Used for both development and production)
- Modules contained in a jlink image, which are stored in a proprietary jimage format (Only used for production)

While there are FileSystem implementations like jdk.zipfs for jar files and the internal jrtfs for jlink images,
there is no unified interface, which results in you having to take care of FileSystem specific differences like ... and closeabilty.
As a result, how to access files in modules using FileSystems depends on how the modules are stored
and can therefore vary between development and production environments.
The current solution to reliably access resources in a package of a jar, or now a module, is to use the old methods
[getResource()](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/Class.html#getResource(java.lang.String)) and
[getResourceAsStream()](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/Class.html#getResourceAsStream(java.lang.String)).
These methods are however way more tedious to work with.





## Features

Allows you to create a FileSystem using the scheme `module` with
`FileSystems.newFileSystem(URI.create("module:/com.myorg.mymodule"), Map.of())`.
The created FileSystem behaves like any other FileSystem, as you can see in the examples.


## Examples
