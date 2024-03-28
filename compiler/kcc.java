package compiler;

public class kcc {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Make sure to enter command line arguments like this: java compiler/kcc inputFile outputFile");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];
        
        
    }
}
