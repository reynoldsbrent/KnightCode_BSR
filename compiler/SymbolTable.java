/**
 * The SymbolTable class manages variable information. It stores variable
 * types and their corresponding indexes which are used for bytecode generation and variable management.
 *
 * @author Brent Reynolds
 * @version 1.0
 * @assignment Assignment 5
 * @course CS322 - Compiler Construction
 * @term Spring 2024
 */
package compiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, VariableInfo> variables = new HashMap<>();
    private int nextIndex = 1; // Tracks the next available index for local variables

    /**
     * Represents information about a variable including its type and index.
     */
    private static class VariableInfo {
        String type; // Type of the variable (INTEGER or STRING)
        int index; // Index of the variable

        /**
         * Constructs a new VariableInfo object.
         *
         * @param type  the type of the variable
         * @param index the index where the variable is stored
         */
        VariableInfo(String type, int index) {
            this.type = type;
            this.index = index;
        }
    }

    /**
     * Registers a new variable in the symbol table with its type. Assigns an index automatically.
     *
     * @param name the name of the variable to register
     * @param type the type of the variable to register
     */
    public void register(String name, String type) {
        if (!variables.containsKey(name)) {
            variables.put(name, new VariableInfo(type, nextIndex++));
        }
    }

    /**
     * Retrieves the type of a variable by its name.
     *
     * @param name the name of the variable
     * @return the type of the variable if it exists, null otherwise
     */
    public String getType(String name) {
        VariableInfo info = variables.get(name);
        return (info != null) ? info.type : null;
    }

    /**
     * Retrieves the index of a variable by its name.
     *
     * @param name the name of the variable
     * @return the index of the variable if it exists, -1 otherwise
     */
    public int getIndex(String name) {
        VariableInfo info = variables.get(name);
        return (info != null) ? info.index : -1;
    }

    /**
     * Checks if a variable with the given name is declared in the symbol table.
     *
     * @param name the name of the variable to check
     * @return true if the variable is declared, false otherwise
     */
    public boolean isDeclared(String name) {
        return variables.containsKey(name);
    }

    /**
    * Returns a string representation of the contents of the symbol table.
    *
    * @return a string detailing all variables and their attributes stored in the symbol table
    */
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
