package model;

import org.sikuli.script.*;


public class Signal extends RailwayObject{


    private Boolean block;

    private Boolean fleet;
    private Boolean disregard;
    private Boolean ars;

    public Boolean getLowSpeed() {
        return lowSpeed;
    }

    public void setLowSpeed(Boolean lowSpeed) {
        this.lowSpeed = lowSpeed;
    }

    private Boolean lowSpeed;

    protected String signalTrack;
    protected String relation;



    private String type;

    public String getSignalTrack() {
        return signalTrack;
    }

    public void setSignalTrack(String signalTrack) {
        this.signalTrack = signalTrack;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getBlock() {
        return block;
    }

    public void setBlock(Boolean block) {
        this.block = block;
    }

    public Boolean getFleet() {
        return fleet;
    }

    public void setFleet(Boolean fleet) {
        this.fleet = fleet;
    }

    public Boolean getDisregard() {
        return disregard;
    }

    public void setDisregard(Boolean disregard) {
        this.disregard = disregard;
    }

    public Boolean getArs() {
        return ars;
    }

    public void setArs(Boolean ars) {
        this.ars = ars;
    }

    public boolean isLowSpeed() {
        return lowSpeed;
    }

    public void setLowSpeed(boolean lowSpeed) {
        this.lowSpeed = lowSpeed;
    }

    public Signal(String id, String name, String type, String signalTrack, String relation, Boolean block, Boolean fleet,
                  Boolean ars, Boolean disregard, Boolean lowSpeed,
                  Interlocking interlocking, Location location,
                  Location screenCoordinate){
        super(id,name,interlocking,location,screenCoordinate);
        this.block = block;
        this.fleet = fleet;
        this.ars= ars;
        this.disregard= disregard;
        this.lowSpeed= lowSpeed;
        this.signalTrack = signalTrack ;
        this.relation = relation;
        this.type = type;

    }


    public String getsignalTrack() {
        return signalTrack;
    }



    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

}
