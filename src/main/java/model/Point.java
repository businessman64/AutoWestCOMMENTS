package model;

import org.sikuli.script.Location;

public class Point extends RailwayObject {

    private String imagePathNormalState;
    private String imagePathReverseState;
    private String imagePathCenterState;

    public void setImagePathNormalState(String imagePathNormalState) {
        this.imagePathNormalState = imagePathNormalState;
    }

    public void setImagePathReverseState(String imagePathReverseState) {
        this.imagePathReverseState = imagePathReverseState;
    }

    public void setImagePathCenterState(String imagePathCenterState) {
        this.imagePathCenterState = imagePathCenterState;
    }

    public static String getReversedPoint() {
        return reversedPoint;
    }

    public static String getNormalisedPoint() {
        return normalisedPoint;
    }

    public static final String reversedPoint = System.getProperty("user.dir")+"/src/resources/points/point_reverse.png";
    public static final String normalisedPoint = System.getProperty("user.dir")+"/src/resources/points/point_normal.png";;

    public static final String BLOCKPOINT = System.getProperty("user.dir")+"/src/resources/points/point_state_block.png";

    public static final String UNBLOCKPOINT = System.getProperty("user.dir")+"/src/resources/points/point_state_unblock.png";

    public String getImagePathNormalState() {
        return imagePathNormalState;
    }

    public String getImagePathReverseState() {
        return imagePathReverseState;
    }


    public Point(String id, String name, String imagePathNormalState, String imagePathCenterState, String imagePathReverseState, Interlocking interlocking, Location location, Location screenCoordinate){
        super(id,name,interlocking,location, screenCoordinate);
        this.imagePathNormalState = imagePathNormalState;
        this.imagePathReverseState = imagePathReverseState;
        this.imagePathCenterState = imagePathCenterState;
        this.interlocking = interlocking;
    }
    public String getImagePathCenterState() {
        return imagePathCenterState;
    }

}
