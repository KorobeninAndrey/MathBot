package mathBot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Pattern;

public class MathBotServer extends Thread {
    private Socket socket;

    private static final ArrayList<Character> mathSigns = new ArrayList<>();
    static {
        mathSigns.add('*');
        mathSigns.add('/');
        mathSigns.add('+');
    }
    private static HashMap<String, Double> memoryMap = new HashMap<>();
    public MathBotServer(Socket socket) {
         this.socket = socket;
    }


    public void run(){
        try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                String text = "";
                do {
                    writer.println("Server: input your request" );
                    Optional<String> line = reader.lines().findFirst();
                    //remove all spaces
                    if (line.isPresent()){
                        text = line.get().replaceAll("\\s+", "");
                    }
                    try {
                        if (text.length() == 0) {
                            throw new Exception("Server: input your request");
                        }
                        switch (text) {
                            case "hello":
                                writer.println("world");
                                break;
                            case "help":
                                writer.println("command list");
                                writer.println("for echo server type \"hello\"");
                                writer.println("for get author type \"author\"");
                                writer.println("for close connection type \"bye\"");
                                writer.println("for put value in memory type \"x = value\"");
                                writer.println("for remove value from memory type \"remove\"");
                                writer.println("for arithmetic evaluation use math signs *, /, +, -, parentheses, and values you put in memory");
                                break;
                            case "author":
                                writer.println("Andrey Korobenin");
                                break;
                            case "bye":
                                writer.println("bye");
                                break;
                            default:
                                if (text.length() > 5 && "remove".equals(text.substring(0, 6))) {
                                    String key = text.substring(6).trim();
                                    if (memoryMap.remove(key) > 0){
                                        writer.println("Element successfully removed from memory");
                                    }
                                    else{
                                        writer.println("Element " + key + " doesn't exists in memory. You have to assign it first");
                                    }
                                }
                                //check if input is correct
                                if (!checkInput(text)) {
                                    throw new Exception("Syntax Error");
                                }
                                //try to calculate
                                text = calculateInput(text);
                                //check if input has "=", then assign value
                                if (text.contains("=")) {
                                    //check if input has value
                                    String key = text.substring(0, text.indexOf("="));
                                    memoryMap.put(key, Double.parseDouble(text.substring(text.indexOf("=") + 1)));
                                    writer.println("Value successfully assigned");
                                } else {
                                    writer.println(text);
                                }
                                break;
                        }
                    } catch (Exception ex){
                        writer.println(ex.getMessage());
                        writer.println("Server: input your request");
                    }
                } while (!text.equals("bye"));
                socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
     }

    private static boolean checkInput(String text){
        return text.charAt(0) != '='
        && text.charAt(0) != '+'
        && text.charAt(0) != '*'
        && text.charAt(0) != '/'
        && text.charAt(text.length() - 1) != '='
        && text.charAt(text.length() - 1) != '+'
        && text.charAt(text.length() - 1) != '-'
        && checkParentheses(text)
        && (text.indexOf('=') < 0 || (text.indexOf('=') > 0 && text.substring(0, text.indexOf('=')).indexOf('-') < 0
        && text.substring(0, text.indexOf('=')).indexOf('+') < 0
        && text.substring(0, text.indexOf('=')).indexOf('*') < 0
        && text.substring(0, text.indexOf('=')).indexOf('/') < 0))
        && !text.contains("**")
        && !text.contains("//")
        && !text.contains("*/")
        && !text.contains("/*")
        && !text.contains("+*")
        && !text.contains("-/")
        && !text.contains("/+")
        && !text.contains("/-")
        && !text.contains("++")
        && !text.contains("--")
        && !text.contains("+-")
        && !text.contains("-+")
        ;
    }
    private static boolean checkParentheses(String text){
        int cnt = 0;
        for (int i = 0; i < text.length(); i++){
            if (text.charAt(i) == '('){
                cnt++;
            }
            if (text.charAt(i) == ')'){
                cnt--;
            }
            if (cnt < 0){
                return false;
            }
        }
        return cnt == 0;
    }
    private static String calculateInput(String text) throws Exception{
        //calculate for parentheses first
        //start from the most right
        int i = text.length() - 1;
        while (i >= 0){
            if (text.charAt(i) == '('){
                for (int j = i + 1; j < text.length(); j++){
                    if (text.charAt(j) == ')'){
                        //try to calculate
                        String tmp = calculate(text.substring(i + 1, j));
                        //replace all occurrences with result
                        text = text.replaceAll(Pattern.quote(text.substring(i, j + 1)), tmp);
                    }
                }
            }
            i--;
        }
        //after try to calculate rest input and remove square parentheses (what means negative value)
        return calculate(text).replaceAll(Pattern.quote("["), "").replaceAll(Pattern.quote("]"), "");
    }
    private static String calculate(String text) throws Exception{
        //check if we have text in memory
        if (memoryMap.containsKey(text)){
            double value = memoryMap.get(text);
            if (value < 0){
                text = "[" + value + "]";
            }
            else {
                text = String.valueOf(value);
            }
        } else {
            //calculate for math signs in order *,/, +
            for (Character mathSign : mathSigns) {
                int mathSignPos = text.indexOf(mathSign);
                // calculate if math sign exists
                while (mathSignPos > 0) {
                    //get left element borders for math sign
                    int[] leftElementBorders = getLeftElementBorders(mathSignPos, text);
                    //get left element
                    double leftValue = getElement(text, leftElementBorders);
                    //get right element borders for math sign
                    int[] rightElementBorders = getRightElementBorders(mathSignPos, text);
                    //get right element
                    double rightValue = getElement(text, rightElementBorders);
                    double value;
                    //calculate for math sign
                    switch (mathSign) {
                        case '*':
                            value = leftValue * rightValue;
                            break;
                        case '/': {
                            if (rightValue == 0) {
                                throw new Exception("Division by zero");
                            }
                            value = leftValue / rightValue;
                        }
                        break;
                        case '+':
                            value = leftValue + rightValue;
                            break;
                        default:
                            value = leftValue - rightValue;
                    }
                    //add square parentheses if result is negative and replace all occurrences with value
                    //else replace all occurrences with value
                    if (value < 0) {
                        text = text.replaceAll(Pattern.quote(text.substring(leftElementBorders[0], rightElementBorders[1])), "[" + value + "]");
                    } else {
                        text = text.replaceAll(Pattern.quote(text.substring(leftElementBorders[0], rightElementBorders[1])), String.valueOf(value));
                    }
                    mathSignPos = text.indexOf(mathSign);
                }
            }
            // calculate for subtraction
            //if input has minus sign
            // and it is not calculated minus(doesn't have square parentheses)
            // or it is first char in input
            if ((text.indexOf('-') > 0 && text.charAt(text.indexOf('-') - 1) != '[') || text.indexOf('-') == 0) {
                for (int i = 0; i < text.length(); i++) {
                    //if current char is "-"
                    // and it is not calculated minus(doesn't have square parentheses)
                    // or it is first char in input
                    if (text.charAt(i) == '-' && (i == 0 || text.charAt(i - 1) != '[')) {
                        int start = i;
                        i++;
                        //try to find right element
                        while (i < text.length() && (!mathSigns.contains(text.charAt(i)) && text.charAt(i) != '-')) {
                            i++;
                        }
                        //replace occurrences with +[] if first char of right element is "("
                        //or replace occurrences with []
                        if (start > 0 && text.charAt(start - 1) != '(') {
                            text = text.replaceAll(Pattern.quote(text.substring(start, i)), "+[" + text.substring(start, i) + "]");
                        } else {
                            text = text.replaceAll(Pattern.quote(text.substring(start, i)), "[" + text.substring(start, i) + "]");
                        }
                    }
                }
                //recalculate
                text = calculate(text);
            }
        }
        return text;
    }
    private static int[] getLeftElementBorders(int zeroPoint, String text){
        int[] ints = new int[2];
        //leftValueIndex is math sign position minus 1
        int leftValueIndex = zeroPoint - 1;
        //run in loop while char is not math sign or "=" and not begin of string
        while ((!mathSigns.contains(text.charAt(leftValueIndex)) && text.charAt(leftValueIndex) != '=') &&  leftValueIndex != 0){
            leftValueIndex--;
        }
        //if leftValueIndex == 0 the it is the most left element in input- take it
        //else it is math sign or "+ and we have to take previous
        leftValueIndex = leftValueIndex == 0 ? 0 : leftValueIndex + 1;
        ints[0] = leftValueIndex;
        ints[1] = zeroPoint;
        return ints;
    }

    private static int[] getRightElementBorders(int zeroPoint, String text){
        int[] ints = new int[2];
        //left index is next after math sing element
        int leftValueIndex = zeroPoint + 1;
        int rightValueIndex  = leftValueIndex;
        //run in loop while not end of input or math sign
        while (rightValueIndex < text.length() && !mathSigns.contains(text.charAt(rightValueIndex))){
            rightValueIndex++;
        }
        ints[0] = leftValueIndex;
        ints[1] = rightValueIndex;
        return ints;
    }

    private static double getElement(String text, int[] borders) throws Exception {
        Double result;
        int left = borders[0];
        int right = borders[1];
        //take next right element if "["
        if (text.charAt(borders[0]) == '['){
            left++;
        }
        //take next left element if "]"
        if (text.charAt(borders[1] - 1) == ']'){
            right--;
        }
        String element = text.substring(left, right);
        if (element.isEmpty()){
            element = "0";
        }
        // check if left element is number
        try
        {
            result = Double.parseDouble(element);
        }
        catch (NumberFormatException ex){
            // element is not number
            // check if have element in memory
            if (memoryMap.containsKey(element)) {
                // element exists return value
                result = memoryMap.get(element);
            }
            else {
                // element doesn't exists. Throw error
                throw new Exception("Element " + element + " doesn't exists in memory. You have to assign it first");
            }
        }
        // element is number- return
        return result;
    }
}
