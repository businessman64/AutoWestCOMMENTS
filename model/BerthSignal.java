package model;

import org.sikuli.script.*;

import java.util.ArrayList;
import java.util.List;


public class BerthSignal {

    private static final List<BerthSignal> instances = new ArrayList<>();

    private String signal;

    private String entrySignal;
    private String behindTrack;
    private String berthTrack;
    protected String spadTrack;

    public String getBerthStored() {
        return berthStored;
    }

    public void setBerthStored(String berthStored) {
        this.berthStored = berthStored;
    }

    private String berthStored;

    public BerthSignal(String signal, String entrySignal, String behindTrack,
                       String berthTrack, String spadTrack, String berthStored) {
        this.signal = signal;
        this.entrySignal = entrySignal;
        this.behindTrack = behindTrack;
        this.berthTrack = berthTrack;
        this.spadTrack = spadTrack;
this.berthStored = berthStored;
        instances.add(this);
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getEntrySignal() {
        return entrySignal;
    }

    public void setEntrySignal(String entrySignal) {
        this.entrySignal = entrySignal;
    }

    public String getBehindTrack() {
        return behindTrack;
    }

    public void setBehindTrack(String behindTrack) {
        this.behindTrack = behindTrack;
    }

    public String getBerthTrack() {
        return berthTrack;
    }

    public void setBerthTrack(String berthTrack) {
        this.berthTrack = berthTrack;
    }

    public String getSpadTrack() {
        return spadTrack;
    }

    public void setSpadTrack(String spadTrack) {
        this.spadTrack = spadTrack;
    }
    public static void removeAllInstances() {
        instances.clear();
    }
}
