import io.xpipe.modulefs.ModuleFileSystemProvider;

import java.nio.file.spi.FileSystemProvider;

module io.xpipe.modulefs {
    requires transitive jdk.zipfs;

    exports io.xpipe.modulefs;
    provides FileSystemProvider with ModuleFileSystemProvider;
}