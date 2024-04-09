package model;

import org.sikuli.script.Location;

public class Point extends RailwayObject {

    private boolean bitPK;
    private boolean bitBLK;
    private String imagePathCenterState;



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


    public boolean isBitPK() {
        return bitPK;
    }

    public void setBitPK(boolean bitPK) {
        this.bitPK = bitPK;
    }

    public boolean isBitBLK() {
        return bitBLK;
    }

    public void setBitBLK(boolean bitBLK) {
        this.bitBLK = bitBLK;
    }

    public Point(String id, String name, boolean bitPK, boolean bitBLK, Interlocking interlocking, Location location, Location screenCoordinate){
        super(id,name,interlocking,location, screenCoordinate);
        this.bitPK=bitPK;
        this.bitBLK=bitBLK;

    }
    public String getImagePathCenterState() {
        return imagePathCenterState;
    }

}
