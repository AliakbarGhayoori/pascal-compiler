import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;


final class ReturnVals{
    public String type = "";
    public String result = "";
}

final class Variable{
    public int id = 0;
    public String type = "";

    public Variable(int id) {
        this.id = id;
    }
}

public class PascalScanner implements Lexical {
    public List<Character> whiteSpace = Arrays.asList(' ', '\f', '\n', '\t', '\r');
    public List<String> keywords = Arrays.asList("and", "or", "array", "assign", "break", "begin", "continue", "do",
            "else", "end", "false","function","procedure", "if","of", "return", "true", "then", "while", "var");
    public List<String> types = Arrays.asList("string", "real", "integer", "char", "boolean");
    public List<String> qoute = Arrays.asList(".", ":", ";", ",", "^", "&", "*", "/", "%", "~", "+", "-", "(", ")", "[", "]", "=", "<", ">");
    public int index = 0;
    public int lenght = 0;
    public String codeTxt ="";

    // for communicate to parser ...
    public boolean inDCL = false;
    public String tokenValue = "";
    Map<String, Variable> symTable = new HashMap<>();
    // end

    private int last_id = 0;

    ReturnVals returnVals = new ReturnVals();

    public PascalScanner(String path) throws IOException {
        codeTxt = new String(Files.readAllBytes(Paths.get(path)));
        lenght = codeTxt.length();
        index = 0;
    }

    public ReturnVals nextTokenAkbar() {
        int textLen = codeTxt.length();
        String result = "";
        String type = "";


        while (index < textLen){
            if (whiteSpace.contains(codeTxt.charAt(index)))
                index +=1;
            else if (Pattern.matches("--", codeTxt.substring(index, index +2))) {
                index += 2;
                while (index < textLen && !Pattern.matches("\n", codeTxt.substring(index, index + 1))) {
                    index += 1;
                }
            }
            else if (Pattern.matches("<--", codeTxt.substring(index, index +3))){
                index += 3;
                while(index < textLen && !Pattern.matches("-->", codeTxt.substring(index, index +3))){
                    index += 1;
                }
                index += 3;
            } else {
                break;
            }
        }

        if (index == textLen){
            returnVals.type = "$";
            returnVals.result = "$";
            return returnVals;
        }

        if (Pattern.matches("[0-9]", codeTxt.substring(index, index +1))){
            result = result.concat(codeTxt.substring(index, index +1));
            index +=1;

            while (index < textLen && Pattern.matches("[0-9]", codeTxt.substring(index, index +1))) {
                result = result.concat(codeTxt.substring(index, index + 1));
                index += 1;
            }
            Character tmpChar = codeTxt.charAt(index);
            if (tmpChar.equals('.')){
                    type = "real_const";
                    result = result.concat(codeTxt.substring(index, index +1));
                    index += 1;

                    while(index < textLen && Pattern.matches("[0-9]", codeTxt.substring(index, index +1))){
                        result = result.concat(codeTxt.substring(index, index +1));
                        index +=1;
                        if (index == textLen-1){
                            returnVals.type = type;
                            returnVals.result = result;
                            return returnVals;
                        }
                    }
                }
            type = "int_const";
            returnVals.type = type;
            returnVals.result = result;
            return returnVals;
        }


        if (qoute.contains(codeTxt.substring(index, index+1))){
            result = result.concat(codeTxt.substring(index, index+1));
            index += 1;
            if (codeTxt.substring(index-1, index).equals(",")){
                returnVals.type = "`";
                returnVals.result = "`";
                return returnVals;
            }

            if (codeTxt.substring(index-1, index).equals(":") || codeTxt.substring(index-1, index ).equals(">")
            || codeTxt.substring(index-1, index ).equals("<")){
                if (codeTxt.substring(index, index+1).equals("=")) {
                    result = result.concat(codeTxt.substring(index, index + 1));
                    index += 1;
                }
                if (codeTxt.substring(index, index+1).equals(">") && codeTxt.substring(index-1, index).equals("<")){
                    result = result.concat(codeTxt.substring(index, index + 1));
                    index += 1;
                }
            }
            type = result;
            returnVals.type = type;
            returnVals.result = result;
            return returnVals;
        }

        if (Pattern.matches("([a-z]|[A-Z])",codeTxt.substring(index, index +1))){
            type = "id";
            result = result.concat(codeTxt.substring(index, index +1));
            index += 1;

            while (index < textLen && Pattern.matches("([a-z]|[A-Z]|[0-9]|_)", codeTxt.substring(index, index +1))){
                result = result.concat(codeTxt.substring(index, index +1));
                index += 1;
            }

            if (types.contains(result)){
                returnVals.type = "typeID";
            }
            else if (keywords.contains(result)){
                returnVals.type = result;
            }else{
                Variable var = symTable.get(result);
                if (!inDCL && var == null) {
                    // TODO error - not found
                } else if (inDCL && var != null) {
                    // TODO error - duplicate
                } else if (inDCL) {
                    symTable.put(result, new Variable(++last_id));
                }
                returnVals.type = type;
            }
            returnVals.result = result;
            return returnVals;
        }

        if(Pattern.matches("'", codeTxt.substring(index, index +1))){
            index += 1;
            result = result.concat(codeTxt.substring(index, index +1));
            index += 1;
            if(Pattern.matches("'", codeTxt.substring(index, index +1))) {
                index++;
                type = "char";
                returnVals.type = type;
                returnVals.result = result;
                return returnVals;
            }
            else {
                System.out.println("undefined syntax");
            }
        }

        if(Pattern.matches("\"", codeTxt.substring(index, index +1))) {
            index +=1;
            while(index < textLen && !Pattern.matches("\"", codeTxt.substring(index, index +1))){
                result = result.concat(codeTxt.substring(index, index +1));
                index += 1;
            }
            index++;
            type = "string";
            returnVals.type = type;
            returnVals.result = result;
            return returnVals;
        }

        returnVals.type = type;
        returnVals.result = result;
        return returnVals;
    }

    public static void main(String[] args) throws IOException {
        PascalScanner scanner = new PascalScanner("/home/aliakbar/EDU/SUT/term7/compiler/projectJava/src/a.txt");
        ReturnVals tmp = scanner.nextTokenAkbar();
        System.out.println(tmp.result);
        System.out.println(tmp.type);
    }

    @Override
    public String nextToken() {
        ReturnVals ret = nextTokenAkbar();
        tokenValue = ret.result;
        return ret.type;
    }
}