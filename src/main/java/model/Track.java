package model;

import org.sikuli.script.*;
import util.CmdLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Track extends RailwayObject{

    private String imagePath;
    private String imagePathBlockedState;

    private String imagePathDisregardedState;

    private Location location;
    private String circuitName;
    private String trackNormalDown;
    private String trackNormalUp;
    private String trackReverseDown;
    private String TrackReverseUp;

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setImagePathBlockedState(String imagePathBlockedState) {
        this.imagePathBlockedState = imagePathBlockedState;
    }

    public void setImagePathDisregardedState(String imagePathDisregardedState) {
        this.imagePathDisregardedState = imagePathDisregardedState;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
    }

    public String getTrackNormalDown() {
        return trackNormalDown;
    }

    public void setTrackNormalDown(String trackNormalDown) {
        this.trackNormalDown = trackNormalDown;
    }

    public String getTrackNormalUp() {
        return trackNormalUp;
    }

    public void setTrackNormalUp(String trackNormalUp) {
        this.trackNormalUp = trackNormalUp;
    }

    public String getTrackReverseDown() {
        return trackReverseDown;
    }

    public void setTrackReverseDown(String trackReverseDown) {
        this.trackReverseDown = trackReverseDown;
    }

    public String getTrackReverseUp() {
        return TrackReverseUp;
    }

    public void setTrackReverseUp(String trackReverseUp) {
        TrackReverseUp = trackReverseUp;
    }

    public Track(String id, String name,
                 String circuitName, String trackNormalDown ,
                 String trackNormalUp, String trackReverseDown, String TrackReverseUp,
                 Interlocking interlocking, Location location, Location screenCoordinate){
        super(id,name,interlocking,location, screenCoordinate);
        this.circuitName = circuitName ;
        this.trackNormalDown = trackNormalDown;
        this.trackNormalUp = trackNormalUp ;
        this.trackReverseDown= trackReverseDown;
        this.TrackReverseUp= TrackReverseUp;
    }
    public String getImagePath() {
        return imagePath;
    }

    public String getImagePathBlockedState() {
        return imagePathBlockedState;
    }
    public String getImagePathDisregardedState() {
        return imagePathDisregardedState;
    }

}
