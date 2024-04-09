package model;

import java.util.List;

public class BerthInfo {
    private String Id;
    private String trackId;

    private int capacity;

    private String direction ;

    private String layer3Name ;

    private List<String> overView;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

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

    public String getLayer3Name() {
        return layer3Name;
    }

    public void setLayer3Name(String layer3Name) {
        this.layer3Name = layer3Name;
    }


    public List<String> getOverView() {
        return overView;
    }

    public void setOverView(List<String> overView) {
        this.overView = overView;
    }

    public BerthInfo(String Id, String trackId, int capacity, String direction, String layer3Name, List<String> overView)
    {
        this.Id= Id;
        this.capacity=capacity;
        this.direction= direction;
        this.trackId= trackId;
        this.layer3Name= layer3Name;
        this.overView= overView;
    }
}
