package com.example;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.*;

import java.util.*;

public class QueryShapeDetector {

    public static List<List<Set<Triple>>> detectQueryShapes(String queryStr) {
        Query query = QueryFactory.create(queryStr);
        ElementGroup group = (ElementGroup) query.getQueryPattern();

        List<Triple> triples = new ArrayList<>();
        for (Element el : group.getElements()) {
            if (el instanceof ElementPathBlock) {
                ElementPathBlock epb = (ElementPathBlock) el;
                epb.patternElts().forEachRemaining(tp -> {
                    if (tp.isTriple()) triples.add(tp.asTriple());
                });
            }
        }

        List<List<Set<Triple>>> allShapeSets = new ArrayList<>();
        detectShapesRecursive(triples, new HashSet<>(), new ArrayList<>(), allShapeSets);
        return allShapeSets;
    }

    private static void detectShapesRecursive(List<Triple> triples, Set<Triple> used, List<Set<Triple>> currentShapes, List<List<Set<Triple>>> allSets) {
        if (used.size() == triples.size()) {
            allSets.add(new ArrayList<>(currentShapes));
            return;
        }

        for (int i = 0; i < triples.size(); i++) {
            Triple t1 = triples.get(i);
            if (used.contains(t1)) continue;

            // Star shape: same subject
            Set<Triple> star = new HashSet<>();
            Node s = t1.getSubject();
            for (Triple t : triples) {
                if (!used.contains(t) && t.getSubject().equals(s)) {
                    star.add(t);
                }
            }
            if (star.size() > 1) {
                used.addAll(star);
                currentShapes.add(star);
                detectShapesRecursive(triples, used, currentShapes, allSets);
                currentShapes.remove(currentShapes.size() - 1);
                used.removeAll(star);
            }

            // Sink shape: same object
            Set<Triple> sink = new HashSet<>();
            Node o = t1.getObject();
            for (Triple t : triples) {
                if (!used.contains(t) && t.getObject().equals(o)) {
                    sink.add(t);
                }
            }
            if (sink.size() > 1) {
                used.addAll(sink);
                currentShapes.add(sink);
                detectShapesRecursive(triples, used, currentShapes, allSets);
                currentShapes.remove(currentShapes.size() - 1);
                used.removeAll(sink);
            }

            // Path shape: recursively follow chains
            Set<Triple> path = new LinkedHashSet<>();
            buildPathShape(t1, triples, used, path);

            if (path.size() > 1) {
                used.addAll(path);
                currentShapes.add(path);
                detectShapesRecursive(triples, used, currentShapes, allSets);
                currentShapes.remove(currentShapes.size() - 1);
                used.removeAll(path);
            }

            // Single triple
            Set<Triple> single = new HashSet<>();
            single.add(t1);
            used.add(t1);
            currentShapes.add(single);
            detectShapesRecursive(triples, used, currentShapes, allSets);
            currentShapes.remove(currentShapes.size() - 1);
            used.remove(t1);

            break; // Only proceed with one starting point at a time
        }
    }

    private static void buildPathShape(Triple start, List<Triple> triples, Set<Triple> used, Set<Triple> path) {
        path.add(start);
        Node currentObj = start.getObject();

        for (Triple t : triples) {
            if (!used.contains(t) && !path.contains(t) && currentObj.equals(t.getSubject())) {
                buildPathShape(t, triples, used, path);
                break; // Only follow one chain
            }
        }
    }

    public static String detectShapeType(Set<Triple> shape) {
        if (shape.size() == 1) return "Single";

        Set<Node> subjects = new HashSet<>();
        Set<Node> objects = new HashSet<>();
        for (Triple t : shape) {
            subjects.add(t.getSubject());
            objects.add(t.getObject());
        }

        if (subjects.size() == 1) return "Star";
        if (objects.size() == 1) return "Sink";

        List<Triple> list = new ArrayList<>(shape);
        boolean isPath = true;
        for (int i = 0; i < list.size() - 1; i++) {
            if (!list.get(i).getObject().equals(list.get(i + 1).getSubject())) {
                isPath = false;
                break;
            }
        }
        return isPath ? "Path" : "Unknown";
    }

    public static String shapeToString(Set<Triple> shape) {
        StringBuilder sb = new StringBuilder("[");
        for (Triple t : shape) {
            sb.append(t.getSubject()).append(" @").append(t.getPredicate()).append(" ").append(t.getObject()).append(", ");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    public static List<List<String>> detectFormattedShapes(String queryStr) {
        List<List<Set<Triple>>> allShapeSets = detectQueryShapes(queryStr);
        List<List<String>> result = new ArrayList<>();

        for (List<Set<Triple>> shapeSet : allShapeSets) {
            List<String> formatted = new ArrayList<>();
            for (Set<Triple> shape : shapeSet) {
                String label = detectShapeType(shape);
                formatted.add("[" + label + "] " + shapeToString(shape));
            }
            result.add(formatted);
        }

        return result;
    }
}

