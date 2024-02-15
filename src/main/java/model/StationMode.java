package model;

import java.io.File;

public class StationMode {
    private String controlType;
    private String fixedMode;
    private File file;
    private static StationMode instance;

    private StationMode() {
        // Private constructor to prevent instantiation
    }

    public static synchronized StationMode getInstance() {
        if (instance == null) {
            instance = new StationMode();
        }
        return instance;
    }

    // Getters and Setters
    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public String getFixedMode() {
        return fixedMode;
    }

    public void setFixedMode(String fixedMode) {
        this.fixedMode = fixedMode;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
