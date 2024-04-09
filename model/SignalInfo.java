package model;

import java.util.List;

public class SignalInfo {

    protected String signalId;
    
    protected  String type;
    protected String signalTrack;
    protected String relation;

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

    public SignalInfo(String signalId, String type, String signalTrack, String relation) {
        this.signalId = signalId;
        this.signalTrack = signalTrack ;
        this.relation = relation;
        this.type = type;

    }
    
    
    
}
