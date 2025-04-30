package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class QueryShapeSingleProcessor {

    public static void main(String[] args) {
        String queryFilePath = null;

        // Step 1: Read Command Line Args
        for (int i = 0; i < args.length; i++) {
            if ("-q".equals(args[i]) && i + 1 < args.length) {
                queryFilePath = args[i + 1];
                break;
            }
        }

        if (queryFilePath == null) {
            System.err.println("Usage: java QueryShapeSingleProcessor -q <path_to_query_file>");
            System.exit(1);
        }

        File file = new File(queryFilePath);
        if (!file.exists() || !file.isFile()) {
            System.err.println("The provided path is not a valid file: " + queryFilePath);
            System.exit(1);
        }

        // Step 2: Read Query File
        String queryStr;
        try {
            queryStr = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            System.exit(1);
            return; // required after System.exit for completeness
        }

        // Step 3: Process Query
        System.out.println("\n=== Processing file: " + file.getName() + " ===");
        try {
            // Step 3.1: Detect and store combinations
            List<List<String>> allCombinations = QueryShapeDetector.detectFormattedShapes(queryStr);

            // Step 3.2: Pass combinations to the display method
            displayCombinations(allCombinations);
        } catch (Exception e) {
            System.err.println("Error parsing or processing query in " + file.getName() + ": " + e.getMessage());
        }
    }

    // Step 3.2: Display stored combinations
    public static void displayCombinations(List<List<String>> allCombinations) {
        if (allCombinations != null) {
            int counter = 1;
            // Iterate over each combination and display it
            for (List<String> combination : allCombinations) {
                System.out.println("\nDetected Shapes (Combination " + counter++ + "):");
                for (String shape : combination) {
                    System.out.println(shape);
                }
                System.out.println("----------------------------");
            }
        } else {
            System.err.println("No combinations detected.");
        }
    }
}
