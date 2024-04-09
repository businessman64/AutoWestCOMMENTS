package model;

import java.util.List;

public class OverViewBerth{
    private String overViewId;
    private List<String> berthIds ;



    public String getOverViewId() {
        return overViewId;
    }

    public void setOverViewId(String overViewId) {
        this.overViewId = overViewId;
    }

    public List<String> getBerthIds() {
        return berthIds;
    }

    public void setBerthIds(List<String> berthIds) {
        this.berthIds = berthIds;
    }

    public OverViewBerth(String overViewId, List<String> berthIds){
     this.overViewId = overViewId ;
     this.berthIds= berthIds;

    }
}
