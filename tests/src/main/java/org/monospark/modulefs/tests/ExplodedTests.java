package org.monospark.modulefs.tests;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ExplodedTests {

    public void testClose() throws IOException {
        try (var fs = FileSystems.newFileSystem(
                URI.create("module:/org.monospark.modulefs.tests"), Map.of())) {
            var p1 = fs.getPath("org/monospark/modulefs/tests/test_resource.txt");
            Assertions.assertEquals(Files.readString(p1), "resource");
        }
    }
}
