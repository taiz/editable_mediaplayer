package creamy.scene.media;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.MediaPlayer;

public class MediaPlayerUtil {
    public interface StatusListener {
        public void changed();
    }

    private ObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<MediaPlayer>();
    private List<StatusListener> endOfMediaListeners = new ArrayList<StatusListener>();
    private List<StatusListener> stoppedListeners = new ArrayList<StatusListener>();

    public MediaPlayerUtil(ObjectProperty<MediaPlayer> mediaPlayer) {
        this.mediaPlayer.bind(mediaPlayer);
        this.mediaPlayer.addListener(new ChangeListener<MediaPlayer>() {
            @Override
            public void changed(ObservableValue<? extends MediaPlayer> ov, MediaPlayer o, MediaPlayer n) {
                initialize();
            } 
        });
        if (mediaPlayer.get() != null) initialize();
    }

    private void initialize() {
        setEndOfMediaListenr();
        setStoppedListener();
    }

    private void setEndOfMediaListenr() {
        endOfMediaListeners.clear();
        mediaPlayer.get().setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                notifyListeners(endOfMediaListeners);
            }
        });
    }

    private void setStoppedListener() {
        stoppedListeners.clear();
        mediaPlayer.get().setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                notifyListeners(stoppedListeners);
            }
        });
    }

    protected void notifyListeners(List<StatusListener> listeners) {
        for (final StatusListener listener : listeners) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listener.changed();
                }
            });
        }
    }

    public void addEndOfMediaListener(StatusListener listener) {
        endOfMediaListeners.add(listener);
    }

    public void removeEndOfMediaListener(StatusListener listener) {
        endOfMediaListeners.remove(listener);
    }
    
    public void addStoppedListener(StatusListener listener) {
        stoppedListeners.add(listener);
    }
    
    public void removeStoppedListener(StatusListener listener) {
        stoppedListeners.remove(listener);
    }
}
