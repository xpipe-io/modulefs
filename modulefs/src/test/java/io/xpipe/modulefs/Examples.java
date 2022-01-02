package io.xpipe.modulefs;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public class Examples {

    public static void complexExample() throws IOException {
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
    }

    public static void urlExample() throws IOException {
        try (ModuleFileSystem fs =
                     ModuleFileSystem.create("module:/com.myorg.mymodule")) {
            ModulePath modulePath = fs.getPath("com/myorg/mymodule/test_resource.txt");
            // Get the internal path of the module path
            Path internalPath = modulePath.getWrappedPath();
            URL usableUrl = internalPath.toUri().toURL();
        }
    }
}
