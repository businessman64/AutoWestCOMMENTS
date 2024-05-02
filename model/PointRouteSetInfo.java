package model;

public class PointRouteSetInfo {

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUnlessPseudoReverse() {
        return unlessPseudoReverse;
    }

    public void setUnlessPseudoReverse(String unlessPseudoReverse) {
        this.unlessPseudoReverse = unlessPseudoReverse;
    }

    private String pointId;
    private String state;
    private String unlessPseudoReverse;

    public PointRouteSetInfo(String pointId,String state, String unlessPseudoReverse){

        this.pointId=pointId;
        this.state= state;
        this.unlessPseudoReverse=unlessPseudoReverse;

    }

}
