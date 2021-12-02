package io.xpipe.modulefs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Objects;

public class ModulePath implements Path {

    private final ModuleFileSystem fs;
    private final Path wrappedPath;

    ModulePath(ModuleFileSystem fs, Path wrappedPath) {
        this.fs = fs;
        this.wrappedPath = wrappedPath;
    }

    ModuleFileSystem getModuleFileSystem() {
        return fs;
    }

    Path getWrappedPath() {
        return wrappedPath;
    }

    @Override
    public FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return wrappedPath.isAbsolute();
    }

    @Override
    public Path getRoot() {
        return fs.getRoot();
    }

    @Override
    public Path getFileName() {
        return wrappedPath.getFileName();
    }

    @Override
    public Path getParent() {
        return wrappedPath.getParent();
    }

    @Override
    public int getNameCount() {
        return wrappedPath.getNameCount();
    }

    @Override
    public Path getName(int index) {
        return wrappedPath.getName(index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return wrappedPath.subpath(beginIndex, endIndex);
    }

    private Path getNullableWrappedPathInternal(Path other) {
        Objects.requireNonNull(other, "other");
        if (!(other instanceof ModulePath)) {
            return null;
        }
        var cast = (ModulePath) other;
        if (!cast.fs.equals(fs)) {
            return null;
        }

        return cast.wrappedPath;
    }

    private Path getNonNullWrappedPathInternal(Path other) {
        Objects.requireNonNull(other, "other");
        if (!(other instanceof ModulePath)) {
            throw new ProviderMismatchException();
        }
        var cast = (ModulePath) other;
        return cast.wrappedPath;
    }

    @Override
    public String toString() {
        return wrappedPath.toString();
    }

    @Override
    public boolean startsWith(Path other) {
        var wp = getNullableWrappedPathInternal(other);
        return wp != null && wrappedPath.startsWith(wp);
    }

    @Override
    public boolean startsWith(String other) {
        return startsWith(fs.getPath(other));
    }

    @Override
    public boolean endsWith(Path other) {
        var wp = getNullableWrappedPathInternal(other);
        return wp != null && wrappedPath.endsWith(wp);
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(fs.getPath(other));
    }

    @Override
    public Path normalize() {
        return new ModulePath(fs, wrappedPath.normalize());
    }

    @Override
    public Path resolve(Path other) {
        var wp = getNonNullWrappedPathInternal(other);
        if (wp.isAbsolute()) {
            return other;
        }

        return new ModulePath(fs, wrappedPath.resolve(wp));
    }

    @Override
    public Path resolve(String other) {
        return new ModulePath(fs, wrappedPath.resolve(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        var wp = getNonNullWrappedPathInternal(other);
        if (wp.isAbsolute()) {
            return other;
        }

        return new ModulePath(fs, wrappedPath.resolveSibling(wp));
    }

    @Override
    public Path resolveSibling(String other) {
        return new ModulePath(fs, wrappedPath.resolveSibling(other));
    }

    @Override
    public Path relativize(Path other) {
        var wp = getNonNullWrappedPathInternal(other);
        return new ModulePath(fs, wrappedPath.relativize(wp));
    }

    @Override
    public URI toUri() {
        return URI.create("module:/" + fs.getModule() + "!/" + fs.basePath.relativize(wrappedPath).toString());
    }

    @Override
    public Path toAbsolutePath() {
        return new ModulePath(fs, wrappedPath.toAbsolutePath());
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return new ModulePath(fs, wrappedPath.toRealPath(options));
    }

    @Override
    public File toFile() {
        return wrappedPath.toFile();
    }

    @Override
    public WatchKey register(
            WatchService watcher,
            WatchEvent.Kind<?>[] events,
            WatchEvent.Modifier... modifiers) throws IOException {
        return wrappedPath.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return wrappedPath.register(watcher, events);
    }

    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {
            private final Iterator<Path> wrappedIterator = wrappedPath.iterator();

            @Override
            public boolean hasNext() {
                return wrappedIterator.hasNext();
            }

            @Override
            public Path next() {
                return new ModulePath(fs, wrappedIterator.next());
            }
        };
    }

    @Override
    public int compareTo(Path other) {
        var wp = getNonNullWrappedPathInternal(other);
        return wrappedPath.compareTo(wp);
    }


}
