import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            PascalScanner scanner = new PascalScanner(
                    "a.txt");
            PascalCG cg = new PascalCG();
            Parser parser = new Parser(scanner, cg,
                    "table.npt", true);
            parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
