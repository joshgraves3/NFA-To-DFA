import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Josh on 2/21/17.
 */
public class State {

    public HashMap<String, ArrayList<String>> input = new HashMap<>();
    public String name;
    public boolean isEmpty;

    public State() {
        this.isEmpty = true;
    }

    public State(String name, String inputVar, String nextState) {
        this.name = name;
        ArrayList<String> tranStates = new ArrayList<>();
        tranStates.add(nextState);
        this.input.put(inputVar,tranStates);
        this.isEmpty = false;
    }

    //add new inputs for transitions
    public void addToInput(String inputVar, String nextState) {

        if (this.input.get(inputVar) == null) {
            ArrayList<String> tranStates = new ArrayList<>();
            tranStates.add(nextState);
            this.input.put(inputVar,tranStates);
        }
        else {
            this.input.get(inputVar).add(nextState);
        }
    }

}
