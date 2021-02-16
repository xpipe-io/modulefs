# ModuleFS library

This library provides a simple file system to access the contents modules in a unified way.

## Motivation

Since version 1.7, Java provides the
[FileSystem](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html) and
[Path](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html) classes, which make working with files much easier.

However, to reliably access resources in a package of a jar, or now a module, you still have to use the old methods
[getResource()](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/Class.html#getResource(java.lang.String)) and
[getResourceAsStream()](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/Class.html#getResourceAsStream(java.lang.String)),
because modules can be stored in different formats:
- Exploded module directory, stored as directory tree (Used during development)
- Module artifacts, mostly stored as jars (Used for both development and production)
- Modules contained in a JLink image in a proprietary jimage format (Only used for production)

While there are FileSystem implementations like jdk.zipfs for jar files and the internal jrtfs for jimage modules,
there is no unified interface and you have to take care of FileSystem specific differences like ... and closeabilty.
As a result, how to access files in modules using FileSystems depends on how the modules are stored
and can therefore vary between development and production environments.

## Features

Allows you to create a FileSystem using the scheme `module` with
`FileSystems.newFileSystem(URI.create("module:/com.myorg.mymodule"), Map.of())`.
The created FileSystem behaves like any other FileSystem, as you can see in the examples.


## Examples
