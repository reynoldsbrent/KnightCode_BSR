package compiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, String> variables = new HashMap<>();

    public void register(String name, String type) {
        variables.put(name, type);
    }

    public String getType(String name) {
        return variables.get(name);
    }

    public boolean isDeclared(String name) {
        return variables.containsKey(name);
    }
}
