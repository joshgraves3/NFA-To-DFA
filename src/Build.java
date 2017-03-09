import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Josh on 2/21/17.
 */
public class Build {

    //keep track of everything
    private HashMap<String,State> statesMap = new HashMap<>();
    private String startState;
    private ArrayList<String> acceptStates = new ArrayList<>();
    private HashSet<String> acceptStatesSet = new HashSet<>();
    private ArrayList<String> inputChars = new ArrayList<>();
    private ArrayList<String > transitions = new ArrayList<>();


    public Build() {

    }

    public void readFile(String file) {

        try {

            //read from the text file
            FileReader reader = new FileReader(file);
            BufferedReader fileReader = new BufferedReader(reader);

            String line;
            String lineArr[];
            String transitionArr[];
            int count = 0;

            //load each row of data into an array
            while ((line = fileReader.readLine()) != null) {

                //get each individual state from NFA
                if (count == 0) {
                    lineArr = line.split("\t");
                    for(String s : lineArr) {
                        statesMap.put(s,new State());
                    }
                }

                //get list of inputs
                if (count == 1) {
                    lineArr = line.split("\t");
                    for(String s : lineArr) {
                        inputChars.add(s);
                    }
                }

                //get start state
                else if (count == 2) {
                    startState = line;
                }

                //put accept states into list
                else if (count == 3) {
                    lineArr = line.split("\t");
                    for(String s : lineArr) {
                        acceptStates.add(s);
                    }
                }

                //load each state into an actual state and give its transitions
                else if (count > 3){
                    lineArr = line.split(",");

                    State currentState = statesMap.get(lineArr[0]);
                    String n = "State" + lineArr[0];
                    transitionArr = lineArr[1].split("=");

                    if (currentState.isEmpty) {
                        currentState = new State(n,transitionArr[0],transitionArr[1]);
                        statesMap.put(lineArr[0],currentState);

                    }
                    else {
                        currentState.addToInput(transitionArr[0],transitionArr[1]);
                    }
                }

                count++;
            }

            System.out.println("States made successfully");
        }
        catch (IOException io) {
            io.printStackTrace();
        }
    }

    public String getEpsTrans(String start) {

        //clean up inputs to avoid nullpointers
        if (start.endsWith(",")){
            start = start.substring(0,start.length()-1);
        }

        //keep a set of what states are already visited when cycling through combined states
        HashSet<String> visited = new HashSet<>();

        //make the start state for the DFA
        StringBuilder newStartState = new StringBuilder(start);
        Stack<String> startStateStack = new Stack<>();
        String current;
        String[] startSplit = start.split(",");

        for(String s: startSplit){

            startStateStack.push(s);
        }

        //cycle through epsilon transitions to make the new state
        while(!startStateStack.isEmpty()) {

            current = startStateStack.pop();
            System.out.println("*******"+current);

            visited.add(current);

            if (statesMap.get(current).input.containsKey("EPS")) {

                for (String s : statesMap.get(current).input.get("EPS")) {

                    if (!visited.contains(s)) {
                        newStartState.append(",");
                        newStartState.append(s);
                        startStateStack.push(s);
                    }
                }
            }
        }

        //make sure inputs are not null
        if (!newStartState.toString().equals("")) {

            //sort the input of new states so that there are no duplicates
            newStartState = sortSet(newStartState);
            String returnStr = newStartState.toString().substring(0,newStartState.toString().length() - 1);
            System.out.println("{" + start + "} EPS: {" + returnStr + "}");
            return returnStr;
        }
        return "";
    }

    public void makeNewStates() {

        //write states to file
        for (Map.Entry<String,State> entry : statesMap.entrySet()) {
            writeToTextFile(entry.getKey() + "\t");
        }

        writeToTextFile("\n");

        //write alphabet to file
        for (String in : inputChars) {
            writeToTextFile(in + "\t");
        }

        writeToTextFile("\n");

        HashSet<String> visited = new HashSet<>();
        String current;

        //run EPS transitions on each state when building new states
        startState = getEpsTrans(startState);

        //write start state to file
        writeToTextFile("{" + startState + "}");
        writeToTextFile("\n");

        visited.clear();

        //prevent duplicates with a set and a stack
        HashSet<String> setOfStates = new HashSet<>();
        StringBuilder newState = new StringBuilder();
        Stack<String> statesVisited = new Stack<>();

        statesVisited.push(startState);

        while(!statesVisited.isEmpty()) {

            current = statesVisited.pop();
            System.out.println("starting with discovery for: {" + current + "}");

            String[] currentStateArray = current.split(",");

            //cycle through each input char to find out which transitions need to be made from
            //the current state and any EPS transitions
            for (String in : inputChars) {
                for (String state : currentStateArray) {
                    if (statesMap.get(state).input.containsKey(in)) {

                        for (String s : statesMap.get(state).input.get(in)) {

                            if (!visited.contains(s)) {
                                visited.add(s);
                                newState.append(s);
                                newState.append(",");

                            }
                        }
                    }
                    visited.clear();
                }

                visited.clear();

                //make and write the new transitions to the file
                if (!newState.toString().equals("")) {

                    newState = sortSet(newState);
                    String endEPSTrans = getEpsTrans(newState.toString());

                    StringBuilder mergedBuilder = sortSet(new StringBuilder(endEPSTrans));
                    String merged = mergedBuilder.toString();
                    merged = merged.substring(0, merged.length() - 1);

                    String key = "{" + current + "},";
                    String value = in + "=" + "{" + merged + "}";
                    System.out.println(key + value + " added to transitions.");
                    String currentTrans = key + value;

                    //add unique transitions to be added to the new file
                    if(!transitions.contains(currentTrans)) {
                        transitions.add(currentTrans);
                    }

                    //add in accept states as anything combined states that contain the initial start state
                    for (String as : acceptStates) {
                        if (merged.contains(as)) {
                            acceptStatesSet.add(merged);
                            System.out.println("Added {" + merged + "} as accept state");
                        }
                    }

                    if (!setOfStates.contains(merged)) {
                        statesVisited.push(merged);
                        setOfStates.add(merged);
                        System.out.println("Added state {" + merged + "} to stack.");
                    }
                }
                newState = new StringBuilder();
            }

        }

        //write accept states
        for (String s : acceptStatesSet) {
            writeToTextFile("{" + s + "}\t");
        }

        writeToTextFile("\n");

        for (String s : transitions) {
            writeToTextFile(s);
            writeToTextFile("\n");
        }

    }

    //method to sort the sets so there are no duplicates
    private StringBuilder sortSet(StringBuilder newState) {
        String[] sorted = newState.toString().split(",");
        Arrays.sort(sorted);

        newState = new StringBuilder();

        for (String s : sorted) {

            newState.append(s);
            newState.append(",");
        }
        return newState;
    }

    //method to write to a text file
    private void writeToTextFile(String line) {

        try{
            FileWriter fw = new FileWriter("new.DFA", true);
            PrintWriter writer = new PrintWriter(fw);
            writer.write(line);

            writer.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
