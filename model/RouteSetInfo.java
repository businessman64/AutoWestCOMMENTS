package model;

import java.util.List;

public class RouteSetInfo {

        private String routeId;
        private String region;

    private String routeType;

    private List<Point> points;

    private String frontContactObjects;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getFrontContactObjects() {
        return frontContactObjects;
    }

    public void setFrontContactObjects(String frontContactObjects) {
        this.frontContactObjects = frontContactObjects;
    }

    public String getBackContactObjects() {
        return backContactObjects;
    }

    public void setBackContactObjects(String backContactObjects) {
        this.backContactObjects = backContactObjects;
    }

    private String backContactObjects;

    public RouteSetInfo(String routeId, String region, String routeType, List<Point> points,
                     String frontContactObjects, String backContactObjects) {
        this.routeId = routeId;
        this.region = region ;
        this.routeType = routeType;
        this.points = points ;
        this.frontContactObjects= frontContactObjects;
        this.backContactObjects= backContactObjects;

    }
}
