package com.example;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShapeToQueryConverter {

    /**
     * Converts a shape string into a Jena Query object.
     * @param shapeString A string like:
     *        "[Star] [?subject @http://example.org/property/predicate10 ?object, ?subject @http://example.org/property/predicate8 ?object1]"
     * @return Jena Query object
     */
    public static Query buildQueryFromShape(String shapeString) {
        // Clean up
        shapeString = shapeString.trim();
        if (shapeString.startsWith("[Star]")) {
            shapeString = shapeString.substring("[Star]".length()).trim();
        }
        if (shapeString.startsWith("[") && shapeString.endsWith("]")) {
            shapeString = shapeString.substring(1, shapeString.length() - 1);
        }

        // Now it looks like: "?subject @predicate1 ?object, ?subject @predicate2 ?object1"

        // Create an empty SELECT query
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.addResultVar("*"); // SELECT *

        ElementGroup body = new ElementGroup();
        ElementPathBlock block = new ElementPathBlock();

        // Split into triples
        String[] triples = shapeString.split("\\s*,\\s*");
        for (String tripleStr : triples) {
            Triple triple = parseTriple(tripleStr);
            block.addTriple(triple);
        }

        body.addElement(block);
        query.setQueryPattern(body);

        return query;
    }

    private static Triple parseTriple(String tripleStr) {
        // Regex to capture ?subject @predicate ?object
        Pattern pattern = Pattern.compile("(\\?[\\w\\d]+)\\s*@([^\\s]+)\\s*(\\?[\\w\\d]+)");
        Matcher matcher = pattern.matcher(tripleStr.trim());

        if (matcher.matches()) {
            String subjectStr = matcher.group(1);
            String predicateStr = matcher.group(2);
            String objectStr = matcher.group(3);

            Node subject = NodeFactory.createVariable(subjectStr.substring(1)); // Remove ?
            Node predicate = NodeFactory.createURI(predicateStr);
            Node object = NodeFactory.createVariable(objectStr.substring(1));   // Remove ?

            return Triple.create(subject, predicate, object);
        } else {
            throw new IllegalArgumentException("Invalid triple format: " + tripleStr);
        }
    }
}