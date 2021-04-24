package org.monospark.modulefs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

public final class ModuleFileStore extends FileStore {

    private final FileStore wrappedStore;
    private final ModuleFileSystem fs;

    ModuleFileStore(FileStore wrappedStore, ModuleFileSystem fs) {
        this.wrappedStore = wrappedStore;
        this.fs = fs;
    }

    @Override
    public String name() {
        return fs.toString();
    }

    @Override
    public String type() {
        return "modulefs";
    }

    @Override
    public boolean isReadOnly() {
        return wrappedStore.isReadOnly();
    }

    @Override
    public long getTotalSpace() throws IOException {
        return wrappedStore.getTotalSpace();
    }

    @Override
    public long getUsableSpace() throws IOException {
        return wrappedStore.getUsableSpace();
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return wrappedStore.getUnallocatedSpace();
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return wrappedStore.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return wrappedStore.supportsFileAttributeView(name);
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return wrappedStore.getFileStoreAttributeView(type);
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        return wrappedStore.getAttribute(attribute);
    }
}
