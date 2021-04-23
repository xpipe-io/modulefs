package org.monospark.modulefs.tests;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class CommonTests {

    public void testFileSystemRead() throws IOException {
        try (var fs = FileSystems.newFileSystem(
                URI.create("module:/org.monospark.modulefs.tests"), Map.of())) {
            var p1 = fs.getPath("org/monospark/modulefs/tests/test_resource.txt");
            Assertions.assertEquals("resource", Files.readString(p1));
        }
    }

    public void testFileSystemWrite() throws IOException {
        try (var fs = FileSystems.newFileSystem(
                URI.create("module:/org.monospark.modulefs.tests"), Map.of())) {
            var p1 = fs.getPath("org/monospark/modulefs/tests/test_resource.txt");
            Assertions.assertThrows(UnsupportedOperationException.class, () -> Files.writeString(p1, "test"));
        }
    }

    public void testMultipleSequentialFileSystemRead() throws IOException {
        var mod = "module:/org.monospark.modulefs.tests";
        try (var fs = FileSystems.newFileSystem(
                URI.create(mod), Map.of())) {
            var p = fs.getPath("org/monospark/modulefs/tests/test_resource.txt");
            Assertions.assertEquals("resource", Files.readString(p));
        }

        try (var fs = FileSystems.newFileSystem(
                URI.create(mod), Map.of())) {
            var p = fs.getPath("org/monospark/modulefs/tests/empty_file.txt");
            Assertions.assertEquals("", Files.readString(p));
        }
    }

    public void testMultipleParallelFileSystemRead() throws IOException {
        var mod = "module:/org.monospark.modulefs.tests";
        try (var fs = FileSystems.newFileSystem(
                URI.create(mod), Map.of())) {

            try (var fs2 = FileSystems.newFileSystem(
                    URI.create(mod), Map.of())) {
                var p2 = fs2.getPath("org/monospark/modulefs/tests/empty_file.txt");
                Assertions.assertEquals("", Files.readString(p2));
            }

            var p = fs.getPath("org/monospark/modulefs/tests/test_resource.txt");
            Assertions.assertEquals("resource", Files.readString(p));
        }
    }

    public void testInvalidURIs() {
        Arrays.stream(new String[] {
                "module:/org.monospark.modulefs.tests/",
                "module:/org.monospark.modulefs.tests/org/../",
                "module:org.monospark.modulefs.tests",
                "module://org.monospark.modulefs.tests",
                "module:/org.monospark.modulefs.tests#frag",
                "module:/abc/org.monospark.modulefs.tests",
                "module:/org.monospark.modulefs.tests?abc=d"

        }).forEach(s -> {
            Assertions.assertThrows(IllegalArgumentException.class, () -> FileSystems.newFileSystem(
                    URI.create(s), Map.of()));
        });
    }

    public void testGetFileSystem() throws IOException {
        Assertions.assertThrows(FileSystemNotFoundException.class,
                () -> FileSystems.getFileSystem(URI.create("module:/org.monospark.modulefs.tests")));

        var fs = FileSystems.newFileSystem(URI.create("module:/org.monospark.modulefs.tests"), Map.of());
        var gotFs = FileSystems.getFileSystem(URI.create("module:/org.monospark.modulefs.tests"));
        Assertions.assertEquals(fs, gotFs);

        fs.close();
        Assertions.assertThrows(FileSystemNotFoundException.class,
                () -> FileSystems.getFileSystem(URI.create("module:/org.monospark.modulefs.tests")));
    }

    public void testInvalidModule() throws IOException {
        Assertions.assertThrows(FileSystemNotFoundException.class, () -> Path.of(URI.create(
                "module:/invalid.module/")));
    }

    public void testReadFromURI() throws IOException {
        var path = Path.of(URI.create(
                "module:/org.monospark.modulefs.tests/org/monospark/modulefs/tests/test_resource.txt"));
        Assertions.assertEquals(Files.readString(path), "resource");
        path.getFileSystem().close();
    }

    public void testDirectoryFromURI() throws IOException {
        var path = Path.of(URI.create("module:/org.monospark.modulefs.tests/org/monospark/modulefs/tests/"));
        Assertions.assertTrue(Files.list(path).anyMatch(p -> p.getFileName().toString().equals("empty_file.txt")));
        path.getFileSystem().close();
    }
}
