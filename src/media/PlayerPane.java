package media;

import media.MediaPlayerUtil.StatusListener;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ParallelTransitionBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class PlayerPane extends BorderPane {
    private ObjectProperty<MediaPlayer> mp = new SimpleObjectProperty<>();
    private MediaView mediaView;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private HBox mediaTopBar;
    private HBox mediaBottomBar;
    private ParallelTransition transition = null;
    
    private Decorator decorator;

    @Override protected void layoutChildren() {
        if (mediaView != null && getBottom() != null) {
            mediaView.setFitWidth(getWidth());
            mediaView.setFitHeight(getHeight() - getBottom().prefHeight(-1));
        }
        super.layoutChildren();
        if (mediaView != null) {
            mediaView.setTranslateX((((Pane)getCenter()).getWidth() - mediaView.prefWidth(-1)) / 2);
            mediaView.setTranslateY((((Pane)getCenter()).getHeight() - mediaView.prefHeight(-1)) / 2);
        }
    }

    @Override protected double computeMinWidth(double height) {
        return mediaBottomBar.prefWidth(-1);
    }

    @Override protected double computeMinHeight(double width) {
        return 200;
    }

    @Override protected double computePrefWidth(double height) {
        return Math.max(mp.get().getMedia().getWidth(), mediaBottomBar.prefWidth(height));
    }

    @Override protected double computePrefHeight(double width) {
        return mp.get().getMedia().getHeight() + mediaBottomBar.prefHeight(width);
    }

    @Override protected double computeMaxWidth(double height) { return Double.MAX_VALUE; }

    @Override protected double computeMaxHeight(double width) { return Double.MAX_VALUE; }

    public PlayerPane(final MediaPlayer mp) {
        this.mp.set(mp);
        MediaPlayerUtil util = new MediaPlayerUtil(this.mp);

        setId("player-pane");

        mediaView = new MediaView(mp);
        decorator = new Decorator(mediaView, new SimpleObjectProperty<>(util));
        decorator.startRec();
        //decorator.setEditMode(Decorator.EditMode.PAINT);
        
        Pane mvPane = new Pane() { };
        mvPane.setId("media-pane");
        //mvPane.getChildren().add(mediaView);
        mvPane.getChildren().add(decorator);
        
        setCenter(mvPane);

        mediaTopBar = HBoxBuilder.create()
                .padding(new Insets(5, 10, 5, 10))
                .alignment(Pos.CENTER)
                .opacity(0)
                .build();
        BorderPane.setAlignment(mediaTopBar, Pos.CENTER);

        mediaBottomBar = HBoxBuilder.create()
                .padding(new Insets(5, 10, 5, 10))
                .alignment(Pos.CENTER)
                .opacity(0)
                .build();
        BorderPane.setAlignment(mediaBottomBar, Pos.CENTER);

        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent t) {
                if (transition != null) transition.stop();
                transition = ParallelTransitionBuilder.create()
                    .children(
                        FadeTransitionBuilder.create()
                            .node(mediaTopBar)
                            .toValue(1)
                            .duration(Duration.millis(200))
                            .interpolator(Interpolator.EASE_OUT)
                            .build(),
                        FadeTransitionBuilder.create()
                            .node(mediaBottomBar)
                            .toValue(1)
                            .duration(Duration.millis(200))
                            .interpolator(Interpolator.EASE_OUT)
                            .build()
                    )
                    .build();
                transition.play();
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent t) {
                if (transition != null) transition.stop();
                transition = ParallelTransitionBuilder.create()
                    .children(
                        FadeTransitionBuilder.create()
                            .node(mediaTopBar)
                            .toValue(0)
                            .duration(Duration.millis(800))
                            .interpolator(Interpolator.EASE_OUT)
                            .build(),
                        FadeTransitionBuilder.create()
                            .node(mediaBottomBar)
                            .toValue(0)
                            .duration(Duration.millis(800))
                            .interpolator(Interpolator.EASE_OUT)
                            .build()
                    )
                    .build();
                transition.play();
            }
        });

        mp.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                updateValues();
            }
        });
        /*
        mp.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mp.pause();
                    stopRequested = false;
                } 
            }
        });
        mp.setOnReady(new Runnable() {
            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues();
            }
        });
        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
        });
        */
        util.addPlayingListener(new StatusListener() {
            @Override
            public void changed() {
                if (stopRequested) {
                    mp.pause();
                    stopRequested = false;
                }
            }
        });
        util.addReadyListener(new StatusListener() {
            @Override
            public void changed() {
                duration = mp.getMedia().getDuration();
                updateValues();
            }
        });
        util.addEndOfMediaListener(new StatusListener() {
            @Override
            public void changed() {
                if (!repeat) {
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
        });
        mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);

        // Time label
        Label timeLabel = LabelBuilder.create()
                .text("Time")
                .minWidth(Control.USE_PREF_SIZE)
                .textFill(Color.WHITE)
                .build();
        mediaTopBar.getChildren().add(timeLabel);

        // Time slider
        timeSlider = SliderBuilder.create()
                .id("media-slider")
                .minWidth(240)
                .maxWidth(Double.MAX_VALUE)
                .build();
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    if (duration != null) {
                        mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                    }
                    updateValues();

                }
            }
        });
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        mediaTopBar.getChildren().add(timeSlider);

        // Play label
        playTime = LabelBuilder.create()
                .prefWidth(130)
                .minWidth(50)
                .textFill(Color.WHITE)
                .build();
        mediaTopBar.getChildren().add(playTime);

        // Volume label
        Label volumeLabel = LabelBuilder.create()
                .text("Vol")
                .textFill(Color.WHITE)
                .minWidth(Control.USE_PREF_SIZE)
                .build();
        mediaTopBar.getChildren().add(volumeLabel);

        // Volume slider
        volumeSlider = SliderBuilder.create()
                .id("media-slider")
                .prefWidth(120)
                .maxWidth(Region.USE_PREF_SIZE)
                .minWidth(30)
                .build();
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
            }
        });
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (volumeSlider.isValueChanging()) {
                    mp.setVolume(volumeSlider.getValue() / 100.0);
                }
            }
        });
        mediaTopBar.getChildren().add(volumeSlider);

        setTop(mediaTopBar);

        final EventHandler<ActionEvent> backAction = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                mp.seek(Duration.ZERO);
            }
        };
        final EventHandler<ActionEvent> stopAction = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                mp.stop();
            }
        };
        final EventHandler<ActionEvent> playAction = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                mp.play();
            }
        };
        final EventHandler<ActionEvent> pauseAction = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                mp.pause();
            }
        };
        final EventHandler<ActionEvent> forwardAction = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Duration currentTime = mp.getCurrentTime();
                mp.seek(Duration.seconds(currentTime.toSeconds() + 5.0));
            }
        };

        mediaBottomBar = HBoxBuilder.create()
                .id("bottom")
                .spacing(0)
                .alignment(Pos.CENTER)
                .children(
                    ButtonBuilder.create()
                        .id("back-button")
                        .text("Back")
                        .onAction(backAction)
                        .build(),
                    ButtonBuilder.create()
                        .id("stop-button")
                        .text("Stop")
                        .onAction(stopAction)
                        .build(),
                    ButtonBuilder.create()
                        .id("play-button")
                        .text("Play")
                        .onAction(playAction)
                        .build(),
                    ButtonBuilder.create()
                        .id("pause-button")
                        .text("Pause")
                        .onAction(pauseAction)
                        .build(),
                    ButtonBuilder.create()
                        .id("forward-button")
                        .text("Forward")
                        .onAction(forwardAction)
                        .build()
                 )
                .build();

        setBottom(VBoxBuilder.create()
                .alignment(Pos.CENTER)
                .spacing(10)
                .children(
                    mediaBottomBar,
                    buildEditButton()
                )
                .build()
            );
    }

    protected void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mp.get().getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                    }
                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mp.get().getVolume() * 100));
                    }
                }
            });
        }
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int)Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d",
                                     elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                                     elapsedMinutes, elapsedSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                                     elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                                     elapsedMinutes, elapsedSeconds);
            }
        }
    }
    
    private HBox buildEditButton() {
        final ToggleGroup group = new ToggleGroup();
        
        final ToggleButton paint = ToggleButtonBuilder.create()
                .toggleGroup(group)
                .text("●:Paint")
                .build();
        final ToggleButton text = ToggleButtonBuilder.create()
                .toggleGroup(group)
                .text("Text")
                .build();
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
                if (newToggle == null) {
                    decorator.setEditable(false);
                } else {
                    decorator.setEditable(true);
                    if (newToggle == paint) {
                        decorator.setEditMode(Decorator.EditMode.PAINT);
                    } else if (newToggle == text) {
                        decorator.setEditMode(Decorator.EditMode.TEXT);
                    }
                }
            }
        });
        
        HBox editButtons = HBoxBuilder.create()
                .spacing(0)
                .children(
                    paint,
                    text
                )
                .alignment(Pos.CENTER)
                .build();
        return editButtons;
    }
}
