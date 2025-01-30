package org.example.cg_attempt;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferParameters;

import java.util.*;

import static org.example.cg_attempt.HelloApplication.*;
import static org.example.cg_attempt.Operations.isEndpoint;
import static org.example.cg_attempt.Operations.onSegment;


public class VisibilityGraph {
    private final Map<Coordinate, Map<Coordinate, Double>> visibilityGraph;
    public List<Coordinate> graphPoints;


    public VisibilityGraph() {
        visibilityGraph = new HashMap<>();
    }

    // Creates the visibility graph
    public void createGraph(Coordinate start, Coordinate end) {

        visibilityGraph.clear();

        //Create the buffer zones
        resizedPolygons = resizePolygons(polygons);

        // Collect all points from polygons
        graphPoints = new ArrayList<>();
        for (Polygon polygon : resizedPolygons) {
            graphPoints.addAll(Arrays.asList(polygon.getCoordinates()));
        }

        if(flagIntersectionIndex!=-1){
            return;
        }



        // Add start and end points
        //Last two points are essential
        //OLD CONTENT
        //graphPoints.add(start);
        //graphPoints.add(end);

        List<Polygon> check_intersection;
        if(buffer_or_polygons){
            check_intersection = polygons;
        }else{
            check_intersection = resizedPolygons;
        }

        //This for map only
        // Iterate through all pairs of points
        for (int i = 0; i < graphPoints.size()-2; i++) {
            for (int j = i + 1; j < graphPoints.size(); j++) {
                Coordinate p1 = graphPoints.get(i);
                Coordinate p2 = graphPoints.get(j);


                //Try creating the path
               createPath(p1, p2, check_intersection);
            }
        }
        //This for player and flag
        addStartEndPoint(start,end);


    }

    //This is for creating a valid path in the visibilityGraph
    private void createPath(Coordinate p1,Coordinate p2,List<Polygon> check_intersection) {
        //Check for visibility
        if (isVisible(p1, p2,check_intersection)) {
            double distance = p1.distance(p2);
            visibilityGraph.computeIfAbsent(p1, k -> new HashMap<>()).put(p2, distance);
            visibilityGraph.computeIfAbsent(p2, k -> new HashMap<>()).put(p1, distance);
        }
    }


    private void addPointToGraph(Coordinate point, List<Coordinate> graphPoints) {
        synchronized (visibilityGraph) {
            for (Coordinate p : graphPoints) {
                int buffer = isInBuffer(point,resizedPolygons);
                if(buffer!=-1){
                    createPath(p,point,polygons);
                }else {
                  createPath(p,point,resizedPolygons);
                }
            }
        }

    }


    public void addStartEndPoint(Coordinate start, Coordinate end) {
        addPointToGraph(start, graphPoints);
        addPointToGraph(end, graphPoints);
        if(isVisible(start,end,polygons)){
            synchronized (visibilityGraph) {
                double distance = start.distance(end);
                visibilityGraph.computeIfAbsent(start, k -> new HashMap<>()).put(end, distance);
                visibilityGraph.computeIfAbsent(end, k -> new HashMap<>()).put(start, distance);
            }
        }
    }



    // Check if two points are visible
    private boolean isVisible(Coordinate p1, Coordinate p2,List<Polygon> polygons) {
        LineSegment segment = new LineSegment(p1, p2);


        // Iterate over all polygons
        for (Polygon polygon : polygons) {
            //Find the polygon which contains p1

            // Get the coordinates of the polygon
            Coordinate[] coords = polygon.getExteriorRing().getCoordinates();

            //This verifies visibility inside polygons
            int result = arePointsOnSamePolygon(p1,p2,coords);

            if(result == 0){
                return false;
            }else if(result == 1) {
                return true;
            }

            //This loop should deal with coords in different polygons
            // Loop over the edges (pairs of consecutive coordinates)
            for (int i = 0; i < coords.length - 1; i++) {
                Coordinate c1 = coords[i];
                Coordinate c2 = coords[i + 1];

                // Create an edge from consecutive points
                LineSegment edge = new LineSegment(c1, c2);

                // Check for intersection with the segment
                if (doIntersect(segment, edge)) {
                    return false;
                }
            }
        }

        // If no intersections were found, the points are visible
        return true;
    }


    // Print the visibility graph
    public void printGraph() {
        for (Map.Entry<Coordinate, Map<Coordinate, Double>> entry : visibilityGraph.entrySet()) {
            Coordinate point = entry.getKey();
            Map<Coordinate, Double> neighbors = entry.getValue();

            System.out.print("Point: (" + point.x + ", " + point.y + "): Visible to: ");
            for (Coordinate neighbor : neighbors.keySet()) {
                System.out.print("(" + neighbor.x + ", " + neighbor.y + "), ");
            }
            System.out.println();
        }
    }


    //Dijkstra, ALG COMPLEXITY(O((M+N)LOG(N))
    public List<Coordinate> DijkstraAlgorithm(Coordinate player, Coordinate flag) {

        Map<Coordinate, Double> distance = new HashMap<>();
        Map<Coordinate, Coordinate> prev = new HashMap<>();
        Set<Coordinate> visited = new HashSet<>();

        PriorityQueue<Coordinate> priority = new PriorityQueue<>(Comparator.comparingDouble(distance::get)); //Min heap

        // Prevents modifications by javaFX thread
        synchronized (visibilityGraph) {
            for (Coordinate coord : visibilityGraph.keySet()) {
                distance.put(coord, Double.MAX_VALUE);
            }
        } //This is N
        //N is the number of vertices


        distance.put(player, 0.0);
        priority.add(player); //Min heap addition (log N)

        while (!priority.isEmpty()) {
            Coordinate current = priority.poll();    //Poll runs in log N time, at most N times

            if (current.equals(flag)) break;

            visited.add(current);

            // For each connection the vertex has check for distance (connections is M)
            for (Map.Entry<Coordinate, Double> adjentNode : visibilityGraph.get(current).entrySet()) {
                Coordinate neighbor = adjentNode.getKey();
                double curr_distance = adjentNode.getValue();


                if (!visited.contains(neighbor)) {
                    double newDist = distance.get(current) + curr_distance;

                    if (newDist < distance.get(neighbor)) {
                        distance.put(neighbor, newDist);
                        prev.put(neighbor, current);
                        priority.add(neighbor);  //This takes log N, inside the for loop is M * Log N
                    }
                }
            }
        }

        //The while loop runs for at most N times, with the poll they run at N * Log N time, withe for loop it's at M * (N * Log N)
        //With the min-heap add it's at M * Log N, together the complexity is O((M+N) Log N)

        // Create the path (from target to player)
        List<Coordinate> path = new ArrayList<>();
        Coordinate iterator = flag;
        while (iterator != null) {
            path.add(iterator);
            iterator = prev.get(iterator);
        }

        Collections.reverse(path);
        return path;
    }


    // A* Algorithm ALG COMPLEXITY(O(N+M)LOG(N))
    public List<Coordinate> AStarAlgorithm(Coordinate player, Coordinate flag) {

        Map<Coordinate, Double> distance = new HashMap<>();
        Map<Coordinate, Double> heuristics = new HashMap<>();
        Map<Coordinate, Coordinate> prev = new HashMap<>();

        PriorityQueue<Coordinate> theFreeSet = new PriorityQueue<>(Comparator.comparingDouble(heuristics::get)); //Min heap
        // for javaFX thread
        synchronized (visibilityGraph) {

            for (Coordinate point : visibilityGraph.keySet()) {
                distance.put(point, Double.MAX_VALUE);
                heuristics.put(point, Double.MAX_VALUE);
            }
        }

        distance.put(player, 0.0);
        heuristics.put(player, heuristic(player, flag)); // heuristic stuff

        theFreeSet.add(player); //Log N

        while (!theFreeSet.isEmpty()) {   //Loop N * something

            Coordinate currentNode = theFreeSet.poll();
            if (currentNode.equals(flag))
                break;



            // Visit neigbhoors  //Verify each edge/connection (M nr of edges)
            for (Map.Entry<Coordinate, Double> adjentNode : visibilityGraph.get(currentNode).entrySet()) {


                Coordinate neighbor = adjentNode.getKey();
                double weight = adjentNode.getValue();

                double score = distance.get(currentNode) + weight;
                if (score < distance.get(neighbor)) {

                    prev.put(neighbor, currentNode);
                    distance.put(neighbor, score);

                    heuristics.put(neighbor, score + heuristic(neighbor, flag));
                    if (!theFreeSet.contains(neighbor)) {
                        theFreeSet.add(neighbor);  //Add on min heap Log N
                    }
                }
            }
        }



        //Complexity is O((N+M)Log N)

        // Create the path (from target to player)
        List<Coordinate> path = new ArrayList<>();
        Coordinate iterator = flag;
        while (iterator != null) {
            path.add(iterator);
            iterator = prev.get(iterator);
        }
        Collections.reverse(path);
        return path;

    }

    // Heuristic function, pretty basic as it's used only for distance(which we already have)
    private double heuristic(Coordinate p1, Coordinate p2) {

        //Maybe able to add detection if player hits the object aka if player has enough space (would require a simulation?)
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }



    //Verifies if two lines intersect
    public boolean doIntersect(LineSegment segment1, LineSegment segment2) {
        //Get the intersection of 2 lines
        Coordinate intersection = segment1.intersection(segment2);

        if (intersection == null) {
            return false; // No intersection
        }

        // Check if the intersection is at an endpoint of either segment
        boolean isEndpointIntersection = isEndpoint(intersection, segment1) || isEndpoint(intersection, segment2);

        // If the intersection is at a shared endpoint, ignore it (they are adjacent)
        return !isEndpointIntersection;

        // If the intersection is not at an endpoint and not on the same polygon, return true for different polygons
    }

    //Creates bufferzones
    private List<Polygon> resizePolygons(List<Polygon> polygons) {
        List<Polygon> res = new ArrayList<>();


        for (Polygon polygon : polygons) {
            // Buffer parameters to ensure straight edges
            BufferParameters bufferParams = new BufferParameters();
            bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT);  // Flat corners
            bufferParams.setQuadrantSegments(0);                    // Straight edges

            // Apply buffer to uniformly scale the polygon
            double scaleDistance = 2.0;  // Distance to grow/shrink
            Polygon scaledPolygon = (Polygon) polygon.buffer(scaleDistance,0);
            res.add(scaledPolygon);
        }

        return res;

    }


    //Verifies if two vertixes are part of the same polygon
    private int arePointsOnSamePolygon(Coordinate p1, Coordinate p2, Coordinate[] coords) {


        //This for deals with coords in same polygon interactions

        // Iterate through the polygon's points (Check if p1 is here)
        for(int i = 0; i < coords.length - 1; i++) {
            //Find the p1's polygon
            if(coords[i].equals(p1)){
                //When p1 is found, check if p2 is part of same polygon
                for(int j = 0; j < coords.length - 1; j++) {
                    //If p2 is found, p1 and p2 are part of the same polygon, do different set of tests
                    if(coords[j].equals(p2)){
                        //If in this case, tell the function that it should exit anyway
                        return areFormingALine(i,j,coords);
                        //0 or 1 means they are on the polygon, so tell the program to return 1 for no intersection and 0 if not one after another

                    }
                    //If p2 is not found leave the for, p2 is part of other polygon
                }
            }
        }
        return -1; // -1 means they are not on the same polygon, so the other checks should continue
    }

    //Helper function for arePointsOnSamePolygon
    private int areFormingALine(int index1, int index2, Coordinate[] coords) {

        // Check if p1 and p2 are a segment of the boundary
        int index_1 = (index1+1)% (coords.length-1);
        int index_2 = (index1-1)% (coords.length-1);

        if (index_2 == -1)
            index_2 = coords.length-2;
        // Basically check if they are one after another
        if(( index_1 == index2|| index_2 ==index2)){
            return 1;  //True if they are one after another
        }
        return 0; //False if they are not (Intersection true)
    }

    //Verifies if a point is inside a polygon (using orientation)
    private boolean isPointsInThePolygonEf(Coordinate p1, Polygon polygon) {
        Coordinate[] coords = polygon.getCoordinates();
        int orientationReference = 0; //need to initialize it

        for (int i = 0; i < coords.length - 1; i++) {
            int orientation = Operations.orientationCoordinates(coords[i], coords[i + 1], p1);

            if (orientation == 0) {
                if(onSegment(coords[i],coords[i+1],p1)){
                    return true;
                }
                return false;
            }

            if (i == 0) {
                //First orientation assumes it's inside
                orientationReference = orientation;

                //If the orientations differ, then it's not inside
            } else if (orientation != orientationReference) {
                return false;
            }
        }

        return true; // True if all orientations are equal
    }

    public Map<Coordinate, Map<Coordinate, Double>>  getVisibilityGraph() {
        return visibilityGraph;
    }

    //Verifies if a point is in a buffer zone
    public int isInBuffer(Coordinate point,List<Polygon> polygons) {
        //If point is in the buffer
        for(int index = 0; index<polygons.size();index++) {

            if(isPointsInThePolygonEf(point,polygons.get(index))) {
                return index;
            }
        }
        return -1;
    }

    //Verifies if a shape is in a polygon
    public boolean isShapeInPolygon(Polygon shape,Polygon polygon) {
        Coordinate[] coords = shape.getCoordinates();

        for (int i = 0; i < coords.length - 1; i++) {
            if(isPointsInThePolygonEf(coords[i],polygon)) {
                return true;
            }
        }
        return false;
    }

    //If a point is in a bufferzone (at the time class is used) attempt to remove any
    //false visible points by verifying intersections with buffer's polygon
    public boolean removeInBufferIntersection(Coordinate point,List<Polygon> resized_polygon) {

        int is_in_buffer = isInBuffer(point,resized_polygon);

        if(is_in_buffer != -1) {
            //Create list with one element
            List<Polygon> list = new ArrayList<>();

            //Add the buffer's polygon
            list.add(polygons.get(is_in_buffer));

            for (Coordinate p : resized_polygon.get(is_in_buffer).getCoordinates()) {

                if(!isVisible(p,point,list)){

                    //remove visibility from p to point and from point to p
                    visibilityGraph.get(p).remove(point);
                    visibilityGraph.get(point).remove(p);
                }
            }
            return true;
        }
        return false;
    }

    // Print the shortest path
    public void printShortestPath(Coordinate start, Coordinate target) {
        List<Coordinate> path = DijkstraAlgorithm(start, target);
        System.out.println("Shortest path from (" + start.x + ", " + start.y + ") to (" + target.x + ", " + target.y + "):");
        for (Coordinate coord : path) {
            System.out.print("(" + coord.x + ", " + coord.y + ") -> ");
        }
        System.out.println("END");
    }
}
