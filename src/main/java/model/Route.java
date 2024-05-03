package model;

import org.sikuli.script.*;

import java.util.List;


public class Route extends RailwayObject{

    private String routeEntry;
    private String routeExit;
    private List<String> routeTracks;
    private List<String> conflictRoute;

    private String direction;


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
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

    public void setRouteExit(String routeExit) {
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
    private String beforeTrack;
    private String afterTrack;

    private int precedence;
    private String precedenceTrack;

    private boolean additionalRoute;
    private boolean trackDrop;

    public String getPrecedenceTrack() {
        return precedenceTrack;
    }

    public void setPrecedenceTrack(String precedenceTrack) {
        this.precedenceTrack = precedenceTrack;
    }


    public boolean hasAdditionalRoute() {
        return additionalRoute;
    }

    public void setAdditionalRoute(boolean additionalRoute) {
        this.additionalRoute = additionalRoute;
    }

    public boolean isAdditionalRoute() {
        return additionalRoute;
    }

    public boolean isTrackDrop() {
        return trackDrop;
    }

    public void setTrackDrop(boolean trackDrop) {
        this.trackDrop = trackDrop;
    }

    public Route(String id, String name, String direction,
                 String beforeTrack, String afterTrack, int precedence, String precedenceTrack,
                 String routeEntry, String routeExit, List<String> routeTracks, List<String> conflictRoute,
                 boolean additionalRoute, boolean trackDrop,
                 Interlocking interlocking, Location location,
                 Location screenCoordinate){
        super(id,name,interlocking,location,screenCoordinate);

        this.routeEntry = routeEntry ;
        this.routeExit = routeExit;
        this.routeTracks = routeTracks ;
        this.conflictRoute= conflictRoute;
        this.direction= direction;
        this.beforeTrack = beforeTrack;
        this.afterTrack = afterTrack;
        this.precedence = precedence;
        this.precedenceTrack = precedenceTrack;
        this.additionalRoute = additionalRoute;
        this.trackDrop = trackDrop;
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

}
