package org.monospark.modulefs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModuleFileSystemProvider extends FileSystemProvider {

    private final Map<String, FileSystem> filesystems = new HashMap<>();

    public ModuleFileSystemProvider() {
    }

    @Override
    public String getScheme() {
        return "module";
    }

    private void checkUri(URI uri) {
        if (!uri.getScheme().equalsIgnoreCase(getScheme())) {
            throw new IllegalArgumentException("URI does not match this provider");
        }
        if (uri.getAuthority() != null) {
            throw new IllegalArgumentException("Authority component present");
        }
        if (uri.getPath() == null) {
            throw new IllegalArgumentException("Path component is undefined");
        }
        String path = uri.getPath();
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path component should start with '/'");
        }
        if (path.substring(1).contains("/")) {
            throw new IllegalArgumentException("Path component should contain '/' other than at the start");
        }
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path component");
        }

        if (uri.getQuery() != null) {
            throw new IllegalArgumentException("Query component present");
        }
        if (uri.getFragment() != null) {
            throw new IllegalArgumentException("Fragment component present");
        }
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        checkUri(uri);

        String moduleName = uri.getPath().substring(1);
        var loc = ModuleHelper.getModuleForName(moduleName)
                .orElseThrow(() -> new ModuleNotResolvedException(
                        "Module " + moduleName + " was not resolved")).reference().location();
        var modUri = loc.orElseThrow(() -> new IllegalArgumentException(
                "Location of module " + moduleName + " is unknown"));

        FileSystem actualFs;
        if (modUri.getScheme().equals("jrt")) {
            var fs = FileSystems.newFileSystem(URI.create("jrt:/"), env);
            var basePath = fs.getPath("modules", moduleName);
            actualFs = new ModuleFileSystem(env != null, basePath);
        } else if (modUri.getPath().endsWith(".jar")) {
            var jarFs = FileSystems.newFileSystem(URI.create("jar:" + modUri.toString()), env);
            actualFs = new ModuleFileSystem(true, jarFs.getPath("/"));
        } else {
            actualFs = new ModuleFileSystem(false, Path.of(modUri));
        }
        filesystems.put(moduleName, actualFs);
        return actualFs;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        checkUri(uri);

        synchronized (filesystems) {
            var moduleName = uri.getPath().substring(1);
            var fs = filesystems.get(moduleName);
            if (fs == null) {
                try {
                    fs = newFileSystem(uri, Map.of());
                    filesystems.put(moduleName, fs);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return fs;
        }
    }

    @Override
    public Path getPath(URI uri) {
        var path = uri.getPath().substring(1);
        var moduleName = path;
        var moduleNameEnd = path.indexOf("/");
        if (moduleNameEnd != -1) {
            moduleName = moduleName.substring(0, moduleNameEnd);
        }
        var inModulePath = path.substring(moduleNameEnd + 1);

        var fs = getFileSystem(URI.create("module:/" + moduleName));

        return fs.getPath(inModulePath);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }
}
