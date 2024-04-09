package model;

public class TrackInfo {
    protected String trackId;
    protected String circuitName;
    protected String trackNormalDown;
    protected String trackNormalUp;
    protected String trackReverseDown;
    protected String TrackReverseUp;



    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
    }

    public String getTrackNormalDown() {
        return trackNormalDown != null ? trackNormalDown : "";
    }

    public void setTrackNormalDown(String trackNormalDown) {
        this.trackNormalDown = trackNormalDown;
    }

    public String getTrackNormalUp() {
        return trackNormalUp != null ? trackNormalUp : "";
    }

    public void setTrackNormalUp(String trackNormalUp) {
        this.trackNormalUp = trackNormalUp;
    }

    public String getTrackReverseDown() {
        return trackReverseDown != null ? trackReverseDown : "";
    }

    public void setTrackReverseDown(String trackReverseDown) {
        this.trackReverseDown = trackReverseDown;
    }

    public String getTrackReverseUp() {
        return TrackReverseUp != null ? TrackReverseUp : "";
    }

    public void setTrackReverseUp(String trackReverseUp) {
        TrackReverseUp = trackReverseUp;
    }

    public TrackInfo(String trackId, String circuitName, String trackNormalDown ,
                     String trackNormalUp, String trackReverseDown, String TrackReverseUp)
    {
        this.trackId = trackId;
        this.circuitName = circuitName ;
        this.trackNormalDown = trackNormalDown;
        this.trackNormalUp = trackNormalUp ;
        this.trackReverseDown= trackReverseDown;
        this.TrackReverseUp= TrackReverseUp;

    }


}
