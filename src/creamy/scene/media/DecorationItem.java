package creamy.scene.media;

import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.util.Duration;

public class DecorationItem extends Decoration {
    private Node node;
    private DoubleProperty x = new SimpleDoubleProperty();
    private DoubleProperty y = new SimpleDoubleProperty();
    private DoubleProperty translateX = new SimpleDoubleProperty();
    private DoubleProperty translateY = new SimpleDoubleProperty();
    private DoubleProperty width  = new SimpleDoubleProperty();
    private DoubleProperty height = new SimpleDoubleProperty();

    public DecorationItem(Node node) {
        setNode(node);
    }

    public Node getNode() { return this.node; }

    public void setNode(Node node) {
        this.node = node;
        this.getChildren().clear();
        this.getChildren().add(node);
    }

    public double getX() { return x.get(); }

    public void setX(double x) { this.x.set(x); }

    public double getY() { return y.get(); }

    public void setY(double y) { this.y.set(y); }

    @Override
    public void adjustPosition(double width, double height) {
        translateX.set(x.get() * width);
        translateY.set(y.get() * height);
        this.width.set(width);
        this.height.set(height);
    }

    @Override
    public void show(Group sheet) {
        try {
            sheet.getChildren().add(this);
        } catch(IllegalArgumentException ex) {
            return;
        }
        setTranslateX(translateX.get());
        setTranslateY(translateY.get());
        if (startAnimation == null) return;
        if (startAnimation == StartAnimation.FADE_IN) setOpacity(0.0);
        startTransition.play();
    }

    @Override
    public void hide(final Group sheet) {
        if (endAnimation == null) {
            sheet.getChildren().remove(this);
            return;
        }
        endTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                sheet.getChildren().remove(DecorationItem.this);
            }
        });
        endTransition.play();
    }

    @Override
    public void setStartAnimation(StartAnimation type, double time) {
        if (type == StartAnimation.FADE_IN) {
            startAnimation = StartAnimation.FADE_IN;
            startTransition = FadeTransitionBuilder.create()
                    .node(this).duration(Duration.seconds(time))
                    .fromValue(0.0)
                    .toValue(1.0)
                    .build();
        } else {
            TranslateTransition tr = TranslateTransitionBuilder.create()
                    .node(this).duration(Duration.seconds(time))
                    .interpolator(Interpolator.EASE_BOTH)
                    .build();
            switch(type) {
                case TOP_IN:
                    startAnimation = StartAnimation.TOP_IN;
                    tr.fromXProperty().bind(translateX);
                    tr.setFromY(0);
                    tr.toXProperty().bind(translateX);
                    tr.toYProperty().bind(translateY);
                    break;
                case LEFT_IN:
                    startAnimation = StartAnimation.LEFT_IN;
                    tr.setFromX(0);
                    tr.fromYProperty().bind(translateY);
                    tr.toXProperty().bind(translateX);
                    tr.toYProperty().bind(translateY);
                    break;
                case BOTTOM_IN:
                    startAnimation = StartAnimation.BOTTOM_IN;
                    tr.fromXProperty().bind(translateX);
                    tr.fromYProperty().bind(height);
                    tr.toXProperty().bind(translateX);
                    tr.toYProperty().bind(translateY);
                    break;
                case RIGHT_IN:
                    startAnimation = StartAnimation.RIGHT_IN;
                    tr.fromXProperty().bind(width);
                    tr.fromYProperty().bind(translateY);
                    tr.toXProperty().bind(translateX);
                    tr.toYProperty().bind(translateY);
                    break;
            }
            startTransition = tr;
        }
    }

    @Override
    public void unsetStartAnimation() {
        startAnimation = null;
        startTransition = null;
    }

    @Override
    public void setEndAnimation(EndAnimation type, double time) {
        if (type == EndAnimation.FADE_OUT) {
            endAnimation = EndAnimation.FADE_OUT;
            endTransition = FadeTransitionBuilder.create()
                    .node(this).duration(Duration.seconds(time))
                    .toValue(0.0)
                    .build();
        } else {
            TranslateTransition tr = TranslateTransitionBuilder.create()
                    .node(this).duration(Duration.seconds(time))
                    .interpolator(Interpolator.EASE_BOTH)
                    .build();
            switch(type) {
                case TOP_OUT:
                    endAnimation = EndAnimation.TOP_OUT;
                    tr.toXProperty().bind(translateX);
                    tr.setToY(0);
                    break;
                case LEFT_OUT:
                    endAnimation = EndAnimation.LEFT_OUT;
                    tr.setToX(0);
                    tr.toYProperty().bind(translateY);
                    break;
                case BOTTOM_OUT:
                    endAnimation = EndAnimation.BOTTOM_OUT;
                    tr.toXProperty().bind(translateX);
                    tr.toYProperty().bind(height);
                    break;
                case RIGHT_OUT:
                    endAnimation = EndAnimation.RIGHT_OUT;
                    tr.toXProperty().bind(width);
                    tr.toYProperty().bind(translateY);
                    break;
            }
            endTransition = tr;
        }
    }

    @Override
    public void unsetEndAnimation() {
        endAnimation = null;
        endTransition = null;
    }
}
