/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creamy.scene.media;

/**
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import javafx.scene.media.MediaMarkerEvent;
 
/**
 * A media player with controls for play, pause, stop, seek, and volume. 
 *
 * @see javafx.scene.media.MediaPlayer
 * @see javafx.scene.media.Media
 */
public class Main extends Application {
    private static final String MEDIA_URL = "http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv";
    //"http://download.oracle.com/otndocs/javafx/JavaRap_ProRes_H264_768kbit_Widescreen.mp4";
    private MediaPlayer mediaPlayer;
 
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        Media media;
        mediaPlayer = new MediaPlayer(media = new Media(MEDIA_URL));

        mediaPlayer.setAutoPlay(true);

        playerPane = new PlayerPane(mediaPlayer);
        playerPane.setMinSize(300, 200);  
        //playerPane.setPrefSize(480, 360);
        playerPane.setPrefSize(1024, 600);
        playerPane.setMaxSize(1024, 768);
        root.getStylesheets().add("creamy/scene/media/PlayerPane.css");

        root.getChildren().add(playerPane);
    }
 
    PlayerPane playerPane;
    
    public void play() {
        Status status = mediaPlayer.getStatus();
        if (status == Status.UNKNOWN || status == Status.HALTED) {
            return;
        }
        if (status == Status.PAUSED || status == Status.STOPPED || status == Status.READY) {
            mediaPlayer.play();
        }
    }
 
    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
    }
    
    public static void main(String[] args) { launch(args); }
}