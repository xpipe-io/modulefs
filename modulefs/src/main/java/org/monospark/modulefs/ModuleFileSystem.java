package org.monospark.modulefs;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public abstract class ModuleFileSystem extends FileSystem {

    private FileSystemProvider provider;
    protected Path basePath;

    ModuleFileSystem(Path basePath, FileSystemProvider provider) {
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
        return null;
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
}
