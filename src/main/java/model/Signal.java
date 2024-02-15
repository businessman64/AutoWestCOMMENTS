package model;

import org.sikuli.script.*;


public class Signal extends RailwayObject{


    private Boolean blockDisabled;

    private Boolean fleetDisabled;
    private Boolean disregardDisabled;
    private Boolean arsDisabled;
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

    public Signal(String id, String name, String type, String signalTrack, String relation, Boolean blockDisabled, Boolean fleetDisabled,
                  Boolean arsDisabled, Boolean disregardDisabled,
                  Interlocking interlocking, Location location,
                  Location screenCoordinate){
        super(id,name,interlocking,location,screenCoordinate);
        this.blockDisabled = blockDisabled;
        this.fleetDisabled = fleetDisabled;
        this.arsDisabled = arsDisabled;
        this.disregardDisabled = disregardDisabled;
        this.signalTrack = signalTrack ;
        this.relation = relation;
        this.type = type;

    }

    public void setBlockDisabled(Boolean blockDisabled) {
        this.blockDisabled = blockDisabled;
    }
    public Boolean getBlockDisabled() {
        return blockDisabled;
    }

    public void setFleetDisabled(Boolean fleetDisabled) {
        this.fleetDisabled = fleetDisabled;
    }
    public Boolean getFleetDisabled() {
        return fleetDisabled;
    }


    public void setDisregardDisabled(Boolean disregardDisabled) {
        this.disregardDisabled = disregardDisabled;
    }
    public Boolean getDisregardDisabled() {
        return disregardDisabled;
    }

    public void setArsDisabled(Boolean arsDisabled) {
        this.arsDisabled = arsDisabled;
    }
    public Boolean getArsDisabled() {
        return arsDisabled;
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
