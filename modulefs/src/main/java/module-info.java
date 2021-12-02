import io.xpipe.modulefs.ModuleFileSystemProvider;

import java.nio.file.spi.FileSystemProvider;

module io.xpipe.modulefs {
    requires jdk.zipfs;

    provides FileSystemProvider with ModuleFileSystemProvider;
}