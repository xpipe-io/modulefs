package org.monospark.modulefs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

public class ModuleDirectoryStream implements DirectoryStream<Path> {

    private ModuleFileSystem fs;
    private DirectoryStream<Path> wrapped;

    public ModuleDirectoryStream(ModuleFileSystem fs, DirectoryStream<Path> wrapped) {
        this.fs = fs;
        this.wrapped = wrapped;
    }

    @Override
    public Iterator<Path> iterator() {
        var it = wrapped.iterator();
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Path next() {
                return new ModulePath(fs, it.next());
            }
        };
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }
}
