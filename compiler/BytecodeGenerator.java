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

    public void startClass(String name) {
        this.className = name.replace(".class", "").replaceAll("/", ".");
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_8, ACC_PUBLIC + ACC_SUPER, this.className, null, "java/lang/Object", null);
        initConstructor();
    }

    private void initConstructor() {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0); 
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1); 
        mv.visitEnd();
    }

    public void startMainMethod() {
        this.methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
    }

    public void finalizeMainMethod() {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(-1, -1); 
        methodVisitor.visitEnd();
    }

    public void loadVariable(int index, String type) {
        if ("INTEGER".equals(type)) {
            methodVisitor.visitVarInsn(ILOAD, index);
        } else if ("STRING".equals(type)) {
            methodVisitor.visitVarInsn(ALOAD, index);
        } else {
            throw new IllegalArgumentException("Unsupported variable type: " + type);
        }
    }

    public void storeVariable(int index, String type) {
        if ("INTEGER".equals(type)) {
            methodVisitor.visitVarInsn(ISTORE, index);
        } else if ("STRING".equals(type)) {
            methodVisitor.visitVarInsn(ASTORE, index);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        //System.out.println("Storing result of expression in local variable table as " + type);
    }
    

    public void storeString(int index, String value) {
        // Push the string value onto the stack
        methodVisitor.visitLdcInsn(value);
        // Store the string at the local variable index
        methodVisitor.visitVarInsn(ASTORE, index);
    }

    public void pushValue(int value) {
        methodVisitor.visitLdcInsn(value);
    }

    public void printString(String text) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn(text);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    public void printStringVariable(int index) {
        // Load the PrintStream object for System.out onto the stack
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // Load the string from the local variable index onto the stack
        methodVisitor.visitVarInsn(ALOAD, index);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    public void printInteger() {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitInsn(SWAP); // Adjust stack order for printStream.println method call
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }

    public void printIntegerVariable(int index) {
        // Load the PrintStream object for System.out onto the stack
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // Load the integer from the local variable index onto the stack
        methodVisitor.visitVarInsn(ILOAD, index);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }
    

    public void addIntegers() {
        methodVisitor.visitInsn(IADD);
    }

    public void subtractIntegers() {
        methodVisitor.visitInsn(ISUB);
    }

    public void multiplyIntegers() {
        methodVisitor.visitInsn(IMUL);
    }

    public void divideIntegers() {
        methodVisitor.visitInsn(IDIV);
    }

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
    
    public void label(Label label) {
        methodVisitor.visitLabel(label);
    }

    public void goTo(Label label) {
        methodVisitor.visitJumpInsn(GOTO, label);
    }

    public void ifFalseJump(Label falseLabel) {
        methodVisitor.visitJumpInsn(IFEQ, falseLabel);
    }

    public void compareGreaterThan(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPGT, label);
    }

    public void compareGreaterThanOrEqual(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPGE, label);
    }
    
    public void compareLessThan(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPLT, label);
    }
    
    public void compareLessThanOrEqual(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPLE, label);
    }

    public void compareEquals(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPEQ, label);
    }
    
    public void compareNotEquals(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPNE, label);
    }
    

    public byte[] getBytecode() {
        classWriter.visitEnd(); 
        return classWriter.toByteArray();
    }
}
