package model;

import org.sikuli.script.Location;

public class Berth extends RailwayObject{
    private String trackId;
    private int capacity;
    private String direction;

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Berth(String id, String name, String trackId, int capacity, String direction,
                 Interlocking interlocking, Location location, Location screenCoordinate){
        super(id,name,interlocking,location, screenCoordinate);
     this.trackId=trackId;
     this.capacity=capacity;
     this.direction= direction;
    }
}
