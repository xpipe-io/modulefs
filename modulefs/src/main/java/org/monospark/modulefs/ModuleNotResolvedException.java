package org.monospark.modulefs;

public class ModuleNotResolvedException extends RuntimeException {

    public ModuleNotResolvedException(String message) {
        super(message);
    }
}
