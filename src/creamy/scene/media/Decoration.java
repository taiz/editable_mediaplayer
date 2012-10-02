package creamy.scene.media;

import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.util.Duration;

public abstract class Decoration extends Group {
    public enum StartAnimation {
        FADE_IN, TOP_IN, LEFT_IN, BOTTOM_IN, RIGHT_IN
    }
    public enum EndAnimation {
        FADE_OUT, TOP_OUT, LEFT_OUT, BOTTOM_OUT, RIGHT_OUT
    }

    protected Integer did;
    protected Duration startTime;
    protected Duration displayTime;
    protected StartAnimation startAnimation;
    protected Transition startTransition;
    protected EndAnimation endAnimation;
    protected Transition endTransition;
    
    public Integer getDid() { return did; }

    public void setDid(Integer did) { this.did = did; }

    public Duration getStartTime() { return startTime; }

    public void setStartTime(Duration startTime) { this.startTime = startTime; }

    public Duration getDisplayTime() { return displayTime; }

    public void setDisplayTime(Duration displayTime) { this.displayTime = displayTime; }

    public Duration getEndTime() {
        if (displayTime == Duration.INDEFINITE)
            return Duration.INDEFINITE;
        else
            return startTime.add(displayTime);
    }

    public abstract void adjustPosition(double width, double height);

    public abstract void show(Group sheet);

    public abstract void hide(Group sheet);

    public abstract void setStartAnimation(StartAnimation type, double time);

    public abstract void setEndAnimation(EndAnimation type, double time);
    
    protected boolean isAdded(Group sheet) {
        for (Node node : sheet.getChildren()) {
            if (node == this) return true;
            System.out.println("added");
        }
        return false;
    }
}
