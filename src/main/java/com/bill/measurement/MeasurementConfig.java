package com.bill.measurement;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Remembers whether {@code /measurement on|off} was last set to on, in config/measurement.properties. */
public final class MeasurementConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementClient.MOD_ID);
    private static final String ENABLED = "enabled";

    private static volatile boolean enabled = true;

    private MeasurementConfig() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    static void load() {
        Path file = file();
        if (!Files.isRegularFile(file)) {
            return;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
            enabled = !"false".equalsIgnoreCase(properties.getProperty(ENABLED, "true").trim());
        } catch (IOException e) {
            LOGGER.warn("Could not read {}", file, e);
        }
    }

    private static void save() {
        Path file = file();
        Properties properties = new Properties();
        properties.setProperty(ENABLED, Boolean.toString(enabled));
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                properties.store(writer, "Measurement: set with /measurement on|off");
            }
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve(MeasurementClient.MOD_ID + ".properties");
    }
}
