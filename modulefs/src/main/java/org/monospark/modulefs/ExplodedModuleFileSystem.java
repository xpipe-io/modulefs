package org.monospark.modulefs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Optional;

public class ExplodedModuleFileSystem extends ModuleFileSystem {

    static Optional<FileSystem> create(FileSystemProvider provider, URI location) throws IOException {
        if (location.getScheme().equals("file")) {
            var basePath = Path.of(location);
            return Optional.of(new ExplodedModuleFileSystem(basePath, provider));
        }
        return Optional.empty();
    }

    private boolean open = true;

    ExplodedModuleFileSystem(Path basePath, FileSystemProvider provider) {
        super(basePath, provider);
    }

    @Override
    public void close() throws IOException {
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }
}
