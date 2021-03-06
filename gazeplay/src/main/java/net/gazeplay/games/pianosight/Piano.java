package net.gazeplay.games.pianosight;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.MouseEvent;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class Piano extends Parent implements GameLifeCycle {

    private static final int maxRadius = 70;
    private static final int minRadius = 30;

    private static final int maxTimeLength = 7;
    private static final int minTimeLength = 4;

    // private final EventHandler<Event> enterEvent;

    private double centerX;
    private double centerY;

    private int FirstNote;

    private Circle circ;
    private List<Tile> TilesTab;
    private Jukebox Jukebox;
    private String fileName;

    private final Stats stats;

    private final GameContext gameContext;

    private final Instru instru;

    private midiReader midiReader;

    private int nbFragments = 5;

    private final List<ImageView> fragments;

    private int prevKey = -1;

    // done
    public Piano(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2.2;
        this.fragments = buildFragments();
        this.getChildren().addAll(fragments);
        TilesTab = new ArrayList<Tile>();
        instru = new Instru();
        gameContext.getChildren().add(this);
        Jukebox = new Jukebox(gameContext);

    }

    private List<ImageView> buildFragments() {
        List<ImageView> fragments = new ArrayList<>(nbFragments);
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (int i = 0; i < nbFragments; i++) {

            String s = "data/pianosight/images/" + i + ".png";

            ImageView fragment = new ImageView(new Image(s));
            fragment.setOpacity(0);
            fragment.setPreserveRatio(true);
            fragment.setFitHeight(dimension2D.getHeight() / 10);
            fragment.setVisible(true);
            fragment.setX(-100);
            fragment.setY(-100);
            fragments.add(fragment);
        }
        return fragments;
    }

    public void explose(double Xcenter, double Ycenter) {

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        for (int i = 0; i < nbFragments; i++) {

            ImageView fragment = fragments.get(i);

            timeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(fragment.xProperty(), Xcenter, Interpolator.LINEAR)));
            timeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(fragment.yProperty(), Ycenter, Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            int worh = (int) (Math.random() * 4);

            double XendValue;
            double YendValue;
            if (worh == 0) {
                XendValue = 0;
                YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();
            } else if (worh == 1) {
                XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
                YendValue = 0;
            } else if (worh == 2) {
                XendValue = Screen.getPrimary().getBounds().getWidth();
                YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();
            } else {
                XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
                YendValue = Screen.getPrimary().getBounds().getHeight();
            }

            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(fragment.xProperty(), XendValue, Interpolator.LINEAR)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(fragment.yProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(timeline, timeline2);
        sequence.play();

    }

    public void loadMusic(boolean b) throws IOException {
        InputStream inputStream;
        if (b) {
            fileName = Jukebox.getS();
            if (fileName == null) {
                return;
            } else {

                File f = new File(fileName);
                inputStream = new FileInputStream(f);
            }
        } else {

            fileName = "RIVER.mid";
            inputStream = Utils.getInputStream("data/pianosight/songs/" + fileName);
        }
        log.info("you loaded the song : " + fileName);
        midiReader = new midiReader(inputStream);
        FirstNote = midiReader.nextNote();
        for (int i = 0; i < TilesTab.size(); i++) {
            TilesTab.get(i).setFill(TilesTab.get(i).color1);
        }

        if (FirstNote != -1) {
            TilesTab.get(FirstNote).setFill(Color.YELLOW);
        }
    }

    @Override
    public void launch() {
        this.gameContext.resetBordersToFront();

        CreateArcs();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        circ = new Circle(centerX, centerY, dimension2D.getHeight() / 4);
        circ.setFill(Color.BLACK);

        this.getChildren().add(circ);
        this.getChildren().addAll(this.TilesTab);
        this.getChildren().get(this.getChildren().indexOf(circ)).toFront();
        ImageView iv = new ImageView(new Image("data/pianosight/images/1.png"));
        Button b = new Button("Open", iv);
        b.setPrefWidth(dimension2D.getWidth() / 7);
        b.setPrefHeight(dimension2D.getHeight() / 7);
        iv.setPreserveRatio(true);
        iv.setFitHeight(b.getPrefHeight());
        b.setOnMousePressed(evt -> {
            try {
                loadMusic(true);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.getChildren().add(b);

        try {

            loadMusic(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {

    }

    public void CreateArc(int index, double angle, Color color1, Color color2, double l, double origin) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double size = dimension2D.getHeight() / l;
        double theta = ((index * 360) / 7 - origin);
        Tile a3 = new Tile(centerX, centerY, size, size, (int) theta, angle);
        a3.setType(ArcType.ROUND);
        a3.setStroke(Color.BLACK);
        a3.color1 = color1;
        a3.color2 = color2;
        a3.setFill(color1);
        a3.setStrokeWidth(10);
        a3.setVisible(true);

        EventHandler<Event> tileEventEnter = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (((Tile) e.getTarget()).note == FirstNote) {

                    int precNote = FirstNote;
                    int precKey = midiReader.key;

                    FirstNote = midiReader.nextNote();

                    if (precNote != -1) {
                        instru.note_on(precKey);
                        /*
                         * if(prevKey!=-1) { instru.note_off(prevKey); }
                         */
                        prevKey = precKey;
                        stats.incNbGoals();
                        stats.notifyNewRoundReady();
                        double x;
                        double y;

                        if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                            MouseEvent me = (MouseEvent) e;
                            x = me.getX();
                            y = me.getY();
                        } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                            GazeEvent ge = (GazeEvent) e;
                            x = ge.getX();
                            y = ge.getY();
                        } else {
                            x = centerX + size * Math.cos(Math.toRadians(-theta));
                            y = centerY + size * Math.sin(Math.toRadians(-theta));
                            explose(x, y);
                            double theta = (((index + 1) * 360) / 7 - origin);
                            x = centerX + size * Math.cos(Math.toRadians(-theta));
                            y = centerY + size * Math.sin(Math.toRadians(-theta));
                        }
                        explose(x, y);
                        if (FirstNote != -1) {
                            if (TilesTab.get(FirstNote).getFill() == Color.YELLOW) {

                                TilesTab.get(FirstNote).setFill(Color.ORANGE);
                            } else if (TilesTab.get(FirstNote).getFill() == Color.ORANGE) {

                                TilesTab.get(FirstNote).setFill(Color.YELLOW);
                            } else {

                                TilesTab.get(precNote).setFill(color1);
                                TilesTab.get(FirstNote).setFill(Color.YELLOW);
                            }

                        } else {
                            TilesTab.get(precNote).setFill(color2);
                        }
                    } else {
                        TilesTab.get(precNote).setFill(color2);
                    }
                } else {

                    TilesTab.get(((Tile) e.getTarget()).note).setFill(color2);

                }

            }

        };

        EventHandler<Event> tileEventExited = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if ((Color) TilesTab.get(((Tile) e.getTarget()).note).getFill() == color2) {
                    TilesTab.get(((Tile) e.getTarget()).note).setFill(color1);
                }

            }
        };

        a3.tileEventEnter = tileEventEnter;
        a3.tileEventExited = tileEventExited;

        a3.addEventFilter(MouseEvent.MOUSE_ENTERED, a3.tileEventEnter);
        a3.addEventFilter(MouseEvent.MOUSE_EXITED, a3.tileEventExited);
        a3.addEventFilter(GazeEvent.GAZE_ENTERED, a3.tileEventEnter);
        a3.addEventFilter(GazeEvent.GAZE_EXITED, a3.tileEventExited);
        a3.note = index;

        gameContext.getGazeDeviceManager().addEventFilter(a3);

        TilesTab.add(index, a3);
    }

    public void CreateArcs() {
        for (int i = 0; i < 7; i++) {
            double angle = 360 / 7;
            if (i == 6) {
                angle = 360 - 6 * angle;
            }
            CreateArc(i, angle, Color.GHOSTWHITE, Color.GAINSBORO.darker(), 2.3, 0);
        }

        for (int i = 7; i < 14; i++) {
            double angle = 360 / 14;
            CreateArc(i, angle, Color.BLACK, Color.DIMGREY.darker(), 2.7, angle / 2);
        }

    }

}
