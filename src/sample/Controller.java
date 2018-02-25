package sample;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.awt.Color.BLACK;
import static java.awt.Color.GREEN;
import static sample.Controller.Movement.*;

public class Controller {

    @FXML
    private Canvas canvas;

    @FXML
    private TextField iterationsField;

    @FXML
    private Button drawButton;

    @FXML
    private Canvas animationCanvas;

    enum Movement{UP, DOWN, LEFT, RIGHT}

    Movement facing;
    int lineWidth = 1;
    int lineLength = 2;

    GraphicsContext gc;
    GraphicsContext gcA;

    Animation animation;



    @FXML
    void drawCurrCurve() {
        int level = Integer.valueOf(iterationsField.getText());
        if (level > 0) {
            System.out.println("Level: " + level);
            genLevel(level);
        }
    }

    @FXML
    public void nextLevel(){
        Dragon.getInstance().nextLevel();
        drawCurve();
    }

    @FXML
    public void prevLevel(){
        Dragon.getInstance().prevLevel();
        drawCurve();
    }

    public void initialize(){
        gc = canvas.getGraphicsContext2D();
        gcA = animationCanvas.getGraphicsContext2D();
    }

    public void nextNLevels(int delveLevel){
        //System.out.println("Current curve: " + Dragon.getInstance().toString());
        Dragon.getInstance().delve(delveLevel);
        //System.out.println("New Curve: " + Dragon.getInstance().toString());
        drawCurve();
    }

    public void genLevel(int delveLevel){
        //System.out.println("Current curve: " + Dragon.getInstance().toString());
        Dragon.getInstance().genLevel(delveLevel);
        //System.out.println("New Curve: " + Dragon.getInstance().toString());
        drawCurve();
    }

    private void drawCurve(){
        if((animation != null) && animation.getStatus().equals(Animation.Status.RUNNING)){
            animation.stop();
        }

        gc.clearRect(0,0,1700, 1000);
        gcA.clearRect(0,0,1700,1000);
        ArrayList<Dragon.DIR> curve = Dragon.getInstance().getCurve();

        int canvasWidth = (int)canvas.getWidth();
        int canvasHeight = (int)canvas.getHeight();

        int startX = canvasWidth/2;
        int startY = canvasHeight/2;

        ListOfPoints curveList = new ListOfPoints();
        Point startPoint = new Point(startX,startY);
        curveList.addPoint(startPoint);
        curveList.addPoint(new Point(startX,startY-lineLength));
        startY-=lineLength;

        gc.setLineWidth(lineWidth);
        facing = UP;
        for(Dragon.DIR direction : curve){
            switch (facing){
                case UP:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX-lineLength,startY));
                        startX-=lineLength;
                        facing=LEFT;
                    }else{
                        curveList.addPoint(new Point(startX+lineLength,startY));
                        startX+=lineLength;
                        facing=RIGHT;
                    }
                    break;
                case DOWN:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX+lineLength,startY));
                        startX+=lineLength;
                        facing=RIGHT;
                    }else{
                        curveList.addPoint(new Point(startX-lineLength,startY));
                        startX-=lineLength;
                        facing=LEFT;
                    }
                    break;
                case LEFT:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX,startY+lineLength));
                        startY+=lineLength;
                        facing=DOWN;
                    }else{
                        curveList.addPoint(new Point(startX,startY-lineLength));
                        startY-=lineLength;
                        facing=UP;
                    }
                    break;
                case RIGHT:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX,startY-lineLength));
                        startY-=lineLength;
                        facing=UP;
                    }else{
                        curveList.addPoint(new Point(startX,startY+lineLength));
                        startY+=lineLength;
                        facing=DOWN;
                    }
                    break;
            }
        }

        //find furthest up, down, left, right points
        curveList.determineParams();
        int leftest = curveList.furthestLeft;
        int rightest = curveList.furthestRight;
        int downest = curveList.furthestDown;
        int upest = curveList.furthestUp;

        double resizeFactor;
        int negativeXFactor = 0;
        int negativeYFactor = 0;

        if (leftest < 0){
            negativeXFactor = leftest;
            rightest -= leftest;
            leftest -= leftest;
        }
        if (upest < 0){
            negativeYFactor = upest;
            downest -= upest;
            upest -= upest;
        }


        if((1686.0 / (double)(rightest-leftest)) < (986.0 / (double)(downest-upest))){
            resizeFactor = 1686.0 / (double)(rightest-leftest);
        }else{
            resizeFactor = 986.0 / (double)(downest-upest);
        }


        gc.setStroke(Color.BLACK);
        //subtract leftest from all points,
        for (int i = 1; i < curveList.getWholeCurve().size(); i++){
            double startAnX = ((curveList.getWholeCurve().get(i-1).getX()-(leftest + negativeXFactor)) * resizeFactor)+7;
            double startAnY = ((curveList.getWholeCurve().get(i-1).getY()-(upest + negativeYFactor)) * resizeFactor)+7;
            double endAnX = ((curveList.getWholeCurve().get(i).getX()-(leftest + negativeXFactor)) * resizeFactor)+7;
            double endAnY = ((curveList.getWholeCurve().get(i).getY()-(upest + negativeYFactor)) * resizeFactor)+7;
            //draw scaled lines
            gc.strokeLine(startAnX, startAnY, endAnX, endAnY);

        }
        animation = createPathAnimation(createPath(curveList.getWholeCurve(), leftest, upest, negativeXFactor, negativeYFactor, resizeFactor), Duration.millis(2000  *Math.pow(curve.size(),.5)));
        animation.play();
    }

    private Path createPath(ArrayList<Point> curve, int leftest, int upest, int negativeXFactor, int negativeYFactor, double resizeFactor) {

        Path path = new Path();

        path.setStroke(Color.RED);
        path.setStrokeWidth(2);

        for(int i = 1; i < curve.size(); i++){
            path.getElements().addAll(new MoveTo(((curve.get(i-1).getX()-(leftest + negativeXFactor)) * resizeFactor)+7, ((curve.get(i-1).getY()-(upest + negativeYFactor)) * resizeFactor)+7), new LineTo(((curve.get(i).getX()-(leftest + negativeXFactor)) * resizeFactor)+7,((curve.get(i).getY()-(upest + negativeYFactor)) * resizeFactor)+7));
        }

        return path;
    }

    private Animation createPathAnimation(Path path, Duration duration) {
        int width = 2;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // move a node along a path. we want its position
        Circle pen = new Circle(0, 0, width);

        // create path transition
        PathTransition pathTransition = new PathTransition( duration, path, pen);
        pathTransition.currentTimeProperty().addListener( new ChangeListener<Duration>() {

            Location oldLocation = null;

            /**
             * Draw a line from the old location to the new location
             */
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

                // skip starting at 0/0
                if( oldValue == Duration.ZERO)
                    return;

                // get current location
                double x = pen.getTranslateX();
                double y = pen.getTranslateY();

                // initialize the location
                if( oldLocation == null) {
                    oldLocation = new Location();
                    oldLocation.x = x;
                    oldLocation.y = y;
                    return;
                }

                // draw line
                gc.setStroke(Color.RED);
                gc.setFill(Color.YELLOW);
                gc.setLineWidth(width);
                gc.strokeLine(oldLocation.x, oldLocation.y, x, y);

                // update old location with current one
                oldLocation.x = x;
                oldLocation.y = y;
            }
        });

        return pathTransition;
    }

    public static class Location {
        double x;
        double y;
    }

}
