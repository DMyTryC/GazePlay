package net.gazeplay.commons.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ProgressCustomButton extends StackPane {

    public CustomButton button;
    ProgressIndicator indicator;
    Timeline timelineProgressBar;
    double buttonWidth;
    double buttonHeight;

    public ProgressCustomButton(String s) {
        super();
        button = new CustomButton(s);
        init();
        this.getChildren().addAll(button, indicator);
    }

    public void init() {
        buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        button.heightProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(newVal.doubleValue() * 0.9);
            buttonHeight = newVal.doubleValue();
            // indicator.setTranslateY(indicator.getTranslateY()-(oldVal.doubleValue()/0.1) +
            // (newVal.doubleValue())*0.1);
            // log.info("button size modified: " + newVal.doubleValue());
            indicator.toFront();
        });
        button.widthProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinWidth(newVal.doubleValue() * 0.9);
            buttonWidth = newVal.doubleValue();
            // indicator.setTranslateX(indicator.getTranslateX()-(oldVal.doubleValue()/0.1)*2 +
            // (newVal.doubleValue()/2)*0.1);
            indicator.toFront();
            // log.info("button size modified: " + newVal.doubleValue());
        });
        button.layoutXProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setTranslateX(newVal.doubleValue() + (buttonWidth / 2) * 0.1);
            indicator.toFront();
            // log.info("position changed: " + newVal.doubleValue());
        });
        button.layoutYProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setTranslateY(newVal.doubleValue() + buttonHeight * 0.1);
            indicator.toFront();
            // log.info("position changed: " + newVal.doubleValue());
        });
    }

    public ProgressIndicator assignIndicator(EventHandler<Event> enterEvent) {
        indicator.setMouseTransparent(true);

        indicator.setOpacity(0);
        EventHandler<Event> enterbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                indicator.setOpacity(1);
                timelineProgressBar = new Timeline();

                timelineProgressBar.setDelay(new Duration(500));

                timelineProgressBar.getKeyFrames()
                        .add(new KeyFrame(new Duration(500), new KeyValue(indicator.progressProperty(), 1)));

                timelineProgressBar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        enterEvent.handle(null);

                    }
                });
                timelineProgressBar.play();

            }
        };
        // this.addEventHandler(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
        this.addEventHandler(GazeEvent.GAZE_ENTERED, enterbuttonHandler);

        EventHandler<Event> exitbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);

            }
        };
        // this.addEventHandler(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
        this.addEventHandler(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        return indicator;
    }

}