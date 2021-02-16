package org.monospark.modulefs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;

public class ModuleHelper {

    public static Optional<ResolvedModule> getModuleForName(String moduleName) {
        return ModuleLayer.boot().configuration().modules().stream()
                .filter(r -> r.name().equals(moduleName))
                .findFirst();
    }
}
