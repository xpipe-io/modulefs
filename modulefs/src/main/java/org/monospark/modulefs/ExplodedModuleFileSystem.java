package org.monospark.modulefs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Optional;

public class ExplodedModuleFileSystem extends ModuleFileSystem {

    static Optional<ExplodedModuleFileSystem> create(String module, FileSystemProvider provider, URI location) throws IOException {
        if (location.getScheme().equals("file")) {
            var basePath = Path.of(location);
            return Optional.of(new ExplodedModuleFileSystem(module, basePath, provider));
        }
        return Optional.empty();
    }

    private boolean open = true;

    ExplodedModuleFileSystem(String module, Path basePath, FileSystemProvider provider) {
        super(module, basePath, provider);
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
