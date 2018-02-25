package sample;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import java.util.ArrayList;

import static sample.Controller.Movement.*;

public class Controller {

    @FXML
    private Canvas canvas;

    @FXML
    private TextField iterationsField;

    /**
     * Possible directions a segment can face
     */
    enum Movement{UP, DOWN, LEFT, RIGHT}

    /**
     * current level of recursion being drawn.
     */
    int currLevel = 0;

    /**
     * The current direction the line is oriented
     */
    Movement facing;

    /**
     * how wide to draw the base skeleton lines
     */
    int lineWidth = 1;

    /**
     * base width to originally draw fractal without scaling to fit screen size. Note that it doesn't matter what you set this to so long as it's greater than 0.
     */
    int lineLength = 2;

    /**
     * Where base skeleton is drawn
     */
    GraphicsContext gc;

    /**
     * honestly don't think this has to be global, but for some reason breaks if it's not :P
     */
    Animation animation;

    /**
     * width of drawing canvas
     */
    double width;

    /**
     * height of drawing canvas
     */
    double height;

    /**
     * margin around fractal
     */
    double margin = 7;


    @FXML
    void slideShow(){
        while(!iterationsField.getText().equals("stop")){
            if(animation.getStatus().equals(Animation.Status.STOPPED)){
                nextLevel();
                System.out.println("Level: " + currLevel);
            }
        }
    }

    /**
     * Called by "Draw" button, or hitting enter in the text field. Extracts the desired level, and calls method to generate appropriate fractal
     */
    @FXML
    void drawCurrCurve() {
        int level = Integer.valueOf(iterationsField.getText());
        if (level > 0) {
            System.out.println("Level: " + level);
            currLevel = level;
            genLevel(level);
        }
    }

    /**
     * Called by ">" button, generates next level deeper fractal
     */
    @FXML
    public void nextLevel(){
        Dragon.getInstance().nextLevel();
        currLevel++;
        drawCurve();
        iterationsField.setText("" + currLevel);
    }

    /**
     * Called by "<" button, generates previous level fractal
     */
    @FXML
    public void prevLevel(){
        Dragon.getInstance().prevLevel();
        currLevel--;
        drawCurve();
        iterationsField.setText("" + currLevel);
    }

    /**
     * gets sets the graphics global var
     */
    public void initialize(){
        gc = canvas.getGraphicsContext2D();
        width = canvas.getWidth();
        height = canvas.getHeight();
    }

    /**
     * Draws the fractal delveLevel levels further. SUUUUPER SLOW.
     * @param delveLevel The number of levels to move forward
     */
    public void nextNLevels(int delveLevel){
        //System.out.println("Current curve: " + Dragon.getInstance().toString());
        Dragon.getInstance().delve(delveLevel);
        //System.out.println("New Curve: " + Dragon.getInstance().toString());
        drawCurve();
    }

    /**
     * Generates the delveLevel'th level fractal
     * @param delveLevel what level of curve to draw
     */
    public void genLevel(int delveLevel){
        //System.out.println("Current curve: " + Dragon.getInstance().toString());
        Dragon.getInstance().genLevel(delveLevel);
        //System.out.println("New Curve: " + Dragon.getInstance().toString());
        drawCurve();
    }

    /**
     * Handles all drawing to the screen.
     */
    private void drawCurve(){

        //If the animation from the previous rendering is still going, stop it.
        if((animation != null) && animation.getStatus().equals(Animation.Status.RUNNING)){
            animation.stop();
        }

        //Erase the canvas
        gc.clearRect(0,0,1700, 1000);

        int canvasWidth = (int)canvas.getWidth();
        int canvasHeight = (int)canvas.getHeight();

        //arbitrary starting coordinates for the fractal. Could be set to literally any real number.
        int startX = canvasWidth/2;
        int startY = canvasHeight/2;

        //get the list of points for all turns
        ListOfPoints curveList = createPoints(startX,startY);

        //find furthest up, down, left, right points. Used for scaling/transformation of points so the fractal fits perfectly on screen.
        curveList.determineParams();
        int leftest = curveList.furthestLeft;
        int rightest = curveList.furthestRight;
        int downest = curveList.furthestDown;
        int upest = curveList.furthestUp;

        //If some of the starting points had negative coords, need to account for that so entire fractal is on screen.
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

        //resizeFactor is used to scale up or down the fractal to fit the screen. Uses min of x scale or y scale on both X and Y coords so proportionally still square.
        //takes into account buffer space between edge of drawing canvas and the drawn fractal
        double resizeFactor;
        if(((width - 2*margin) / (double)(rightest-leftest)) < ((height-2*margin) / (double)(downest-upest))){
            resizeFactor = (width-2*margin)/ (double)(rightest-leftest);
        }else{
            resizeFactor = (height-2*margin) / (double)(downest-upest);
        }

        gc.setStroke(Color.BLACK);

        //Goes through all turns, and draws the basic black skeleton of the fractal. Deals with all transformation of old points using negativeFactors, resizeFactor, and margin
        for (int i = 1; i < curveList.getWholeCurve().size(); i++){
            double startAnX = ((curveList.getWholeCurve().get(i-1).getX()-(leftest + negativeXFactor)) * resizeFactor)+margin;
            double startAnY = ((curveList.getWholeCurve().get(i-1).getY()-(upest + negativeYFactor)) * resizeFactor)+margin;
            double endAnX = ((curveList.getWholeCurve().get(i).getX()-(leftest + negativeXFactor)) * resizeFactor)+margin;
            double endAnY = ((curveList.getWholeCurve().get(i).getY()-(upest + negativeYFactor)) * resizeFactor)+margin;
            //draw scaled lines
            gc.strokeLine(startAnX, startAnY, endAnX, endAnY);

        }

        //Creates the animation, then plays it on top of the skeleton.
        //Duration done by guess and check, it isn't perfect. The smaller the number, the quicker but messier the animation.
        //Animation has some sort of acceleration that I can't seem to get rid of
        animation = createPathAnimation(createPath(curveList.getWholeCurve(), leftest, upest, negativeXFactor, negativeYFactor, resizeFactor), Duration.millis(2000 *Math.pow(Dragon.getInstance().getCurve().size(),.6)));
        animation.play();
    }


    /**
     * This function creates a ListOfPoints object containing a list of the coords of all turns in the fractal using the generated list of turns in the Dragon singleton.
     * @param startX The arbitrary start x position
     * @param startY The arbitrary start y position
     * @return A ListOfPoints object containing all points needed to draw the fractal
     */
    private ListOfPoints createPoints(int startX, int startY){

        gc.setLineWidth(lineWidth);

        //get the list of turns from the Dragon Singleton
        ArrayList<Dragon.DIR> curve = Dragon.getInstance().getCurve();

        //initialize and add the first points of the first segment before the first turn of the fractal
        ListOfPoints curveList = new ListOfPoints();
        Point startPoint = new Point(startX,startY);
        curveList.addPoint(startPoint);
        curveList.addPoint(new Point(startX,startY-lineLength));
        startY-=lineLength;
        facing = UP;

        //go through each turn in the fractal, and add the resulting end point
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
        return curveList;
    }


    //--------------------------------------------------------------------------------------------------------------------------------------------------------//

    //------FOLLOWING CODE MODIFIED FROM:  https://stackoverflow.com/questions/35585035/what-the-easiest-way-to-animate-a-path-as-an-object-traverses-it------//

    //--------------------------------------------------------------------------------------------------------------------------------------------------------//


    /**
     * Creates a Path object of all of the points in the fractal for the animation to use
     * @param curve List of all points in the fractal
     * @param leftest The smallest x coord (including negative), used for transforming to fit screen
     * @param upest The smallest y coord (including negative), used for transforming to fit screen
     * @param negativeXFactor Used to account for negative x coords
     * @param negativeYFactor Used to account for negative y coords
     * @param resizeFactor Scales fractal to fit screen while retaining square ratio
     * @return Path for animation
     */
    private Path createPath(ArrayList<Point> curve, int leftest, int upest, int negativeXFactor, int negativeYFactor, double resizeFactor) {

        Path path = new Path();
        path.setStroke(Color.RED);
        path.setStrokeWidth(2);

        //goes through each Point, and creates a line segment for the animation to follow
        for(int i = 1; i < curve.size(); i++){
            path.getElements().addAll(new MoveTo(((curve.get(i-1).getX()-(leftest + negativeXFactor)) * resizeFactor)+7, ((curve.get(i-1).getY()-(upest + negativeYFactor)) * resizeFactor)+7), new LineTo(((curve.get(i).getX()-(leftest + negativeXFactor)) * resizeFactor)+7,((curve.get(i).getY()-(upest + negativeYFactor)) * resizeFactor)+7));
        }

        return path;
    }

    /**
     * Creates an animation following the path of generation of the fractal.
     * @param path The path for the animation to follow
     * @param duration How long the animation should last
     * @return Animation to be played on screen.
     */
    private Animation createPathAnimation(Path path, Duration duration) {

        //width of animation line
        int anWidth = 2;

        gc = canvas.getGraphicsContext2D();

        Circle pen = new Circle(0, 0, anWidth);

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
                gc.setStroke(Color.hsb(0,1,1,1));
                gc.setFill(Color.YELLOW);
                gc.setLineWidth(anWidth);
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
