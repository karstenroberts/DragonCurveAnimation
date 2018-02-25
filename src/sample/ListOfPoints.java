package sample;

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
