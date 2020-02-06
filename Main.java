import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            PascalScanner scanner = new PascalScanner(
                    "/home/aliakbar/EDU/SUT/term7/compiler/projectJava/pascal-compiler/a.txt");
            PascalCG cg = new PascalCG();
            Parser parser = new Parser(scanner, cg,
                    "/home/aliakbar/EDU/SUT/term7/compiler/projectJava/pascal-compiler/table.npt");
            parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
