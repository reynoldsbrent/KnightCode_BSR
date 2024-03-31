package compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BytecodeGenerator implements Opcodes {
    private ClassWriter classWriter = null; // Initialized later in startClass
    private MethodVisitor methodVisitor = null;
    private String className = null; // Will be set in startClass

    public BytecodeGenerator() {
        // Constructor remains empty for now, as setup requiring class name is deferred to startClass
    }
    public void initConstructor() {
        // Generates the default constructor
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0); // Load "this"
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // Call the constructor of super class (Object)
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        
    }
    public void startClass(String name) {
        this.className = name.replace(".class", "").replace("/", ".");
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // Initiate class with ACC_PUBLIC and specify it extends java/lang/Object
        classWriter.visit(V1_8, ACC_PUBLIC + ACC_SUPER, this.className, null, "java/lang/Object", null);
        initConstructor(); // Initialize the default constructor
        startMainMethod(); // Optionally start the main method here, if your class structure assumes it
    }


    public void startMainMethod() {
        // Start the main method. 
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
    }

    public void endMainMethod() {
        // Ends the main method
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(-1, -1); // Auto-compute stack and local variable size
        methodVisitor.visitEnd();
    }

    public byte[] getBytecode() {
        return classWriter.toByteArray();
    }

    // Method for adding an integer variable
    public void addIntegerVariable(String name, int value) {
        // Use methodVisitor to visitVarInsn and visitIntInsn
    }

    // Additional methods for handling arithmetic, control structures, etc.
}

