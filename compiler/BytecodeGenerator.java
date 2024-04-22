/**
 * This class is responsible for generating bytecode for the KnightCode programming language.
 * It uses the ASM library to generate Java bytecode dynamically based on the parsed
 * source code of KnightCode.
 *
 * @author Brent Reynolds
 * @version 1.0
 * @assignment Assignment 5
 * @course CS322 - Compiler Construction
 * @term Spring 2024
 */
package compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BytecodeGenerator implements Opcodes {
    private ClassWriter classWriter;
    private MethodVisitor methodVisitor;
    private String className;

    
    public BytecodeGenerator() {
        
    }

    /**
     * Starts the bytecode generation for a class with the specified name.
     * This method sets up the class header using ASM and initializes a constructor.
     *
     * @param name the name of the class to generate bytecode for
     */
    public void startClass(String name) {
        this.className = name.replace(".class", "").replaceAll("/", ".");
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_8, ACC_PUBLIC + ACC_SUPER, "output/" + this.className, null, "java/lang/Object", null);
        initConstructor();
    }

    /**
     * Initializes a constructor for the class.
     */
    private void initConstructor() {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0); 
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1); 
        mv.visitEnd();
    }

    /**
     * Starts the main method for the class.
     */
    public void startMainMethod() {
        this.methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
    }

    /**
     * Finalizes the main method.
     */
    public void finalizeMainMethod() {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(-1, -1); 
        methodVisitor.visitEnd();
    }

    /**
     * Loads a variable onto the stack based on its type.
     *
     * @param index the index of the variable in the local variable table
     * @param type the type of the variable, currently supports "INTEGER" and "STRING"
     */
    public void loadVariable(int index, String type) {
        if ("INTEGER".equals(type)) {
            methodVisitor.visitVarInsn(ILOAD, index);
        } else if ("STRING".equals(type)) {
            methodVisitor.visitVarInsn(ALOAD, index);
        } else {
            throw new IllegalArgumentException("Unsupported variable type: " + type);
        }
    }

    /**
     * Stores a variable from the stack based on its type.
     *
     * @param index the index of the variable in the local variable table
     * @param type the type of the variable, currently supports "INTEGER" and "STRING"
     */
    public void storeVariable(int index, String type) {
        if ("INTEGER".equals(type)) {
            methodVisitor.visitVarInsn(ISTORE, index);
        } else if ("STRING".equals(type)) {
            methodVisitor.visitVarInsn(ASTORE, index);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
    

    /**
     * Stores a string constant into a specified local variable.
     *
     * @param index the local variable index where the string should be stored
     * @param value the string value to store
     */
    public void storeString(int index, String value) {
        // Push the string value onto the stack
        methodVisitor.visitLdcInsn(value);
        // Store the string at the local variable index
        methodVisitor.visitVarInsn(ASTORE, index);
    }

    /**
     * Pushes an integer constant onto the stack.
     *
     * @param value the integer value to push onto the stack
     */
    public void pushValue(int value) {
        methodVisitor.visitLdcInsn(value);
    }

    /**
     * Prints a string to the console. 
     *
     * @param text the text to be printed
     */
    public void printString(String text) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn(text);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    /**
     * Prints the content of a string variable to the console. Loads a string from the local variable
     * index and prints it.
     *
     * @param index the index of the string variable in the local variable table
     */
    public void printStringVariable(int index) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitVarInsn(ALOAD, index);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    /**
    * Prints an integer to the console.
    */
    public void printInteger() {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitInsn(SWAP); 
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }

    /**
    * Prints the content of an integer variable to the console. Loads an integer from the specified local variable
    * index and prints it.
    *
    * @param index the index of the integer variable in the local variable table
    */
    public void printIntegerVariable(int index) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitVarInsn(ILOAD, index);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }
    
    /**
     * Adds two integers from the stack and pushes the result back to the stack.
    */
    public void addIntegers() {
        methodVisitor.visitInsn(IADD);
    }

    /**
    * Subtracts the top integer from the second top integer on the stack and pushes the result back to the stack.
    */
    public void subtractIntegers() {
        methodVisitor.visitInsn(ISUB);
    }

    /**
    * Multiplies two integers from the stack and pushes the result back to the stack.
    */
    public void multiplyIntegers() {
        methodVisitor.visitInsn(IMUL);
    }

    /**
    * Divides the second top integer by the top integer on the stack and pushes the result back to the stack.
    */
    public void divideIntegers() {
        methodVisitor.visitInsn(IDIV);
    }

    /**
    * Compares two integers from the stack for greater than condition and pushes the boolean result (0 or 1) back to the stack.
    */
    public void compareGreaterThan() {
        Label trueLabel = new Label();
        Label endLabel = new Label();
    
        methodVisitor.visitJumpInsn(IF_ICMPGT, trueLabel);
        methodVisitor.visitInsn(ICONST_0); // false
        methodVisitor.visitJumpInsn(GOTO, endLabel);
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(ICONST_1); // true
        methodVisitor.visitLabel(endLabel);
    }
    
    /**
    * Compares two integers from the stack for less than condition and pushes the boolean result (0 or 1) back to the stack.
    */
    public void compareLessThan() {
        Label trueLabel = new Label();
        Label endLabel = new Label();
    
        methodVisitor.visitJumpInsn(IF_ICMPLT, trueLabel);
        methodVisitor.visitInsn(ICONST_0); // false
        methodVisitor.visitJumpInsn(GOTO, endLabel);
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(ICONST_1); // true
        methodVisitor.visitLabel(endLabel);
    }
    
    /**
    * Compares two integers from the stack to see if they are equal and pushes the boolean result (0 or 1) back to the stack.
    */
    public void compareEquals() {
        Label trueLabel = new Label();
        Label endLabel = new Label();
    
        methodVisitor.visitJumpInsn(IF_ICMPEQ, trueLabel);
        methodVisitor.visitInsn(ICONST_0); // false
        methodVisitor.visitJumpInsn(GOTO, endLabel);
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(ICONST_1); // true
        methodVisitor.visitLabel(endLabel);
    }
    
    /**
    * Compares two integers from the stack to see if they are not equal and pushes the boolean result (0 or 1) back to the stack.
    */
    public void compareNotEquals() {
        Label trueLabel = new Label();
        Label endLabel = new Label();
    
        methodVisitor.visitJumpInsn(IF_ICMPNE, trueLabel);
        methodVisitor.visitInsn(ICONST_0); // false
        methodVisitor.visitJumpInsn(GOTO, endLabel);
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(ICONST_1); // true
        methodVisitor.visitLabel(endLabel);
    }
    
    /**
     * Reads an integer and stores it at the specified index.
     *
     * @param index the index where the read integer will be stored
     */
    public void readInteger(int index) {
        // Instantiate Scanner System.in
        methodVisitor.visitTypeInsn(NEW, "java/util/Scanner");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        
        // Call Scanner.nextInt()
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
        
        // Store the result in the local variable table
        methodVisitor.visitVarInsn(ISTORE, index);
    }
    
    /**
     * Reads a string and stores it at the specified index.
     *
     * @param index the index where the read string will be stored
     */
    public void readString(int index) {
        // Instantiate Scanner System.in
        methodVisitor.visitTypeInsn(NEW, "java/util/Scanner");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        
        // Call Scanner.nextLine()
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
        
        // Store the result in the local variable table
        methodVisitor.visitVarInsn(ASTORE, index);
    }
    
    /**
     * Defines a label at the location in the method where this is called.
     *
     * @param label the label to define
     */
    public void label(Label label) {
        methodVisitor.visitLabel(label);
    }

    /**
     * Jumps to the specified label within the method. 
     *
     * @param label the label to jump to
     */
    public void goTo(Label label) {
        methodVisitor.visitJumpInsn(GOTO, label);
    }

    /**
     * Jumps to a specified label if the top of the stack evaluates to false.
     *
     * @param falseLabel the label to jump to if the top of the stack is false
     */
    public void ifFalseJump(Label falseLabel) {
        methodVisitor.visitJumpInsn(IFEQ, falseLabel);
    }

    /**
     * Compares two integers on the stack for greater than and jumps to the specified label if true.
     *
     * @param label the label to jump to if the comparison is true
     */
    public void compareGreaterThan(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPGT, label);
    }

    /**
     * Compares two integers on the stack for greater than or equal to and jumps to the specified label if true.
     *
     * @param label the label to jump to if the comparison is true
     */
    public void compareGreaterThanOrEqual(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPGE, label);
    }
    
    /**
     * Compares two integers on the stack for less than and jumps to the specified label if true.
     *
     * @param label the label to jump to if the comparison is true
     */
    public void compareLessThan(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPLT, label);
    }
    
    /**
     * Compares two integers on the stack for less than or equal to and jumps to the specified label if true.
     *
     * @param label the label to jump to if the comparison is true
     */
    public void compareLessThanOrEqual(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPLE, label);
    }

    /**
     * Compares two integers on the stack to see if they are equal and jumps to the specified label if true.
     *
     * @param label the label to jump to if the comparison is true
     */
    public void compareEquals(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPEQ, label);
    }
    
    /**
     * Compares two integers on the stack to see if they are not equal and jumps to the specified label if true.
     *
     * @param label the label to jump to if the comparison is true
     */
    public void compareNotEquals(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPNE, label);
    }
    

    /**
     * Generates and returns the bytecode for the class that has been built by this BytecodeGenerator instance.
     *
     * @return the array of bytes representing the bytecode of the class
     */
    public byte[] getBytecode() {
        classWriter.visitEnd(); 
        return classWriter.toByteArray();
    }
}
