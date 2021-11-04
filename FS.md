## Why the Java FileSystem API is bad

### No distinction between absolute and relative paths

    void checkFile(Path target, Path toCheck) {
        FileSystem fs = FileSystems.newFileSystem(target, Map.of());
        fs.
    }

One easy solution to this would be to create a `AbstractPath` class
that only supports operations applicable to all types of paths and is not bound to a file system.
