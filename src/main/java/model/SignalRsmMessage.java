package model;

public class SignalRsmMessage extends RsmMessage{

    private boolean isFleetingOn;

    private boolean isArsOn;

    private boolean isRouteSet;

    public boolean isRouteSet() {
        return isRouteSet;
    }

    public void setRouteSet(boolean routeSet) {
        isRouteSet = routeSet;
    }

    public boolean isRouteSetInRear() {
        return isRouteSetInRear;
    }

    public void setRouteSetInRear(boolean routeSetInRear) {
        isRouteSetInRear = routeSetInRear;
    }

    private boolean isRouteSetInRear;

    public boolean isFleetingOn() {
        return isFleetingOn;
    }

    public void setFleetingOn(boolean fleetingOn) {
        isFleetingOn = fleetingOn;
    }

    public boolean isArsOn() {
        return isArsOn;
    }

    public void setArsOn(boolean arsOn) {
        isArsOn = arsOn;
    }

    public boolean isCommsFailed() {
        return commsFailed;
    }

    public void setCommsFailed(boolean commsFailed) {
        this.commsFailed = commsFailed;
    }

    private boolean commsFailed;

}
