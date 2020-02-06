import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;


final class ReturnVals{
    public String type = "";
    public String result = "";
}

public class PascalScanner implements Lexical {
    public ArrayList<String> whiteSpace = new ArrayList<String>();
    List<String> keywords = Arrays.asList("array", "assign", "break", "begin", "continue", "do", "else", "end", "false","function","procedure", "if","of", "return", "true", "while", "var");
    List<String> types = Arrays.asList("string", "real", "integer", "char", "boolean");
    public int index = 0;
    public int lenght = 0;
    public String codeTxt ="";

    // for communicate to parser ...
    public boolean inDCL = false;
    public String tokenValue = "";
    Map<String, Integer> symTable = new HashMap<>();
    // end

    private int last_id = 0;

    ReturnVals returnVals = new ReturnVals();

    public PascalScanner(String path) throws IOException {
        codeTxt = new String(Files.readAllBytes(Paths.get(path)));
        lenght = codeTxt.length();
        index = 0;
    }

    public void fillWhiteSpace(){
        whiteSpace.add("\n");
        whiteSpace.add("\t");
        whiteSpace.add("\f");
        whiteSpace.add("\r");
    }




    public ReturnVals nextTokenAkbar() {
        int textLen = codeTxt.length();
        String result = "";
        String type = "";


        while (index < textLen & whiteSpace.contains(codeTxt.charAt(index))){
            index +=1;
        }
        if (index == textLen){
            return returnVals;
        }

        while (true){
            if (index == textLen-1){
                break;
            }
            if (Pattern.matches("[0-9]", codeTxt.substring(index, index +1))){
                result = result.concat(codeTxt.substring(index, index +1));
                index +=1;

                while (Pattern.matches("[0-9]", codeTxt.substring(index, index +1))) {
                    result = result.concat(codeTxt.substring(index, index + 1));
                    index += 1;
                }
                Character tmpChar = codeTxt.charAt(index);
                if (tmpChar.equals('.')){
                        type = "real";
                        result = result.concat(codeTxt.substring(index, index +1));
                        index += 1;

                        while(Pattern.matches("[0-9]", codeTxt.substring(index, index +1))){
                            result = result.concat(codeTxt.substring(index, index +1));
                            index +=1;
                            if (index == textLen-1){
                                returnVals.type = type;
                                returnVals.result = result;
                                return returnVals;
                            }
                        }
                    }
                type = "int";
                returnVals.type = type;
                returnVals.result = result;
                return returnVals;
                }

            if (Pattern.matches("([a-z]|[A-Z])",codeTxt.substring(index, index +1))){
                type = "id";
                result = result.concat(codeTxt.substring(index, index +1));
                index += 1;

                while (Pattern.matches("([a-z]|[A-Z]|[0-9]|_)", codeTxt.substring(index, index +1))){
                    result = result.concat(codeTxt.substring(index, index +1));
                    index += 1;
                }

                if (types.contains(result)){
                    returnVals.type = "typeID";
                }
                else if (keywords.contains(result)){
                    returnVals.type = result;
                }else{
                    Integer index = symTable.get(result);
                    if (!inDCL && index == null) {
                        // error - not found
                    } else if (inDCL && index != null) {
                        // error - duplicate
                    } else if (inDCL) {
                        symTable.put(result, ++last_id);
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
                while(!Pattern.matches("\"", codeTxt.substring(index, index +1))){
                    result = result.concat(codeTxt.substring(index, index +1));
                    index += 1;
                }
                index++;
                type = "string";
                returnVals.type = type;
                returnVals.result = result;
                return returnVals;
            }



            if(Pattern.matches("--", codeTxt.substring(index, index +2))) {
                index +=2;
                while(!Pattern.matches("\n", codeTxt.substring(index, index +1))){
                    result = result.concat(codeTxt.substring(index, index +1));
                    index += 1;
                }
                type = "comment";
                returnVals.type = type;
                returnVals.result = result;
                return returnVals;
            }


            if (Pattern.matches("<--", codeTxt.substring(index, index +3))){
                type = "comment";
                index += 3;
                while(!Pattern.matches("-->", codeTxt.substring(index, index +3))){
                    result = result.concat(codeTxt.substring(index, index +1));
                    index += 1;
                }
                index += 3;
                returnVals.type = type;
                returnVals.result = result;
                return returnVals;
            }
            }
        returnVals.type = type;
        returnVals.result = result;
        return returnVals;
    }

    public static void main(String[] args) throws IOException {
        PascalScanner scanner = new PascalScanner("/home/aliakbar/EDU/SUT/term7/compiler/projectJava/src/a.txt");
        scanner.fillWhiteSpace();
        System.out.println(scanner.nextTokenAkbar().result);
        System.out.println(scanner.nextTokenAkbar().type);
    }

    @Override
    public String nextToken() {
        ReturnVals ret = nextTokenAkbar();
        tokenValue = ret.result;
        return ret.type;
    }
}