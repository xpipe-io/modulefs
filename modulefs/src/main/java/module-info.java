import org.monospark.modulefs.ModuleFileSystemProvider;

import java.nio.file.spi.FileSystemProvider;

module org.monospark.modulefs {
    requires jdk.zipfs;

    provides FileSystemProvider with ModuleFileSystemProvider;
}