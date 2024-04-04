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
        // Placeholder for potential future initialization
    }

    public void startClass(String name) {
        this.className = name.replace(".class", "").replaceAll("/", ".");
        // COMPUTE_FRAMES flag to automatically compute stack map frames and local variable tables.
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_8, ACC_PUBLIC + ACC_SUPER, this.className, null, "java/lang/Object", null);
        initConstructor();
    }

    private void initConstructor() {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0); // Load "this"
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1); // Auto-compute stack and locals
        mv.visitEnd();
    }

    public void startMainMethod() {
        this.methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
        // Start of method bytecode generation goes here
    }

    public void finalizeMainMethod() {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(-1, -1); // ASM computes the frame and stack sizes.
        methodVisitor.visitEnd();
    }

    public void loadVariable(int index) {
        methodVisitor.visitVarInsn(ILOAD, index);
    }

    public void storeVariable(int index) {
        // Store the top of the stack in the local variable at `index`.
        // This corresponds to the result of the expression left on the stack by `evaluateExpression`.
        methodVisitor.visitVarInsn(ISTORE, index);
    }

    public void pushValue(int value) {
        methodVisitor.visitLdcInsn(value);
    }

    public void printString(String text) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn(text);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    public void printInteger() {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitInsn(SWAP); // Adjust stack order for printStream.println method call
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

    // Conditional jump based on integer comparison equal
    public void ifIntegersEqual(Label label) {
        methodVisitor.visitJumpInsn(IF_ICMPEQ, label);
    }

    // Label definition and placement
    public void label(Label label) {
        methodVisitor.visitLabel(label);
    }

    // Unconditional jump to a label
    public void goTo(Label label) {
        methodVisitor.visitJumpInsn(GOTO, label);
    }

    public byte[] getBytecode() {
        classWriter.visitEnd(); // Complete the class
        return classWriter.toByteArray();
    }

    // Additional utility methods can be added as needed.
}
