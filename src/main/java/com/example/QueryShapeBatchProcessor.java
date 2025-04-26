
package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class QueryShapeBatchProcessor {
    public static void main(String[] args) {
        String queryFolderPath = null;

        // Step 1: Read Command Line Args
        for (int i = 0; i < args.length; i++) {
            if ("-q".equals(args[i]) && i + 1 < args.length) {
                queryFolderPath = args[i + 1];
                break;
            }
        }

        if (queryFolderPath == null) {
            System.err.println("Usage: java QueryShapeBatchProcessor -q <path_to_queries_folder>");
            System.exit(1);
        }

        File folder = new File(queryFolderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("The provided path is not a valid directory: " + queryFolderPath);
            System.exit(1);
        }

        // Step 2: Filter .rq, .sparql, or .txt files
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".rq") || name.endsWith(".sparql") || name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.err.println("No query files found in: " + queryFolderPath);
            System.exit(1);
        }

        // Step 3: Process Each Query File
        for (File file : files) {
            System.out.println("\n=== Processing file: " + file.getName() + " ===");
            String queryStr;

            try {
                queryStr = Files.readString(file.toPath());
            } catch (IOException e) {
                System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
                continue;
            }

            try {
                List<List<String>> allCombinations = QueryShapeDetector.detectFormattedShapes(queryStr);
                int counter = 1;
                for (List<String> combination : allCombinations) {
                    System.out.println("Detected Shapes (Combination " + counter++ + "):");
                    for (String shape : combination) {
                        System.out.println(shape);
                        // 🔧 Step 1: Print triples in the shape
                        System.out.println(">> Extracted Triple Patterns:");
                    //    QueryUtils.printTriples(shape);
                    }
                    System.out.println("----------------------------");
                }
            } catch (Exception e) {
                System.err.println("Error parsing or processing query in " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
