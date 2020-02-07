import java.util.LinkedList;
import java.util.Stack;

public class PascalCG implements CodeGenerator {
    Stack<Object> seStack = new Stack<>();
    private PascalScanner scanner;

    public PascalCG(Lexical lexical) {
        this.scanner = (PascalScanner) lexical;
    }

    @Override
    public void doSemantic(String sem) {
        switch (sem) {
            case "push_constant":
                push_constant();
        }
    }

    private void push_constant() {
        seStack.push(scanner.returnVals);
    }
}
