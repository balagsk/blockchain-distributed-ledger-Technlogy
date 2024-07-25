package com.code.trade.springboot;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bala
 */

@Service
public class AccumulatorService {

    public static int add(String numbers) throws IllegalArgumentException {
        int resultSum = 0;
        if (numbers.isEmpty()) {
            return 0;
        }
        // Default delimiter are comma and newline
        String delimiter = ",|\n";

        // Support for multiple delimiters
        if (numbers.startsWith("//")) {
            int delimiterEndIndex = numbers.indexOf('\n');
            delimiter = numbers.substring(2, delimiterEndIndex);
            numbers = numbers.substring(delimiterEndIndex + 1);

            // Split the input using the custom whitelisted delimiters
            if (delimiter.contains("***")) {
                String[] tokens = numbers.split("\\*{3}");
                resultSum = calculation(tokens);
            } else if (delimiter.contains("*|%")) {
                String[] tokens = numbers.split("\\*|%");
                resultSum = calculation(tokens);
            } else{
                String[] tokens = numbers.split(delimiter);
                resultSum = calculation(tokens);
            }
        } else {
            String[] tokens = numbers.split(delimiter);
            resultSum = calculation(tokens);
        }
        return resultSum;
    }

    public static int calculation(String[] tokens) {
        List<Integer> negatives = new ArrayList<>();
        int sum = 0;

        for (String token : tokens) {
            if (!token.isEmpty()) {
                int number = Integer.parseInt(token.trim());

                if (number < 0) {
                    negatives.add(number);
                }

                // Ignore numbers bigger than 1000
                if (number <= 1000) {
                    sum += number;
                }
            }
        }
        if (!negatives.isEmpty()) {
            throw new IllegalArgumentException("negatives not allowed: " + negatives);
        }
        return sum;
    }

    //Renamed to test method, due to Banking email restrictions
    public static void test() {
        AccumulatorService acc = new AccumulatorService();

        // Test cases
        System.out.println("Result: " + acc.add("")); // passed
        System.out.println("Result: " + acc.add("1")); // passed
        System.out.println("Result: " + acc.add("1,2"));// passed
        System.out.println("Result: " + acc.add("1\n2,3"));// passed
        try {
            System.out.println("Result: " + acc.add("1,-2"));// passed
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Should print "negatives not allowed: [-2]" // passed
        }
        System.out.println("Result: " + acc.add("//;\n1;2"));// passed
        System.out.println("Result: " + acc.add("//***\n1***2***3")); //passed
        System.out.println("Result: " + acc.add("//*|%\n1*2%3"));// passed
        System.out.println("Result: " + acc.add("1,1001"));// passed
    }

    // public static void main(String[] args) {  test();  }

}
