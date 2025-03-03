

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Info> classT;
    private final Map<String, Info> subroutineT;
    private int staticIndex;
    private int fieldIndex;
    private int argIndex;
    private int varIndex;

    //Creating an Info object
    private static class Info {
        String type;
        String kind;
        int index;

        public Info(String type, String kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    // Constructor
    public SymbolTable() {
        classT = new HashMap<>();
        subroutineT = new HashMap<>();
        staticIndex = 0;
        fieldIndex = 0;
        reset();
    }

    public void reset() {
        argIndex = 0;
        varIndex = 0;
        subroutineT.clear();
    }

    public void define(String name, String type, String kind) {
        switch (kind) {
            case "static" -> classT.put(name, new Info(type, kind, staticIndex++));
            case "field" -> classT.put(name, new Info(type, "this", fieldIndex++));
            case "arg" -> subroutineT.put(name, new Info(type, "argument", argIndex++));
            case "var" -> {
                subroutineT.put(name, new Info(type, "local", varIndex++));
            }
            default -> throw new IllegalArgumentException("This kind isn't valid - " + kind);
        }
    }

    public int varCount(String kind) {
        int count = switch (kind) {
            case "static" -> staticIndex;
            case "field" -> fieldIndex;
            case "arg" -> argIndex;
            case "var" -> varIndex;
            default -> throw new IllegalArgumentException("This kind isn't valid - " + kind);
        };
        return count;
    }

    public String kindOf(String name) {
        //Check if subroutine table contains the name and returns its kind if so
        if (subroutineT.containsKey(name)) {
            return subroutineT.get(name).kind;

        //Check if class table contains the name and returns its kind if so
        } else if (classT.containsKey(name)) {
            return classT.get(name).kind;

        //Returns NONE if there is no such name in the tables
        } else {
            return "NONE";
        }
    }

    public String typeOf(String name) {
        //Check if subroutine table contains the name and returns its type if so
        if (subroutineT.containsKey(name)) {
            return subroutineT.get(name).type;

        //Check if class table contains the name and returns its type if so
        } else if (classT.containsKey(name)) {
            return classT.get(name).type;

        } else {
            throw new IllegalArgumentException("No such name in the tables - " + name);
        }
    }

    public int indexOf(String name) {
        //Check if subroutine table contains the name and returns its index if so
        if (subroutineT.containsKey(name)) {
            return subroutineT.get(name).index;

            //Check if class table contains the name and returns its index if so
        } else if (classT.containsKey(name)) {
            return classT.get(name).index;

        } else {
            throw new IllegalArgumentException("No such name in the tables - " + name);
        }
    }

    public Map<String, Info> getClassTable() {
        return classT;
    }

    public Map<String, Info> getSubroutineTable() {
        return subroutineT;
    }
}