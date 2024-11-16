package io.xpipe.modulefs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class JarModuleFileSystem extends ModuleFileSystem {

    static Optional<JarModuleFileSystem> create(
            String module, FileSystemProvider provider, URI modUri) throws IOException {
        if (modUri.getPath().endsWith(".jar")) {
            var fsUri = URI.create("jar:" + modUri.toString());
            FileSystem jarFs;

            Path modFilePath = Path.of(modUri);
            synchronized (openFsCounts) {
                if (openFsCounts.containsKey(modFilePath)) {
                    jarFs = FileSystems.getFileSystem(fsUri);
                } else {
                    jarFs = FileSystems.newFileSystem(fsUri, Map.of());
                    try {
                        var m = jarFs.getClass().getDeclaredMethod("setReadOnly");
                        m.invoke(jarFs);
                    } catch (IllegalAccessException ignored) {
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to make file " + modFilePath + " read-only", e);
                    }
                }
                return Optional.of(new JarModuleFileSystem(module, jarFs.getPath("/"), modFilePath, provider));
            }
        }
        return Optional.empty();
    }

    private static final Map<Path,Integer> openFsCounts = new HashMap<>();

    private final Path jarFilePath;

    JarModuleFileSystem(String module, Path basePath, Path jarFilePath, FileSystemProvider provider) {
        super(module, basePath, provider);
        this.jarFilePath = jarFilePath;
        synchronized (openFsCounts) {
            openFsCounts.put(jarFilePath, openFsCounts.getOrDefault(jarFilePath, 0) + 1);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (openFsCounts) {
            int openCount = openFsCounts.get(this.jarFilePath);
            openFsCounts.put(this.jarFilePath, --openCount);
            if (openCount == 0) {
                basePath.getFileSystem().close();
                openFsCounts.remove(this.jarFilePath);
            }
        }
    }

    @Override
    public boolean isOpen() {
        return basePath.getFileSystem().isOpen();
    }
}
