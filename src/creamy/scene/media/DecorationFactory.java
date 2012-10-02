package creamy.scene.media;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class DecorationFactory {
    private Integer currentId = 0;

    public DecorationItem createItem(Node node) {
        DecorationItem deco = new DecorationItem(node);
        deco.setDid(currentId++);
        return deco;
    }

    public DecorationGroup createGroup() {
        DecorationGroup deco = new DecorationGroup();
        deco.setDid(currentId++);
        return deco;
    }

    public static class Point extends Circle {
        private static final double RADIUS = 5.0;
        
        public Point() {
            setFill(Color.WHITE);
            setRadius(RADIUS);
            setEffect(
                DropShadowBuilder.create()
                    .offsetX(1.0)
                    .offsetY(1.0)
                    .color(Color.CORAL)
                    .build()
            );
            setCache(true);
        }
    }

    public static class InputText extends TextField {
        public InputText() {
            setPrefColumnCount(1);
            setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    setPrefColumnCount(getText().length());
                }
            });
        }
    }

    public static class DisplayText extends Text {
        public DisplayText(String text) {
            setText(text);
            setFill(Color.WHITE);
            setFont(Font.font("Times New Roman", 30));
            setEffect(
                DropShadowBuilder.create()
                    .offsetX(1.0)
                    .offsetY(1.0)
                    .color(Color.CORAL)
                    .build()
            );
            setCache(true);
        }
    }
    
}
