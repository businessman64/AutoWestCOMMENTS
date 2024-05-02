package model;

import java.util.List;

public class SignalInfo {

    protected String signalId;
    
    protected  String type;
    protected String signalTrack;
    protected String relation;
protected  Boolean hasArs;


    public String getSignalId() {
        return signalId;
    }

    public void setSignalId(String signalId) {
        this.signalId = signalId;
    }

    public String getsignalTrack() {
        return signalTrack;
    }

    public void setsignalTrack(String signalTrack) {
        this.signalTrack = signalTrack;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public Boolean getHasArs() {
        return hasArs;
    }

    public void setHasArs(Boolean hasArs) {
        this.hasArs = hasArs;
    }

    public SignalInfo(String signalId, String type, String signalTrack, String relation, Boolean hasARS) {
        this.signalId = signalId;
        this.signalTrack = signalTrack ;
        this.relation = relation;
        this.type = type;
        this.hasArs= hasARS;


    }
    
    
    
}
