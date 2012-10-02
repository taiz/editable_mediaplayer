package creamy.scene.media;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;

public class DecorationGroup extends Decoration {
    private List<Decoration> decorations = new ArrayList<Decoration>();

    public void add(Decoration deco) {
        decorations.add(deco);
    }

    public void remove(Decoration deco) {
        decorations.remove(deco);
    }

    @Override
    public void adjustPosition(double width, double height) {
        for (Decoration deco : decorations) {
            deco.adjustPosition(width, height);
        }
    }

    @Override
    public void show(Group sheet) {
        sheet.getChildren().add(this);
        for (final Decoration deco : decorations) {
            Timeline timeline = TimelineBuilder.create()
                .keyFrames(
                    new KeyFrame(deco.getStartTime(),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            deco.show(DecorationGroup.this);
                        }
                    })
                )
                .build();
            timeline.play();      
        }
    }

    @Override
    public void hide(final Group sheet) {
        for (final Decoration deco : decorations) {
            Timeline timeline = TimelineBuilder.create()
                .keyFrames(
                    new KeyFrame(deco.getStartTime(),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            deco.hide(DecorationGroup.this);
                            if (deco == decorations.get(decorations.size() - 1))
                                sheet.getChildren().remove(DecorationGroup.this);
                        }
                    })
                )
                .build();
            timeline.play();      
        }
    }

    @Override
    public void setStartAnimation(StartAnimation type, double time) {
        for (Decoration deco : decorations) {
            deco.setStartAnimation(type, time);
        }
    }

    @Override
    public void setEndAnimation(EndAnimation type, double time) {
        for (Decoration deco : decorations) {
            deco.setEndAnimation(type, time);
        }
    }
}
