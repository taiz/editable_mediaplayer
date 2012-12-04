package media;

import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceBoxBuilder;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
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

    public void setStartTime(Duration startTime) {this.startTime = startTime; }

    public Duration getDisplayTime() { return displayTime; }

    public void setDisplayTime(Duration displayTime) {
        this.displayTime = displayTime;
        if (decorator == null) return;
        decorator.unregisterDecoration(this);
        decorator.registerDecoratoin(this);
    }

    public Duration getEndTime() {
        if (displayTime == Duration.INDEFINITE)
            return Duration.INDEFINITE;
        else
            return startTime.add(displayTime);
    }

    public abstract void adjustPosition(double width, double height);

    public abstract void show(Group sheet);

    public abstract void hide(Group sheet);
    
    public void hideImmediately(Group sheet) {
        sheet.getChildren().remove(this);
    }

    public abstract void setStartAnimation(StartAnimation type, double time);

    public abstract void unsetStartAnimation();
    
    public abstract void setEndAnimation(EndAnimation type, double time);

    public abstract void unsetEndAnimation();
    
    public void setSubMenu(Decorator decorator) {
        this.decorator = decorator;
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!(event.getButton() == MouseButton.SECONDARY)) return;
                rightMenu.show(Decoration.this, event.getSceneX(), event.getSceneY());
            }
        });
    }
    
    private Decorator decorator;
    
    private final ContextMenu rightMenu = ContextMenuBuilder.create()
            .items(
                MenuItemBuilder.create()
                    .text("Remove")
                    .onAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            decorator.unregisterDecoration(Decoration.this);
                            hideImmediately(decorator);
                        }
                    })
                    .build(),
                new SeparatorMenuItem(),
                MenuItemBuilder.create()
                    .text("Settings")
                    .onAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            new SettingDialog(getScene().getWindow(), Decoration.this).showAndWait();
                        }
                    })
                    .build()
            )
            .build();
    
    private class SettingDialog extends Stage {
        private Decoration decoration;
        private Duration displayTime;
        private TextField textDisplayTime;
        private StartAnimation startAnimationType;
        private TextField startAnimationTime;
        private EndAnimation endAnimationType;
        private TextField endAnimationTime;
        
        public SettingDialog(Window owner, Decoration decoration) {
            initOwner(owner);
            setScene(buildScene());
            setResizable(false);
            sizeToScene();
            this.decoration = decoration;
        }
        
        private Scene buildScene() {
            VBox root = VBoxBuilder.create()
                .padding(new Insets(10,10,10,10))
                .spacing(10)
                .children(
                    buildTime(),
                    new Separator(),
                    buildStartAnimation(),
                    new Separator(),
                    buildEndAnimation(),
                    new Separator(),
                    buildOkCancel()
                )
                .build();
            Scene scene = new Scene(root, 400, 300);
            return scene;
        }
        
        private Node buildTime() {
            final ToggleGroup group = new ToggleGroup();
            final RadioButton indefinite = RadioButtonBuilder.create()
                    .text("Indefinite")
                    .toggleGroup(group).build();
            final RadioButton manual = RadioButtonBuilder.create()
                    .text("Manual")
                    .toggleGroup(group).build();
            textDisplayTime = TextFieldBuilder.create()
                    .prefColumnCount(5).disable(true).build();
            group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
                    if (newToggle == indefinite) {
                        displayTime = Duration.INDEFINITE;
                        textDisplayTime.clear();
                        textDisplayTime.setDisable(true);
                     } else {
                        displayTime = null;
                        textDisplayTime.setDisable(false);
                     }
                }
            });
            indefinite.setSelected(true);
            return VBoxBuilder.create()
                .spacing(10)
                .children(
                    LabelBuilder.create()
                        .text("Display time")
                        .font(Font.font("Arial Black", FontWeight.BOLD, 14))
                        .build(),
                    HBoxBuilder.create()
                        .padding(new Insets(0,0,0,10))
                        .spacing(5)
                        .children(
                            indefinite,
                            manual,
                            HBoxBuilder.create()
                                .spacing(2.0)
                                .children(
                                    textDisplayTime,
                                    new Label("sec") 
                                )
                                .build()
                        )
                        .build()
                )
                .build();
        }
    
        private Node buildStartAnimation() {
            startAnimationTime = TextFieldBuilder.create()
                    .prefColumnCount(5).disable(true).build();
            final ObservableList items = FXCollections.observableArrayList(
                    "NONE", StartAnimation.FADE_IN, StartAnimation.TOP_IN, StartAnimation.LEFT_IN,
                    StartAnimation.BOTTOM_IN, StartAnimation.RIGHT_IN);
            final ChoiceBox type = ChoiceBoxBuilder.create()
                    .items(items)
                    .value(items.get(0))
                    .build();
            type.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number oldNumber, Number newNumber) {
                    if (newNumber == 0) {
                        startAnimationType = null;
                        startAnimationTime.setDisable(true);
                        startAnimationTime.clear();
                    } else {
                        startAnimationType = (StartAnimation)items.get(newNumber.intValue());
                        startAnimationTime.setDisable(false);
                    }
                }
            });
            final GridPane grid = GridPaneBuilder.create()
                .padding(new Insets(0,0,0,10))
                .hgap(15)
                .vgap(10)
                .build();
            grid.addRow(0, new Label("Type"), type);
            grid.addRow(1, new Label("Time"), HBoxBuilder.create()
                .spacing(2)
                .children(startAnimationTime, new Label("sec")).build()
            );
            return VBoxBuilder.create()
                .spacing(10)
                .children(
                    LabelBuilder.create()
                        .text("Start animation")
                        .font(Font.font("Arial Black", FontWeight.BOLD, 14))
                        .build(),
                    grid
                )
                .build();
        }

        private Node buildEndAnimation() {
            endAnimationTime = TextFieldBuilder.create()
                    .prefColumnCount(5).disable(true).build();
            final ObservableList items = FXCollections.observableArrayList(
                    "NONE", EndAnimation.FADE_OUT, EndAnimation.TOP_OUT, EndAnimation.LEFT_OUT,
                    EndAnimation.BOTTOM_OUT, EndAnimation.RIGHT_OUT);
            final ChoiceBox type = ChoiceBoxBuilder.create()
                    .items(items)
                    .value(items.get(0))
                    .build();
            type.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number oldNumber, Number newNumber) {
                    if (newNumber == 0) {
                        endAnimationType = null;
                        endAnimationTime.setDisable(true);
                        endAnimationTime.clear();
                    } else {
                        endAnimationType = (EndAnimation)items.get(newNumber.intValue());
                        endAnimationTime.setDisable(false);
                    }
                }
            });
            final GridPane grid = GridPaneBuilder.create()
                .padding(new Insets(0,0,0,10))
                .hgap(15)
                .vgap(10)
                .build();
            grid.addRow(0, new Label("Type"), type);
            grid.addRow(1, new Label("Time"), HBoxBuilder.create()
                .spacing(2)
                .children(endAnimationTime,new Label("sec")) .build()
            );
            return VBoxBuilder.create()
                .spacing(10)
                .children(
                    LabelBuilder.create()
                        .text("End animation")
                        .font(Font.font("Arial Black", FontWeight.BOLD, 14))
                        .build(),
                    grid
                )
                .build();
        }
        
        private Node buildOkCancel() {
            Button ok = ButtonBuilder.create()
                .text("OK")
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        apply();
                        close();
                    }
                })
                .build();
            Button cancel = ButtonBuilder.create()
                .text("Cancel")
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        close();
                    }
                })
                .build();
            return HBoxBuilder.create()
                .spacing(10)
                .alignment(Pos.CENTER)
                .children(
                    ok, cancel
                )
                .build();
        }

        private void apply() {
            // Display time
            if (displayTime == null) {
                double time = Double.parseDouble(textDisplayTime.getText());
                displayTime = Duration.seconds(time);
            }
            decoration.setDisplayTime(displayTime);
            // Start animation
            if (startAnimationType == null) {
                decoration.unsetStartAnimation();
            } else {
                double time = Double.parseDouble(startAnimationTime.getText());
                decoration.setStartAnimation(startAnimationType, time);
            }
            // End animatoin
            if (endAnimationType == null) {
                decoration.unsetEndAnimation();
            } else {
                double time = Double.parseDouble(endAnimationTime.getText());
                decoration.setEndAnimation(endAnimationType, time);
            }
        }
    }
}
