package model;

import org.sikuli.script.Location;

import java.util.List;

public class RouteInfo {
    private String routeEntry;
    private String routeExit;
    private List<String> routeTracks;
    private List<String> conflictRoute;
    private String routeId;

    private String direction;

    private String beforeTrack;
    private String afterTrack;
    private int precedence;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getBeforeTrack() {
        return beforeTrack;
    }

    public void setBeforeTrack(String beforeTrack) {
        this.beforeTrack = beforeTrack;
    }

    public String getAfterTrack() {
        return afterTrack;
    }

    public void setAfterTrack(String afterTrack) {
        this.afterTrack = afterTrack;
    }

    public int getPrecedence() {
        return precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public RouteInfo(String routeId, String direction, int precedence, String routeEntry,
                     String routeExit, String beforeTrack, String afterTrack,
                     List<String> routeTracks, List<String> conflictRoute) {
        this.routeId = routeId;
        this.routeEntry = routeEntry ;
        this.routeExit = routeExit;
        this.routeTracks = routeTracks ;
        this.conflictRoute= conflictRoute;
        this.direction= direction;
        this.beforeTrack= beforeTrack;
        this.afterTrack= afterTrack;
        this.precedence = precedence;
    }

    public String getRouteEntry() {
        return routeEntry;
    }

    public void setRouteEntry(String routeEntry) {
        this.routeEntry = routeEntry;
    }

    public String getRouteExit() {
        return routeExit;
    }

    public void setRouteExit(String exit) {
        this.routeExit = routeExit;
    }

    public List<String> getRouteTracks() {
        return routeTracks;
    }

    public void setRouteTracks(List<String> routeTracks) {
        this.routeTracks = routeTracks;
    }
    public List<String> getConflictRoute() {
        return conflictRoute;
    }

    public void setConflictRoute(List<String> conflictRoute) {
        this.conflictRoute = conflictRoute;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
