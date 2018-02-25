package sample;

import java.util.ArrayList;

public class Dragon {

    /**
     * The Fractal Singleton Instance
     */
    private static Dragon ourInstance = new Dragon();

    /**
     * Getter for list of turns
     * @return
     */
    public ArrayList<DIR> getCurve() {
        return curve;
    }

    /**
     * Setter for list of turns
     * @param curve
     */
    public void setCurve(ArrayList<DIR> curve) {
        this.curve = curve;
    }

    /**
     * Valid turn directions
     */
    enum DIR {L,R};

    /**
     * List of turns in current level of fractal
     */
    private ArrayList<DIR> curve;

    /**
     * Gets instance of Dragon, or creates it if it hasn't been created already
     * @return Dragon instance at current level
     */
    public static Dragon getInstance() {
        if(ourInstance == null){
            ourInstance = new Dragon();
        }
        return ourInstance;
    }

    /**
     * Constructor
     */
    private Dragon() {
        curve = new ArrayList<>();
    }

    /**
     * Takes the current curve, and replaces it with the n+delveLevel iteration
     * @param delveLevel number of iterations to increase
     */
    public void delve(int delveLevel){
        for(int i = 0; i < delveLevel; i++){
            nextLevel();
        }
    }

    public void genLevel(int delveLevel){
        curve.clear();
        delve(delveLevel);
    }

    /**
     * Takes the current curve, and replaces it with the next iteration
     */
    public void nextLevel(){
        ArrayList<DIR> levelUp = new ArrayList<>();
        //XOR
        levelUp = xOR();
        //reverse it
        levelUp = reverseCurve(levelUp);
        curve.add(DIR.R);
        curve.addAll(levelUp);
    }

    public void prevLevel(){
        ArrayList<DIR> levelDown = new ArrayList<>();
        for(int i = 0; i < (curve.size()/2); i++){
            levelDown.add(curve.get(i));
        }
        curve = levelDown;
    }

    /**
     * Takes the current curve, and returns a copy of it with all L's swapped with R's and R's swapped with L's
     * @return A mirrored curve
     */
    private ArrayList<DIR> xOR(){
        ArrayList<DIR> mirror = new ArrayList<>();
        for(DIR turn: curve){
            if (turn.equals(DIR.L)){
                mirror.add(DIR.R);
            }else{
                mirror.add(DIR.L);
            }
        }
        return mirror;
    }

    /**
     * Takes the mirrored curve, and reverses the order of it
     * @param xorCurve mirrored curve
     * @return Fully transformed copy
     */
    private ArrayList<DIR> reverseCurve(ArrayList<DIR> xorCurve){
        ArrayList<DIR> reverse = new ArrayList<>();
        for(int i = xorCurve.size()-1; i > -1; i--){
            reverse.add(xorCurve.get(i));
        }
        return reverse;
    }

    /**
     * Make it printable (relic of text only version)
     * @return String consisting of the L's and R's that make up the fractal
     */
    @Override
    public String toString() {
        String stringified = "";
        if(curve.isEmpty()){
            stringified = "No curve yet generated!";
        }else {
            for (DIR turn : curve) {
                if (turn == DIR.L) {
                    stringified = stringified + "L";
                } else {
                    stringified = stringified + "R";
                }
            }
        }
        return stringified;
    }
}
