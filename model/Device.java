package model;
import javafx.beans.property.SimpleStringProperty;

public class Device {
    private final SimpleStringProperty type;
    private final SimpleStringProperty ipAddress;
    private final SimpleStringProperty screen;
    private final SimpleStringProperty isScreenShotSelected;


    public View getScreenDeatils() {
        return screenDeatils;
    }

    public void setScreenDeatils(View screenDeatils) {
        this.screenDeatils = screenDeatils;
    }

    private View screenDeatils;
    public String getIsScreenShotSelected() {
        return isScreenShotSelected.get();
    }

    public SimpleStringProperty isScreenShotSelectedProperty() {
        return isScreenShotSelected;
    }

    public void setIsScreenShotSelected(String isScreenShotSelected) {
        this.isScreenShotSelected.set(isScreenShotSelected);
    }

    public Device(String type, String ipAddress, String screen, String isScreenShotSelected, View screenDeatils) {
        this.type = new SimpleStringProperty(type);
        this.ipAddress = new SimpleStringProperty(ipAddress);
        this.screen = new SimpleStringProperty(screen);
        this.isScreenShotSelected = new SimpleStringProperty(isScreenShotSelected);
        this.screenDeatils = screenDeatils;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
    }

    public String getScreen() {
        return screen.get();
    }

    public void setScreen(String screen) {
        this.screen.set(screen);
    }




}

