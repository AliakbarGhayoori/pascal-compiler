import java.util.*;

public class PascalCG implements CodeGenerator {
    Stack<Object> seStack = new Stack<>();
    private PascalScanner scanner;
    private boolean in_global = true;
    private List<String> generated_code = Arrays.asList("@.i32 = private unnamed_addr constant [3 x i8] c\"%d\\00\", ",
            "align 1", "declare i32 @scanf(i8*, ...)",
            "declare i32 @printf(i8*, ...)");

    public PascalCG(Lexical lexical) {
        this.scanner = (PascalScanner) lexical;
    }

    private String change_type(String input){
        switch (input) {
            case "real":
                return "float";
            case "boolean":
                return "i1";
            case "integer":
                return "i32";
            case "character":
                return "i8";
            case "string":
                return "i8*";
            default:
                return "";

        }
    }

    @Override
    public void doSemantic(String sem) {
        System.out.println("my print ... " + sem);
        switch (sem) {
            case "push_constant":
                push_constant();
                break;
            case "push_id":
                seStack.push(scanner.returnVals.result);
                break;
            case "dcl_var":
                dcl_var();
                break;
            case "push_type_arr_dcl":
                seStack.push("arr_" + scanner.returnVals.result);
                break;
            case "assign_val":
                assign_val();
        }
    }

    private void assign_val() {
        if (in_global) {
            System.out.println("Error ---- assignment in global is not permitted");
            return;
        }

        String expr_result = (String) seStack.pop();
        String type = (String) seStack.pop();
        String var_name = "var"+seStack.pop();
        String var_type = (String)seStack.pop();
        seStack.push(null);
        seStack.push(null);
        
        generated_code.add("store "+type+" %"+expr_result+", "+var_type+" "+var_name);
    }

    private void dcl_var() {
        String id = (String) seStack.pop();
        int varID = scanner.symTable.get(id).id;
        String type = change_type(scanner.returnVals.result);
        seStack.push(type);
        seStack.push(varID);

        if (type.equals(""))
            System.out.println("Error ---- type is not correct!");
        if (!scanner.symTable.get(id).type.equals(""))
            System.out.println("Error ---- double definition");

        scanner.symTable.get(id).type = type;

        if (in_global)
            generated_code.add("@var"+varID+" = weak global "+type);
        else
            generated_code.add("%var"+varID+" = alloca "+type);
    }

    private void push_constant() {
        seStack.push(scanner.returnVals.type); // can be "real_const", "int_const", "string", "char"
        seStack.push(scanner.returnVals.result); // result is a string
    }
}
