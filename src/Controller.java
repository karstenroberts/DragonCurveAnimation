import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import java.util.ArrayList;

//import static Controller.Movement.*;

public class Controller {

    @FXML
    private Button pausePlayButton;

    @FXML
    private Canvas canvas;

    @FXML
    private TextField iterationsField;

    @FXML
    private Canvas animationCanvas;

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
     * Where Animation is drawn
     */
    GraphicsContext gcA;

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

    ListOfPoints curveList;



    /**
     * gets sets the graphics global vars
     */
    public void initialize(){
        gc = canvas.getGraphicsContext2D();
        gcA = animationCanvas.getGraphicsContext2D();
        width = canvas.getWidth();
        height = canvas.getHeight();
    }


    @FXML
    void toggleAnimation(){
        if(animation != null){
            if(animationCanvas.isVisible()) {
                if (animation.getStatus().equals(Animation.Status.RUNNING)) {
                    animation.pause();
                }
                animationCanvas.setVisible(false);
            }else{
                animationCanvas.setVisible(true);
                if(animation.getStatus().equals(Animation.Status.PAUSED)){
                    animation.play();
                }else if(animation.getStatus().equals(Animation.Status.RUNNING)){
                    //Following three lines of code necessary to keep old animation started when animation wasn't visible from continuing.
                    //Only occurred when switching levels with animations turned off, and turning animations back on
                    animation.stop();
                    gcA.clearRect(0,0,width,height);
                    playAnimation();
                }
            }
        }
    }

    @FXML
    void toggleSkeleton(){
        if(canvas.isVisible()){
            canvas.setVisible(false);
        }else{
            canvas.setVisible(true);
        }
    }

    /**
     * Called by "Pause" or "Play" button. Pauses or resumes the animation, depending on the context
     */
    @FXML
    void pausePlay(){
        if(animation != null){
            if(animation.getStatus().equals(Animation.Status.RUNNING)){
                animation.pause();
                pausePlayButton.setText("Play");
            }else if(animation.getStatus().equals(Animation.Status.PAUSED)){
                animation.play();
                pausePlayButton.setText("Pause");
            }
        }
    }

// FIXME: 2/26/18
    /**
     * Designed to start the next level animation after current one is done.
     * TODO:MAKE THIS THING ACTUALLY WORK
     */
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
        gcA.clearRect(0,0,1700,1000);

        int canvasWidth = (int)canvas.getWidth();
        int canvasHeight = (int)canvas.getHeight();

        //arbitrary starting coordinates for the fractal. Could be set to literally any real number.
        int startX = canvasWidth/2;
        int startY = canvasHeight/2;

        //get the list of points for all turns
        curveList = createPoints(startX,startY);

        //find furthest up, down, left, right points. Used for scaling/transformation of points so the fractal fits perfectly on screen.
        curveList.determineParams();

        //draw basic black structure of current level of fractal
        drawSkeleton();


        //Creates the animation, then plays it on top of the skeleton.
        //Duration done by guess and check, it isn't perfect. The smaller the number, the quicker but messier the animation.
        //Animation has some sort of acceleration that I can't seem to get rid of
        playAnimation();
    }

    private void playAnimation(){
        if(currLevel<=1) { //switch to <= to always draw whole fractal, >= to animate only the previous level (only animates half of current level)
            animation = createPathAnimation(createPath(), Duration.millis( (2000 * Math.pow(Dragon.getInstance().getCurve().size(), .6)) + 4000));
        }else {
            animation = createPathAnimation(createPath(), Duration.millis(((2000 * Math.pow(Dragon.getInstance().getCurve().size(), .7)) + 4000)/2));
        }

        animation.play();
    }

    private void drawSkeleton(){
        gc.setStroke(Color.BLACK);

        //Goes through all turns, and draws the basic black skeleton of the fractal. Deals with all transformation of old points using negativeFactors, resizeFactor, and margin
        for (int i = 1; i < curveList.getWholeCurve().size(); i++){

            double startAnX = ((curveList.getWholeCurve().get(i-1).getX()-(curveList.getFurthestLeft())) * curveList.getResizeFactor(width,height,margin))+margin;
            double startAnY = ((curveList.getWholeCurve().get(i-1).getY()-(curveList.getFurthestUp())) * curveList.getResizeFactor(width,height,margin))+margin;
            double endAnX = ((curveList.getWholeCurve().get(i).getX()-(curveList.getFurthestLeft())) * curveList.getResizeFactor(width,height,margin))+margin;
            double endAnY = ((curveList.getWholeCurve().get(i).getY()-(curveList.getFurthestUp())) * curveList.getResizeFactor(width,height,margin))+margin;
            if (i == 1) {
                System.out.println(curveList.getFurthestLeft());
                System.out.println(curveList.getFurthestUp());
                //System.out.println(curveList.getNegativeXFactor());
                //System.out.println(curveList.getNegativeYFactor());
                System.out.println(curveList.getResizeFactor(width,height,margin));

            }
            //draw scaled lines
            gc.strokeLine(startAnX, startAnY, endAnX, endAnY);

        }
    }

    /**
     * Generates the list of points for the level previous
     * @return A list of points for the level previous
     */
    private ArrayList<Point> getPrevCurve(){
        ArrayList<Point> prevCurve = new ArrayList<>();
        for (Point p : curveList.getWholeCurve().subList(0, (curveList.getWholeCurve().size()/2)+1)){
            prevCurve.add(p);
        }
        return prevCurve;
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
        facing = Movement.UP;

        //go through each turn in the fractal, and add the resulting end point
        for(Dragon.DIR direction : curve){
            switch (facing){
                case UP:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX-lineLength,startY));
                        startX-=lineLength;
                        facing=Movement.LEFT;
                    }else{
                        curveList.addPoint(new Point(startX+lineLength,startY));
                        startX+=lineLength;
                        facing=Movement.RIGHT;
                    }
                    break;
                case DOWN:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX+lineLength,startY));
                        startX+=lineLength;
                        facing=Movement.RIGHT;
                    }else{
                        curveList.addPoint(new Point(startX-lineLength,startY));
                        startX-=lineLength;
                        facing=Movement.LEFT;
                    }
                    break;
                case LEFT:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX,startY+lineLength));
                        startY+=lineLength;
                        facing=Movement.DOWN;
                    }else{
                        curveList.addPoint(new Point(startX,startY-lineLength));
                        startY-=lineLength;
                        facing=Movement.UP;
                    }
                    break;
                case RIGHT:
                    if(direction == Dragon.DIR.L){
                        curveList.addPoint(new Point(startX,startY-lineLength));
                        startY-=lineLength;
                        facing=Movement.UP;
                    }else{
                        curveList.addPoint(new Point(startX,startY+lineLength));
                        startY+=lineLength;
                        facing=Movement.DOWN;
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
     * @return Path for animation
     */
    private Path createPath() {

        Path path = new Path();
        path.setStroke(Color.RED);
        path.setStrokeWidth(2);

        //goes through each Point, and creates a line segment for the animation to follow
        for(int i = 1; i < curveList.getWholeCurve().size(); i++){
            path.getElements().addAll(new MoveTo(((curveList.getWholeCurve().get(i-1).getX()-(curveList.getFurthestLeft())) * curveList.getResizeFactor(width,height,margin))+7, ((curveList.getWholeCurve().get(i-1).getY()-(curveList.getFurthestUp())) * curveList.getResizeFactor(width,height,margin))+7), new LineTo(((curveList.getWholeCurve().get(i).getX()-(curveList.getFurthestLeft())) * curveList.getResizeFactor(width,height,margin))+7,((curveList.getWholeCurve().get(i).getY()-(curveList.getFurthestUp())) * curveList.getResizeFactor(width,height,margin))+7));
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
        Circle pen = new Circle(0, 0, anWidth);

        // create path transition

        PathTransition pathTransition = new PathTransition( duration, path, pen);
        pathTransition.setInterpolator(Interpolator.LINEAR);
        //pathTransition.cycleCountProperty().setValue(2);
        pathTransition.setAutoReverse(true);
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
                gcA.setStroke(Color.hsb(0,1,1,1));
                gcA.setFill(Color.YELLOW);
                gcA.setLineWidth(anWidth);
                gcA.strokeLine(oldLocation.x, oldLocation.y, x, y);

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
