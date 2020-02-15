import com.sun.jdi.connect.Connector;

import java.io.ObjectInputStream;
import java.util.*;

public class PascalCG implements CodeGenerator {
    Stack<Object> seStack = new Stack<>(){
        @Override
        public synchronized Object pop() {
            Object obj;
            try {
                obj = super.pop();
                return obj;
            } catch (Exception e) {
                return null;
            }
        }
    };
    private int counter = 1;
    private PascalScanner scanner;
    private boolean in_global = true;
    private boolean in_argument = false;
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

    private void function_call_end (){
        String argument_str="", arg_type="", arg_id="";
        boolean flag = false;

        String last_arg_id = "", last_arg_type = "";
        while(true) {
            arg_type = (String) seStack.pop();
            arg_id = (String) seStack.pop();
            if (arg_id!=null && arg_id.startsWith("!arg_")) {
                String handle = (flag)? ", ":"";
                arg_id = arg_id.substring(4);
                if (!arg_id.startsWith("reg_")) {
                    arg_id = scanner.symTable.get(arg_id).var_sign + arg_id;
                } else {
                    arg_id = "%" + arg_id;
                }
                argument_str = arg_type+" "+arg_id+handle + argument_str;
            } else {
                if (arg_id!=null) {
                    seStack.push(arg_id);
                }
                arg_id = arg_type;
                break;
            }
            last_arg_id = arg_id;
            last_arg_type = arg_type;
            flag = true;
        }

        if (arg_id.equals("write") && !last_arg_type.equals("string")) {
            generated_code.add("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @."
                    + last_arg_type + ", i32 0, i32 0), " + last_arg_type + " " + last_arg_id + ")");
        } else if(arg_id.equals("write")) {
            generated_code.add("call i32 (i8*, ...) @printf(i8* getelementptr inbounds (" + last_arg_type + ", "
                            + last_arg_type + "* " + last_arg_id + ", i32 0, i32 0)");
        } else if (arg_id.equals("read")) {
            generated_code.add("call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @."
                            + last_arg_type +", i32 0, i32 0), " + last_arg_type + "* " + last_arg_id + ")");
        }

        String type = scanner.symTable.get(arg_id).type;
        String result_id = "reg_"+counter;
        generated_code.add(result_id + " = call "+type+" @"+arg_id+"("+argument_str+")");
        seStack.push(result_id);
        seStack.push(type);
    }

    private void function_dcl_end() {
        generated_code.add("}");
    }

    private void function_dcl () {
        in_argument = false;
        String func_type = change_type(scanner.returnVals.result);
        String arg_type="", func_id = "";
        String argument_str = "";
        boolean flag = false;
        while(true) {
            arg_type = (String) seStack.pop();
            func_id = (String) seStack.pop();
            if (func_id!=null && func_id.startsWith("!arg_")) {
                String handle = (flag)? ", ":"";
                argument_str = arg_type+" %"+func_id.substring(4)+handle + argument_str;
            } else {
                if (func_id!=null) {
                    seStack.push(func_id);
                }
                func_id = arg_type;
                break;
            }
            flag = true;
        }

        if (func_id.equals("main")) {
            generated_code.add("define i32 @main(i1 b, i32 a ) {");
            return;
        }
        scanner.symTable.get(func_id).type = func_type;
        generated_code.add("define "+func_type+" @"+func_id+"("+argument_str+") {");
    }

    private void start_func_dcl() {
        in_argument = true;
    }

    private void argument_var_dcl() {
        String type = (String) seStack.pop();
        String id = (String) seStack.pop();
        seStack.push("!arg_" + id);
        seStack.push(type);
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

        if(in_argument)
            return;
        if (in_global) {
            scanner.symTable.get(id).var_sign = "@";
            generated_code.add("@var" + varID + " = weak global " + type);
        } else
            generated_code.add("%var"+varID+" = alloca "+type);
    }

    private void push_constant() {
        seStack.push(scanner.returnVals.type); // can be "real_const", "int_const", "string", "char"
        seStack.push(scanner.returnVals.result); // result is a string
    }
}