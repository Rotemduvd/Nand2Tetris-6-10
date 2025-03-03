import java.util.HashMap;
import java.util.Map;
public class SymbolTable {

static final Map<String, Integer> map = new HashMap<>();

    //Initialize the map with the basics
    public SymbolTable(){
        //Insert R0->R15
        for (int i =0 ; i<16; i++){
            String key = "R" + i;
            map.put(key, i);
        }
        //Insert the rest
        map.put("SCREEN",16384);
        map.put("KBD",24576);
        map.put("SP",0);
        map.put("LCL",1);
        map.put("ARG",2);
        map.put("THIS",3);
        map.put("THAT",4);
    }

    //add key and val to the map
    public static void addEntry(String key, int value){
        map.put(key, value);
    }

    //checks whether the map contains a specific key
    public static boolean contains(String symbol){
        return map.containsKey(symbol);
    }

    //gets the value (address) of the string key
    public static int getAddress(String symbol){
        return map.get(symbol);
    }

}
