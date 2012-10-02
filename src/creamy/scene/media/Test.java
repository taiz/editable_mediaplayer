/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creamy.scene.media;

import creamy.scene.media.DecorationFactory.DisplayText;
import creamy.scene.media.DecorationFactory.InputText;
import creamy.scene.media.DecorationFactory.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author miyabetaiji
 */
public class Test extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        
        VBox root = new VBox();
        root.setPadding(new Insets(30,30,30,30));
        root.setSpacing(20);
        
        Rectangle rect = RectangleBuilder.create()
                .width(50)
                .height(50)
                .build();
        
        final DecorationItem deco = new DecorationItem(new Point());
        deco.setX(300.0 / 800.0);
        deco.setY(300.0 / 600.0);
        final Group sheet = new Group();
        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: AQUA;");
        pane.setPrefWidth(800);
        pane.setPrefHeight(600);
        sheet.getChildren().add(pane);
        
        root.getChildren().add(sheet);
        
        final DecorationGroup group = new DecorationGroup();
        //group.adjustPosition(800, 600);
        //group.show(sheet);
        
        group.add(deco);
        deco.setStartAnimation(Decoration.StartAnimation.RIGHT_IN, 2);
        //deco.setStartTime(Duration.seconds(4));
        deco.setStartTime(Duration.millis(0.1));
        //deco.adjustPosition(800, 600);
        //deco.show(group);
        group.adjustPosition(800, 600);
        group.show(sheet);
        
        group.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (Node item : group.getChildren()) {
                    if (!(item instanceof DecorationItem)) continue;
                    Node node = ((DecorationItem)item).getNode();
                    if (!(node instanceof Point)) continue;
                    ((Point)node).setFill(Color.BLUE);
                }
            }
        });
        group.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (Node item : group.getChildren()) {
                    if (!(item instanceof DecorationItem)) continue;
                    Node node = ((DecorationItem)item).getNode();
                    if (!(node instanceof Point)) continue;
                    ((Point)node).setFill(Color.WHITE);
                }
            }
        });
        /*
        deco.setStartAnimation(Decoration.StartAnimation.RIGHT_IN, 3);
        deco.adjustPosition(800, 600);
        deco.show(sheet);
        */
        
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
       
        //deco.setEndAnimation(Decoration.EndAnimation.TOP_OUT, 2.0);
        
        Button btn = new Button("hide");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                group.hide(sheet);
            }
        });
        root.getChildren().add(btn);
        
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
