package io.xpipe.modulefs;

import java.io.IOException;
import java.net.URI;
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
}
