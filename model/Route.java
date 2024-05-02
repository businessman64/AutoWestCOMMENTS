package model;

import org.sikuli.script.*;

import java.util.List;


public class Route extends RailwayObject{


    private String beforeTrack;
    private String afterTrack;

    private int precedence;
    private String precedenceTrack;

    private boolean additionalRoute;
    private boolean trackDrop;

    public String getGroundFrame() {
        return groundFrame;
    }

    public void setGroundFrame(String groundFrame) {
        this.groundFrame = groundFrame;
    }

    public String getControlLine() {
        return controlLine;
    }

    public void setControlLine(String controlLine) {
        this.controlLine = controlLine;
    }

    public String getExtractControlledRoute() {
        return extractControlledRoute;
    }

    public void setExtractControlledRoute(String extractControlledRoute) {
        this.extractControlledRoute = extractControlledRoute;
    }

    private String groundFrame;
    private String controlLine;
    private String extractControlledRoute;

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

    private RouteSetInfo setInfo;
    private RouteInfo routeInfo;

    public RouteSetInfo getSetInfo() {
        return setInfo;
    }

    public void setSetInfo(RouteSetInfo setInfo) {
        this.setInfo = setInfo;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(RouteInfo routeInfo) {
        this.routeInfo = routeInfo;
    }


    public Route(String id, String name, String beforeTrack, String afterTrack,RouteInfo routeInfo,RouteSetInfo setInfo,
                   String precedenceTrack,String groundFrame, String controlLine, String extractControlledRoute,
                 boolean additionalRoute, boolean trackDrop,

                 Interlocking interlocking, Location location,
                 Location screenCoordinate)

    {
        super(id,name,interlocking,location,screenCoordinate);

        this.groundFrame= groundFrame;
        this.controlLine=controlLine;
        this.extractControlledRoute =extractControlledRoute;
        this.beforeTrack = beforeTrack;
        this.afterTrack = afterTrack;
        this.additionalRoute = additionalRoute;

        this.setInfo= setInfo;
        this.routeInfo= routeInfo;
        this.trackDrop = trackDrop;
       this.precedenceTrack = precedenceTrack;

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
