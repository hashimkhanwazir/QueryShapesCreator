package com.example;

import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdtjena.HDTGraph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.graph.Graph;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import java.util.Set;
import java.util.HashSet;

public class HDTQueryExample {

    public static void main(String[] args) {
        try {
            System.out.println("i am inside the method");
            // Load the HDT file
            String hdtPath = "/Users/hashimkhan/Documents/java-mvn-practice/hdt-based-project/hdt-sparql-project/data/sample.hdt";  // Update with your HDT path
            HDT hdt = HDTManager.loadIndexedHDT(hdtPath, null);
            HDTGraph graph = new HDTGraph(hdt);

            Model model = ModelFactory.createModelForGraph(graph); // Wrap HDTGraph into a Model
            // Create a Jena model from the HDT graph
            Dataset dataset = DatasetFactory.create(model);

            // SPARQL Query example
            String sparqlQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 10";

            Query query = QueryFactory.create(sparqlQuery);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
                ResultSet results = qexec.execSelect();
                ResultSetFormatter.out(System.out, results, query);
            }
    System.out.println("Through the HDT ");
    IteratorTripleString it = hdt.search("", "", ""); // search all triples
  //  while (it.hasNext()) {
  //      TripleString ts = it.next();
  //      System.out.println(ts.getSubject() + " " + ts.getPredicate() + " " + ts.getObject());
//}

 // Map to hold adjacency list
 Map<String, List<String>> adjacencyList = new HashMap<>();

 while (it.hasNext()) {
    TripleString triple = it.next();
    String subject = triple.getSubject().toString();
    String predicate = triple.getPredicate().toString();
    String object = triple.getObject().toString();

    String edge = "(" + predicate + ", " + object + ")";

    adjacencyList.computeIfAbsent(subject, k -> new ArrayList<>()).add(edge);
}

// Print adjacency list (first 10 entries)
int count = 0;
for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
    if (++count == 10) break;
}

// Now to see the IDs we have to do like that. 

Dictionary dictionary = hdt.getDictionary();


//String subject1 = "http://example.org/subject1";
//String predicate1 = "http://example.org/predicate1";

// Get the internal IDs
//long subjectId =  dictionary.stringToId(subject1, TripleComponentRole.SUBJECT);
//long predicateId = dictionary.stringToId(predicate1, TripleComponentRole.PREDICATE);

//subjectId = (int) subjectId;
//predicateId = (int) predicateId; 

//TripleID pattern = new TripleID(subjectId, predicateId, 0); // 0 means wildcard object

IteratorTripleID itForID = hdt.getTriples().search(new TripleID(0L, 0L, 0L));

//Set<Long> seenObjects = new HashSet<>(); // To avoid duplicates

System.out.println("-----------------------------------------------------............................");

while (itForID.hasNext()) {
    TripleID triple = itForID.next();
    
    long subjectId = triple.getSubject();
    long objectId = triple.getObject();
    long predicateId = triple.getPredicate();

    CharSequence objectString = dictionary.idToString(objectId, TripleComponentRole.OBJECT);
    CharSequence subjectString = dictionary.idToString(subjectId, TripleComponentRole.SUBJECT);
    CharSequence predicateString = dictionary.idToString(predicateId, TripleComponentRole.PREDICATE);

    // Print subject, predicate, and object IDs and their corresponding strings
    System.out.println("Subject ID: " + subjectId + " | Subject String: " + subjectString);
    System.out.println("Predicate ID: " + predicateId + " | Predicate String: " + predicateString);
    System.out.println("Object ID: " + objectId + " | Object String: " + objectString);
    System.out.println("-----------------------------------------------------");

    
//while (itForID.hasNext()) {
 //   TripleID tripleID = itForID.next();
 //   long objectId = tripleID.getObject();
 //   System.out.println("Object ID: " + objectId);
}

System.out.println("_____________________________________________________________________________");

String queryString_1 = "SELECT ?subject ?object WHERE {?subject <http://example.org/property/predicate10> ?object . ?subject <http://example.org/property/predicate8> ?object1}";
String queryString_2 = "SELECT ?subject ?object WHERE {?subject <http://example.org/property/predicate10> ?object . ?subject <http://example.org/property/predicate8> ?object1 . ?object1 <http://example.org/property/predicate5> ?object2} ";

String queryString_3 = "SELECT ?subject ?object WHERE {?subject <http://example.org/property/predicate10> ?object . ?subject <http://example.org/property/predicate8> ?object1 .  ?object1 <http://example.org/property/predicate5> ?object2 . ?subject3 <http://example.org/property/predicate5> ?object2} ";

System.out.println(queryString_2);


    Query query_1 = QueryFactory.create(queryString_2);
    QueryUtils.printTriples(queryString_1);
   
    try (QueryExecution qexec = QueryExecutionFactory.create(query_1, dataset)) {
        ResultSet results = qexec.execSelect();
        ResultSetFormatter.out(System.out, results, query_1);
    }


    //QueryShapeDetector.analyzer(queryString_3);

// LETS IMPLEMENT THE NESTED LOOP JOIN HERE 

            hdt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}