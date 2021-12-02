package io.xpipe.modulefs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Optional;

public final class JrtModuleFileSystem extends ModuleFileSystem {

    static Optional<JrtModuleFileSystem> create(
            String module,
            FileSystemProvider provider,
            URI uri,
            URI location) throws IOException {
        if (location.getScheme().equals("jrt")) {
            String moduleName = uri.getPath().substring(1);
            var fs = FileSystems.newFileSystem(URI.create("jrt:/"), Map.of());
            var basePath = fs.getPath("modules", moduleName);
            return Optional.of(new JrtModuleFileSystem(module, basePath, provider));
        }
        return Optional.empty();
    }

    private boolean open = true;

    JrtModuleFileSystem(String module, Path basePath, FileSystemProvider provider) {
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
