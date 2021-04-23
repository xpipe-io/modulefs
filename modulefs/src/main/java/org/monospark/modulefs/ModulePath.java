package org.monospark.modulefs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

public class ModulePath implements Path {

    private ModuleFileSystem fs;
    private Path wrappedPath;

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
        return wrappedPath.getRoot();
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

    @Override
    public boolean startsWith(Path other) {
        return false;
    }

    @Override
    public boolean startsWith(String other) {
        return false;
    }

    @Override
    public boolean endsWith(Path other) {
        return false;
    }

    @Override
    public boolean endsWith(String other) {
        return false;
    }

    @Override
    public Path normalize() {
        return null;
    }

    @Override
    public Path resolve(Path other) {
        return null;
    }

    @Override
    public Path resolve(String other) {
        return null;
    }

    @Override
    public Path resolveSibling(Path other) {
        return null;
    }

    @Override
    public Path resolveSibling(String other) {
        return null;
    }

    @Override
    public Path relativize(Path other) {
        return null;
    }

    @Override
    public URI toUri() {
        return null;
    }

    @Override
    public Path toAbsolutePath() {
        return null;
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return null;
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return null;
    }

    @Override
    public Iterator<Path> iterator() {
        return null;
    }

    @Override
    public int compareTo(Path other) {
        return 0;
    }
}
