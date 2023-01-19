package io.xpipe.modulefs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Stream;

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

    private Optional<ResolvedModule> resolveModule(String name, ModuleLayer l) {
        var found = l.configuration().modules().stream()
                .filter(r -> r.name().equals(name))
                .findFirst();
        if (found.isPresent()) {
            return found;
        }

        for (var p : l.parents()) {
            var r = resolveModule(name, p);
            if (r.isPresent()) {
                return r;
            }
        }

        return Optional.empty();
    }

    @Override
    public ModuleFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        checkUri(uri);

        var layer = env.containsKey("layer") ? (ModuleLayer) env.get("layer") : ModuleLayer.boot();
        var moduleLocation = env.containsKey("location") ? (URI) env.get("location") : null;
        String moduleName = uri.getPath().substring(1);

        if (moduleLocation == null) {
            var loc = resolveModule(moduleName, layer)
                    .orElseThrow(() -> new FileSystemNotFoundException(
                            "Module " + moduleName + " was not resolved"));
            moduleLocation = loc.reference().location().orElseThrow(() -> new IllegalArgumentException(
                    "Location of module " + moduleName + " is unknown"));
        }

        var scheme = moduleLocation.getScheme();
        var fs = Stream.of(
                JrtModuleFileSystem.create(moduleName, this, uri, moduleLocation),
                JarModuleFileSystem.create(moduleName, this, moduleLocation),
                ExplodedModuleFileSystem.create(moduleName, this, moduleLocation))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported module file system type " + scheme));
        filesystems.put(moduleName, fs);
        return fs;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        checkUri(uri);

        synchronized (filesystems) {
            var moduleName = uri.getPath().substring(1);
            var fs = filesystems.get(moduleName);
            if (fs == null) {
                throw new FileSystemNotFoundException("No FileSystem for module " + moduleName + " found");
            }

            if (!fs.isOpen()) {
                filesystems.remove(moduleName);
                throw new FileSystemNotFoundException("Existing FileSystem for module " + moduleName + " is closed");
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

        try {
            var fs = getFileSystem(URI.create("module:/" + moduleName));
            return fs.getPath(inModulePath);
        } catch (FileSystemNotFoundException e) {
            try {
                var fs = newFileSystem(URI.create("module:/" + moduleName), Map.of());
                return fs.getPath(inModulePath);
            } catch (IOException ioException) {
                throw new UncheckedIOException(ioException);
            }
        }

    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        if (!options.isEmpty() && !options.equals(Set.of(StandardOpenOption.READ))) {
            throw new UnsupportedOperationException();
        }

        var mp = ((ModulePath) path);
        return mp.getModuleFileSystem().getBaseProvider().newByteChannel(mp.getWrappedPath(), options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        var mp = ((ModulePath) dir);
        var ds = mp.getModuleFileSystem().getBaseProvider().newDirectoryStream(mp.getWrappedPath(), filter);
        return new ModuleDirectoryStream(mp.getModuleFileSystem(), ds);
    }

    private ModulePath getModulePath(Path path) {
        Objects.requireNonNull(path, "path");
        if (!(path instanceof ModulePath)) {
            throw new ProviderMismatchException();
        }
        return (ModulePath) path;
    }

    private FileSystemProvider getBaseProvider(Path path) {
        return getModulePath(path).getModuleFileSystem().getBaseProvider();
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        getBaseProvider(dir).createDirectory(getModulePath(dir).getWrappedPath(), attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        getBaseProvider(path).delete(getModulePath(path).getWrappedPath());
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        getBaseProvider(source).copy(getModulePath(source).getWrappedPath(),
                getModulePath(target).getWrappedPath(), options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        getBaseProvider(source).move(getModulePath(source).getWrappedPath(),
                getModulePath(target).getWrappedPath(), options);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return getBaseProvider(path).isSameFile(
                getModulePath(path).getWrappedPath(),
                getModulePath(path2).getWrappedPath());
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return getBaseProvider(path).isHidden(getModulePath(path).getWrappedPath());
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }

        return getModulePath(path).getFileSystem().getFileStores().iterator().next();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        getBaseProvider(path).checkAccess(getModulePath(path).getWrappedPath(), modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return getBaseProvider(path).getFileAttributeView(getModulePath(path).getWrappedPath(), type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return getBaseProvider(path).readAttributes(getModulePath(path).getWrappedPath(), type, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return getBaseProvider(path).readAttributes(getModulePath(path).getWrappedPath(), attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        getBaseProvider(path).setAttribute(getModulePath(path).getWrappedPath(), attribute, value, options);
    }
}
