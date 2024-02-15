package model;

public class RsmMessage {
    private boolean vitalBlocked;

    private boolean disregarded;
    private boolean isCentered;
    private boolean isReversed;

    private boolean isNormal;

    public boolean isVitalBlocked() {
        return vitalBlocked;
    }

    public void setVitalBlocked(boolean vitalBlocked) {
        this.vitalBlocked = vitalBlocked;
    }

    public boolean isDisregarded() {
        return disregarded;
    }

    public void setDisregarded(boolean disregarded) {
        this.disregarded = disregarded;
    }

    public boolean isCommsFailed() {
        return commsFailed;
    }

    public void setCommsFailed(boolean commsFailed) {
        this.commsFailed = commsFailed;
    }

    private boolean commsFailed;

    @Override
    public String toString(){
        return String.valueOf(this.commsFailed)+ this.vitalBlocked + this.disregarded;
    }

    public boolean isCentered() {
        return this.isCentered;
    }

    public boolean isReversed() {
        return this.isReversed;
    }
    public boolean isNormal(){
        return this.isNormal;
    }

    public void setNormalState(boolean state) {
        this.isNormal = state;
    }

    public void setCentreState(boolean state) {
        this.isCentered = state;
    }

    public void setReverseState(boolean state) {
        this.isReversed = state;
    }
}
