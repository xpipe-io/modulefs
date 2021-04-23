package org.monospark.modulefs.tests.junit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.monospark.modulefs.tests.Main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ExplodedModuleTest {

    @Test
    public void testFileSystemRead() throws Exception {
        Main.main(new String[0]);
    }
}
