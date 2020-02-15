import java.io.IOException;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        try {
            Stack<String> stack = new Stack<>();
            System.out.println(stack.pop());
            PascalScanner scanner = new PascalScanner(
                    "a.txt");
            PascalCG cg = new PascalCG(scanner);
            Parser parser = new Parser(scanner, cg,
                    "table.npt", true);
            parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
