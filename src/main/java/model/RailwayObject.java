package model;

import org.sikuli.script.Location;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class RailwayObject {

    protected Interlocking interlocking;
    protected String id;
    protected String name;
    protected Location location;
    protected Location screenCoordinates;



    public RailwayObject(String id, String name, Interlocking interlocking, Location location, Location screenCoordinate){
        this.id = id;
        this.name = name;
        this.interlocking = interlocking;
        this.location = location;
        this.screenCoordinates = screenCoordinate;
    }
    public Interlocking getInterlocking() {
        return interlocking;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setInterlocking(Interlocking interlocking) {
        this.interlocking = interlocking;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getScreenCoordinates() {
        return screenCoordinates;
    }

    public void setScreenCoordinates(Location screenCoordinates) {
        this.screenCoordinates = screenCoordinates;
    }


}
