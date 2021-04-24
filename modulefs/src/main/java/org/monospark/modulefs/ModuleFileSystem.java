package org.monospark.modulefs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ModuleFileSystem extends FileSystem {

    public static ModuleFileSystem create(String uri) throws IOException {
        ModuleFileSystemProvider fsp = FileSystemProvider.installedProviders().stream()
                .filter(p -> p instanceof ModuleFileSystemProvider)
                .map(p -> (ModuleFileSystemProvider) p)
                .findFirst()
                .orElseThrow(() -> new ProviderNotFoundException("modulefs provider not found"));
        return fsp.newFileSystem(URI.create(uri), Map.of());
    }

    private final String module;
    private final FileSystemProvider provider;
    protected Path basePath;

    ModuleFileSystem(String module, Path basePath, FileSystemProvider provider) {
        this.module = module;
        this.basePath = basePath;
        this.provider = provider;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    FileSystemProvider getBaseProvider() {
        return basePath.getFileSystem().provider();
    }

    Path getRoot() {
        return new ModulePath(this, basePath);
    }

    @Override
    public boolean isReadOnly() {
        return basePath.getFileSystem().isReadOnly();
    }

    @Override
    public String getSeparator() {
        return basePath.getFileSystem().getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Set.of(basePath);
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        var store = basePath.getFileSystem().getFileStores().iterator().next();
        return List.of(new ModuleFileStore(store, this));
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return basePath.getFileSystem().supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        var current = basePath.resolve(first);
        for (var m : more) {
            current = current.resolve(m);
        }
        return new ModulePath(this, current);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return basePath.getFileSystem().getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return basePath.getFileSystem().getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return basePath.getFileSystem().newWatchService();
    }

    public String getModule() {
        return module;
    }
}
