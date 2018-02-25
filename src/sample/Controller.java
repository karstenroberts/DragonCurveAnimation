package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.awt.Color.BLACK;
import static sample.Controller.Movement.*;

public class Controller {

    @FXML
    private Canvas canvas;

    @FXML
    private TextField iterationsField;

    @FXML
    private Button drawButton;

    enum Movement{UP, DOWN, LEFT, RIGHT}

    Movement facing;
    int lineWidth = 1;
    int lineLength = 2;

    GraphicsContext gc;


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
        gc.clearRect(0,0,1700, 1000);
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

        int difX = rightest-leftest;
        int difY = downest-upest;

        if((1686.0 / (double)(rightest-leftest)) < (986.0 / (double)(downest-upest))){
            resizeFactor = 1686.0 / (double)(rightest-leftest);
        }else{
            resizeFactor = 986.0 / (double)(downest-upest);
        }

        //subtract leftest from all points,
        for (int i = 1; i < curveList.getWholeCurve().size(); i++){
            gc.strokeLine(((curveList.getWholeCurve().get(i-1).getX()-(leftest + negativeXFactor)) * resizeFactor)+7, ((curveList.getWholeCurve().get(i-1).getY()-(upest + negativeYFactor)) * resizeFactor)+7, ((curveList.getWholeCurve().get(i).getX()-(leftest + negativeXFactor)) * resizeFactor)+7, ((curveList.getWholeCurve().get(i).getY()-(upest+negativeYFactor)) * resizeFactor)+7);

        }

        //draw scaled lines
    }
}
