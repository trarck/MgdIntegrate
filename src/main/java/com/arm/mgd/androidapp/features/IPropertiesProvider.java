package com.arm.mgd.androidapp.features;

import java.io.FileNotFoundException;
import java.util.Properties;

public interface IPropertiesProvider {
    Properties getProperties() throws IPropertiesProvider.PropertyStorageException, FileNotFoundException;

    void setFilename(String var1);

    void storeProperties(Properties var1) throws IPropertiesProvider.PropertyStorageException;

    public static class PropertyStorageException extends Exception {
        public PropertyStorageException(String var1, Throwable var2) {
            super(var1, var2);
        }
    }
}
