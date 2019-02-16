import java.util.ArrayList;

public class ListOfPoints {

    ArrayList<Point> wholeCurve;

    int furthestLeft;
    int furthestRight;
    int furthestUp;
    int furthestDown;

    public ArrayList<Point> getWholeCurve() {
        return wholeCurve;
    }

    public int getFurthestLeft() {
        return furthestLeft;
    }

    public int getFurthestRight() {
        return furthestRight;
    }

    public int getFurthestUp() {
        return furthestUp;
    }

    public int getFurthestDown() {
        return furthestDown;
    }

    public ListOfPoints(){
        this.wholeCurve = new ArrayList<>();
    }

    public void addPoint(Point point){
        this.wholeCurve.add(point);
    }

    //resizeFactor is used to scale up or down the fractal to fit the screen. Uses min of x scale or y scale on both X and Y coords so proportionally still square.
    //takes into account buffer space between edge of drawing canvas and the drawn fractal
    public double getResizeFactor(double width, double height, double margin){
        if(((width - 2*margin) / (double)(furthestRight-furthestLeft)) < ((height-2*margin) / (double)(furthestDown-furthestUp))){
            return (width-2*margin)/ (double)(furthestRight-furthestLeft);
        }else{
            return (height-2*margin) / (double)(furthestDown-furthestUp);
        }
    }

    public void determineParams(){
        Point firstPoint = wholeCurve.get(0);
        furthestDown = firstPoint.getY();
        furthestUp = firstPoint.getY();
        furthestLeft = firstPoint.getX();
        furthestRight = firstPoint.getX();

        for(Point p : wholeCurve){
            if(p.getX() > furthestRight){
                furthestRight = p.getX();
            }else if(p.getX() < furthestLeft){
                furthestLeft = p.getX();
            }

            if(p.getY() > furthestDown){
                furthestDown = p.getY();
            }else if(p.getY() < furthestUp){
                furthestUp = p.getY();
            }
        }
    }
}
