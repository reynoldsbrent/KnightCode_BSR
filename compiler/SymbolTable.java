package compiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, VariableInfo> variables = new HashMap<>();
    private int nextIndex = 1; // Tracks the next available index for local variables

    // Inner class to hold variable information (type and index)
    private static class VariableInfo {
        String type;
        int index;

        VariableInfo(String type, int index) {
            this.type = type;
            this.index = index;
        }
    }

    // Registers a new variable with its type. Assigns the next available index to it.
    public void register(String name, String type) {
        if (!variables.containsKey(name)) {
            variables.put(name, new VariableInfo(type, nextIndex++));
        }
    }

    // Retrieves the type of a variable by name
    public String getType(String name) {
        VariableInfo info = variables.get(name);
        return (info != null) ? info.type : null;
    }

    // Retrieves the index of a variable by name
    public int getIndex(String name) {
        VariableInfo info = variables.get(name);
        return (info != null) ? info.index : -1;
    }

    // Checks if a variable is declared
    public boolean isDeclared(String name) {
        return variables.containsKey(name);
    }

    @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Symbol Table Contents:\n");
    for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
        String name = entry.getKey();
        VariableInfo info = entry.getValue();
        sb.append("Name: ").append(name)
          .append(", Type: ").append(info.type)
          .append(", Index: ").append(info.index)
          .append("\n");
    }
    return sb.toString();
}
}
