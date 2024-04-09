package model;

import java.io.File;
import java.util.List;

public class StationMode {
    private String controlType;
    private String fixedMode;
    private File file;

    public Device getControlDevice() {
        return controlDevice;
    }

    public void setControlDevice(Device controlDevice) {
        this.controlDevice = controlDevice;
    }

    private Device  controlDevice;
    private Device  tcDevice;

    public Device getTcDevice() {
        return tcDevice;
    }

    public void setTcDevice(Device tcDevice) {
        this.tcDevice = tcDevice;
    }

    public Device getSimDevice() {
        return simDevice;
    }

    public void setSimDevice(Device simDevice) {
        this.simDevice = simDevice;
    }

    private Device simDevice;
    public boolean isSimIPRequired() {
        return isSimIPRequired;
    }

    public void setSimIPRequired(boolean simIPRequired) {
        isSimIPRequired = simIPRequired;
    }

    public boolean isSimWindowRequired() {
        return isSimWindowRequired;
    }

    public void setSimWindowRequired(boolean simWindowRequired) {
        isSimWindowRequired = simWindowRequired;
    }

    public boolean isTCWindowRequired() {
        return isTCWindowRequired;
    }

    public void setTCWindowRequired(boolean TCWindowRequired) {
        isTCWindowRequired = TCWindowRequired;
    }

    public boolean isControlWindowRequired() {
        return isControlWindowRequired;
    }

    public void setControlWindowRequired(boolean controlWindowRequired) {
        isControlWindowRequired = controlWindowRequired;
    }

    public boolean isControlIPRequired() {
        return isControlIPRequired;
    }

    public void setControlIPRequired(boolean controlIPRequired) {
        isControlIPRequired = controlIPRequired;
    }

    private boolean isSimIPRequired;

    private boolean isSimWindowRequired;

    private boolean isTCWindowRequired;

    private boolean isControlWindowRequired;

    private boolean isControlIPRequired;

    public String getLocalServer() {
        return localServer;
    }

    public void setLocalServer(String localServer) {
        this.localServer = localServer;
    }

    private String localServer;

    public List<Device> getAllDevice() {
        return allDevice;
    }

    public void setAllDevice(List<Device> allDevice) {
        this.allDevice = allDevice;
    }

    private List<Device> allDevice ;


    public String getControlIP() {
        return controlIP;
    }

    public void setControlIP(String controlIP) {
        this.controlIP = controlIP;
    }

    public String getSimIP() {
        return SimIP;
    }

    public void setSimIP(String simIP) {
        SimIP = simIP;
    }

    private String controlIP;
    private String SimIP;
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
