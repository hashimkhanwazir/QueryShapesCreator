package com.example;


import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.graph.*;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import java.util.Iterator;

import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

 public static List<Triple> extractTriplePatterns(String queryString) {
    List<Triple> triples = new ArrayList<>();

    Query query = QueryFactory.create(queryString);
    Element pattern = query.getQueryPattern();

    if (pattern instanceof ElementGroup) {
        ElementGroup group = (ElementGroup) pattern;
        List<Element> elements = group.getElements();

        for (Element el : elements) {
            if (el instanceof ElementPathBlock) {
                ElementPathBlock epb = (ElementPathBlock) el;
                Iterator<TriplePath> iter = epb.getPattern().iterator();
                while (iter.hasNext()) {
                    TriplePath triplePath = iter.next();
                    if (triplePath.isTriple()) {
                        triples.add(triplePath.asTriple());
                    }
                }
            }
        }
    }

    return triples;
}

    public static String nodeToString(Node node) {
        if (node.isVariable()) return "VAR(" + Var.alloc(node).getVarName() + ")";
        else return node.toString();
    }

    public static void printTriples(String queryString) {
        List<Triple> triples = extractTriplePatterns(queryString);
        for (Triple triple : triples) {
            System.out.println("Triple Pattern:");
            System.out.println("  Subject  : " + nodeToString(triple.getSubject()));
            System.out.println("  Predicate: " + nodeToString(triple.getPredicate()));
            System.out.println("  Object   : " + nodeToString(triple.getObject()));
            System.out.println();
        }
    }
}