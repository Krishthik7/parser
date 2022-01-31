package com.example;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    // used to stored final value after resolving the postfix expression
    // example a1 -> 10, a2 -> 40 , a3-> ERR etc
    static Map<String, String> resultMap = new TreeMap<>();
    // used to stored individual express at individual cell for reference
    // example a1 -> 10, a2 -> b1 b2 * etc
    static Map<String, String> expressionMap = new TreeMap<>();

    public static void main(String[] args) throws FileNotFoundException {

        try {
            // read the file
            File file = new File(args[0]);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            char start = 'a';
            int index = 1;
            while ((line = br.readLine()) != null) { // Split on new line to get a list of all the lines
                List<String> al = Arrays.stream(line.split(",")).collect(Collectors.toList()); // split each entry by splitting is using ','
                for (String expression : al) {
                    expression = expression.trim(); // trim to remove extra spaces around.
                    expressionMap.put("" + start + index, expression); // put the express with appropriate key(a1,a2 ...) in the map
                    start = (char) (start + 1);
                }
                index = index + 1;
                start = 'a';
            }
            fr.close();
        } catch (IOException e) { //file to be parsed doesnt exist
            System.out.println("File " + args[0] + " not found");
        }
        // we will loop equal to the number of size of the total expression.
        // this should be good enough to resolve all the express at individual cells.
        IntStream.range(0, expressionMap.size())
                .forEach(loop -> {
                    expressionMap.entrySet().forEach(data -> {
                        if (data.getValue().contains(data.getKey())) { // if the entry at the cell has any reference to cell that means it cannot be calculated
                            resultMap.put(data.getKey(), "ERR");
                            return;
                        }
                        String expression = data.getValue();
                        try {
                            Double value = Double.valueOf(expression); // check if the cell value is resolved to a number
                            resultMap.put(data.getKey(), value.toString()); // if yes put the result to resultmap for display
                        } catch (NumberFormatException e) {
                            if (Pattern.compile(".*[a-z]+.*").matcher(expression).find()) {  // check for all the reference to another cell in a particular cell E.g. b1 b2 *
                                for (String key : resultMap.keySet()) { // if references are found then one by one replace all the references from the calculated values in the final result
                                    expression = expression.replace(key, resultMap.get(key));
                                    expressionMap.put(data.getKey(), expression); // put the updated result back in the expression map
                                }
                            } else {
                                String stringValue = evaluatePostfix(expression); // if there are not references to other cell then calculate the postfix value of the cell
                                try {
                                    Double.valueOf(stringValue);  // the result can be ERR. if so this will throw a  NumberFormatException and we will push ERR in result
                                    resultMap.put(data.getKey(), evaluatePostfix(expression)); // if its a proper number push it to the resultmap
                                } catch (NumberFormatException e1) {
                                    resultMap.put(data.getKey(), "ERR");
                                }
                            }
                        }
                    });

                });
        printResultMap(resultMap);
    }

    public static String evaluatePostfix(String exp) {
        //create a stack
        Stack<Double> stack = new Stack<>();

        // Scan all characters one by one
        String[] arr = exp.split(" ");
        for (String s : arr) {
            try {
                stack.push(Double.parseDouble(s)); // if its a normal number push it to a stack. if its not a numebr this will throw exception and we will parse the expression in catch
            } catch (Exception e) { // this means we are dealing with a operation
                try {
                    //get top two numbers
                    double val1 = stack.pop();
                    double val2 = stack.pop();
                    // do the appropriate operation on the above two numbers
                    switch (s) {
                        case "+":
                            stack.push(val2 + val1);
                            break;

                        case "-":
                            stack.push(val2 - val1);
                            break;

                        case "/":
                            stack.push(val2 / val1);
                            break;

                        case "*":
                            stack.push(val2 * val1);
                            break;
                        default:
                            return "ERR"; // if nothing matches means error
                    }
                } catch (Exception e1) {
                    return "ERR"; // if its a wrong expression then we will have error
                }

            }
        }
        return stack.pop().toString(); // return the result of the postfix operation
    }

    public static void printResultMap(Map<String, String> resultMap) {
        Map<Object, List<Map.Entry<String, String>>> result = resultMap.entrySet().stream().collect(Collectors.groupingBy(stringStringEntry -> { // group all the keys based on row, that is row 1 row 2 etc
            String key = stringStringEntry.getKey();
            String index = key.replaceAll("[a-z]", ""); //we will remove the character and will be left with row number. that is a1 will be converted to 1 that means row one
            return index;
        }));
        for (List<Map.Entry<String, String>> entry : result.values()) { // we have result grouped row wise not we just need to display it
            entry.stream().forEach(e -> {
                if (e.getValue().equals("ERR")) { // if the entry is ERR display ERR
                    System.out.print("ERR, ");
                } else {
                    double d = Double.valueOf(e.getValue());
                    if (d == (long) d) // if the result has a decimal point. then this will hold true.
                        System.out.print(String.format("%d", (long) d) + ", "); // display the result in long, meaning along with decimal point
                    else
                        System.out.print(String.format("%s", d) + ", "); // just display the reult integer without any decimal point
                }
            });
            System.out.println(); // this is to give a new line after each row display
        }
    }
}
