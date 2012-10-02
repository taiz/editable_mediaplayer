package creamy.scene.media;

import creamy.scene.media.DecorationFactory.DisplayText;
import creamy.scene.media.DecorationFactory.InputText;
import creamy.scene.media.DecorationFactory.Point;
import creamy.scene.media.MediaPlayerUtil.StatusListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Decorator extends Group {
    public enum EditMode {PAINT, TEXT}

    public interface MarkerHandler {
        public void handle();
    }

    private final MediaView mediaView;
    private final ObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<MediaPlayer>();

    public Decorator(MediaView mediaView, ObjectProperty<MediaPlayer> mediaPlayer) {
        getChildren().add(this.mediaView = mediaView);
        this.mediaPlayer.bind(mediaPlayer);
        this.mediaPlayer.addListener(new ChangeListener<MediaPlayer>() {
            @Override
            public void changed(ObservableValue ov, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
                initialize(newPlayer);
            }
        });
        initialize();
    }

    private MediaPlayerUtil mediaPlayerUtil;
    private DecorationFactory factory;

    public void initialize() {
        factory = new DecorationFactory();
        clearDecorations();
        markerHandlers.clear();
    }

    private void initialize(MediaPlayer mediaPlayer) {
        initialize();
        //mediaPlayerUtil = new MediaPlayerUtil(mediaPlayer);
        mediaPlayer.setOnMarker(new EventHandler<MediaMarkerEvent>() {
            @Override
            public void handle(MediaMarkerEvent event) {
                operateDecoration(event.getMarker().getKey());
            }
        });
    }
    
    private void clearDecorations() {
        List<Decoration> decs = new ArrayList<Decoration>();
        for (Node node : getChildren()) {
            if (node instanceof Decoration)
                decs.add((Decoration)node);
        }
        for (Decoration deco : decs)
            getChildren().remove(deco);
    }

    private EventHandler<MouseEvent> editFilter = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            event.consume();
        }  
    };

    private BooleanProperty editable = new SimpleBooleanProperty(false);
    {
        editable.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                if (newValue)
                    addEventFilter(MouseEvent.ANY, editFilter);
                else
                    removeEventFilter(MouseEvent.ANY, editFilter);
            }
        });
    }

    public void setEditable(boolean enable) { editable.set(enable); }

    public void startRec() {setEditable(true); }

    public void endRec() { setEditable(false); }

    private final Map<EventType<MouseEvent>, EventHandler<MouseEvent>> paintHandlers;
    {
        paintHandlers = new HashMap<EventType<MouseEvent>, EventHandler<MouseEvent>>();
        // MOUSE_PRESSED
        paintHandlers.put(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                paintGroup = createDecorationGroup();
                registerDecoratoin(paintGroup);
                showDecoration(paintGroup);
                setPaintStartTime();
                DecorationItem item = createDecorationItem(new Point(), event.getX(), event.getY(), Duration.millis(0.1));
                item.adjustPosition(mediaView.getFitWidth(), mediaView.getFitHeight());
                item.show(paintGroup);
            }
        });
        // MOUSE_DRAGGED
        paintHandlers.put(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Duration elapseTime = getPaintElapseTime();
                DecorationItem item = createDecorationItem(new Point(), event.getX(), event.getY(), elapseTime);
                item.adjustPosition(mediaView.getFitWidth(), mediaView.getFitHeight());
                item.show(paintGroup);
            }
        });
        // MOUSE_RELEASED
        paintHandlers.put(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // group.setSubMenu(sheet)
                paintGroup.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        for (Node item : paintGroup.getChildren()) {
                            if (!(item instanceof DecorationItem)) continue;
                            Node node = ((DecorationItem)item).getNode();
                            if (!(node instanceof Point)) continue;
                            ((Point)node).setFill(Color.BLUE);
                        }
                    }
                });
                paintGroup.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        for (Node item : paintGroup.getChildren()) {
                            if (!(item instanceof DecorationItem)) continue;
                            Node node = ((DecorationItem)item).getNode();
                            if (!(node instanceof Point)) continue;
                            ((Point)node).setFill(Color.WHITE);
                        }
                    }
                });
            }
        });
    }
                
    private final Map<EventType<MouseEvent>, EventHandler<MouseEvent>> textHandlers;
    {
        textHandlers = new HashMap<EventType<MouseEvent>, EventHandler<MouseEvent>>();
        // MOUSE_PRESSED
        textHandlers.put(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                final InputText inputText = new InputText();
                final DecorationItem deco = createDecorationItem(inputText, event.getX(), event.getY());
                registerDecoratoin(deco);
                showDecoration(deco);
                inputText.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        deco.setNode(new DisplayText(inputText.getText()));
                        //deco.setSubMenu(sheet)              
                    }
                });
                inputText.focusedProperty().addListener(new ChangeListener<Boolean> () {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                        if (newValue) return;
                        deco.setNode(new DisplayText(inputText.getText()));
                        //deco.setSubMenu(sheet)   
                    }
                });
            }
        });
    }
    
    private Map<EventType<MouseEvent>, EventHandler<MouseEvent>> currentHandlers;

    public void setEditMode(EditMode mode) {
        switch(mode) {
            case PAINT:
                setEditHandlers(paintHandlers);
                break;
            case TEXT:
                setEditHandlers(textHandlers);
                break;
        }
    }

    private void setEditHandlers(Map<EventType<MouseEvent>, EventHandler<MouseEvent>> handlers) {
        clearEditHandlers();
        for (Entry<EventType<MouseEvent>, EventHandler<MouseEvent>> entry : handlers.entrySet()) {
            addEventHandler(entry.getKey(), entry.getValue());
        }
        currentHandlers = handlers;
    }

    private void clearEditHandlers() {
        if (currentHandlers == null) return;
        for (Entry<EventType<MouseEvent>, EventHandler<MouseEvent>> entry : currentHandlers.entrySet()) {
            removeEventHandler(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, MarkerHandler> markerHandlers = new HashMap<String, MarkerHandler>();
    private Map<Decoration, StatusListener> endListeners = new HashMap<Decoration, StatusListener>();
    private DecorationGroup paintGroup;
    private long paintTime;

    private DecorationGroup createDecorationGroup() {
        DecorationGroup deco = factory.createGroup();
        deco.setStartTime(mediaPlayer.get().getCurrentTime());
        deco.setDisplayTime(Duration.INDEFINITE);
        return deco;
    }

    private DecorationItem createDecorationItem(Node node, double x, double y) {
        return createDecorationItem(node, x, y, mediaPlayer.get().getCurrentTime());
    }

    private DecorationItem createDecorationItem(Node node, double x, double y, Duration startTime) {
        DecorationItem deco = factory.createItem(node);
        deco.setX(x / mediaView.getFitWidth());
        deco.setY(y / mediaView.getFitHeight());
        deco.setStartTime(startTime);
        deco.setDisplayTime(Duration.INDEFINITE);
        return deco;
    }

    private void registerDecoratoin(Decoration deco) {
        registerStart(deco);
        registerEnd(deco);
    }

    private void registerStart(Decoration deco) {
        String key = getStartKey(deco);
        registerMarkerAndHandler(key, deco.getStartTime(), createStartMarkerHandler(deco));
    }

    private void registerEnd(final Decoration deco) {
        if (deco.getEndTime() == Duration.INDEFINITE) {
            StatusListener listenr = new StatusListener() {
                @Override
                public void changed() {
                    deco.hide(Decorator.this);
                }
            };
            mediaPlayerUtil.addEndOfMediaListener(listenr);
            mediaPlayerUtil.addStoppedListener(listenr);
            endListeners.put(deco, listenr);
        } else {
            String key = getEndKey(deco);
            registerMarkerAndHandler(key, deco.getStartTime(), createEndMarkerHandler(deco));
        }
    }

    private void registerMarkerAndHandler(String key, Duration time, MarkerHandler handler) {
        mediaPlayer.get().getMedia().getMarkers().put(key, time);
        markerHandlers.put(key, handler);
    }

    private MarkerHandler createStartMarkerHandler(final Decoration deco) {
        return new MarkerHandler() {
            @Override
            public void handle() {
                deco.adjustPosition(mediaView.getFitWidth(), mediaView.getFitHeight());
                deco.show(Decorator.this);
            }
        };
    }

    private MarkerHandler createEndMarkerHandler(final Decoration deco) {
        return new MarkerHandler() {
            @Override
            public void handle() {
                deco.hide(Decorator.this);
            }
        };
    }

    private void unregisterDecoration(Decoration deco) {
        unregisterStart(deco);
        unregisterEnd(deco);
    }

    private void unregisterStart(Decoration deco) {
        String key = getStartKey(deco);
        unregisterMarkerAndHandler(key);
    }

    private void unregisterEnd(Decoration deco) {
        String key = getEndKey(deco);
        unregisterMarkerAndHandler(key);
        StatusListener listener = endListeners.remove(deco);
        mediaPlayerUtil.removeStoppedListener(listener);
        mediaPlayerUtil.removeEndOfMediaListener(listener);
    }

    private void unregisterMarkerAndHandler(String key) {
        mediaPlayer.get().getMedia().getMarkers().remove(key);
        markerHandlers.remove(key);
    }

    private void showDecoration(Decoration deco) {
        operateDecoration(getStartKey(deco));
    }

    private void operateDecoration(String key) {
        MarkerHandler handler = markerHandlers.get(key);
        handler.handle();
    }

    private void setPaintStartTime() {
        paintTime = Calendar.getInstance().getTimeInMillis();
    }

    private Duration getPaintElapseTime() {
        long elapseTime = Calendar.getInstance().getTimeInMillis() - paintTime;
        return Duration.millis(elapseTime);
    }

    private String getStartKey(Decoration deco) {
        return getKey(deco, "S");
    }

    private String getEndKey(Decoration deco) {
        return getKey(deco, "E");
    }

    private String getKey(Decoration deco, String postFix) {
        return deco.getDid().toString() + postFix;
    }
}
