package org.example.cg_attempt;


import org.locationtech.jts.*;
import org.locationtech.jts.algorithm.Intersection;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.List;

public class Operations {

    GeometryFactory geometryFactory = new GeometryFactory();

    //Return distance between points
    static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }

    static int orientation(Point p1, Point p2, Point p3) {
        double val = (p2.getY() - p1.getY()) * (p3.getX() - p2.getX()) - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY());

        //Collinear points
        if(val == 0){
            return 0;
        }

        //Clockwise points
        if(val > 0){
            return 1;
        }

        //CounterClockwise points
        if(val < 0){
            return -1;
        }

        //Supposedly error
        return -2;
    }

    public static int orientationCoordinates(Coordinate p1, Coordinate p2, Coordinate p3) {
        double val = (p2.getY() - p1.getY()) * (p3.getX() - p2.getX()) - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY());

        //Clockwise points (to the right)
        if(val > 0){
            return 1;
        }

        //CounterClockwise points (to the left)
        if(val < 0){
            return -1;
        }

        if(val == 0){
            return 0;
        }

        //Supposedly error
        return -2;
    }

    // Check if the point is one of the endpoints of the line segment
    public static boolean isEndpoint(Coordinate coordinate, LineSegment segment) {
        return coordinate.equals(segment.p0) || coordinate.equals(segment.p1);
    }

    // Checks if point q is on the segment p-r (if yes, then the collision is correct)
    static boolean onSegment(Coordinate p, Coordinate r, Coordinate q) {
        return q.getX() <= Math.max(p.getX(), r.getX()) &&
                q.getX() >= Math.min(p.getX(), r.getX()) &&
                q.getY() <= Math.max(p.getY(), r.getY()) &&
                q.getY() >= Math.min(p.getY(), r.getY());
    }


    static Polygon rotate(Polygon body, double angle) {

        Coordinate centroid = body.getCentroid().getCoordinate();

        // Create a rotation transformation
        AffineTransformation rotation = AffineTransformation.rotationInstance(angle, centroid.x, centroid.y);

        // Apply the rotation to the polygon
        Geometry rotatedPolygon = rotation.transform(body);
        return (org.locationtech.jts.geom.Polygon) rotatedPolygon;
    }

    // Move polygon
    static Polygon setPosition(double x, double y,Polygon body) {

        GeometryFactory gf = new GeometryFactory();

        Coordinate centroid = body.getCentroid().getCoordinate();

        double xDiff = x-centroid.getX();
        double yDiff = y-centroid.getY();

        Coordinate[] coords = body.getCoordinates();
        Coordinate[] newCoords = new Coordinate[coords.length];

        for (int i = 0; i < coords.length; i++) {
            newCoords[i] = new Coordinate(coords[i].getX()+xDiff, coords[i].getY()+yDiff);
        }


         return gf.createPolygon(newCoords);
    }


    //Find closest available point on the buffer
    static Coordinate findClosestPoint(Coordinate point,Polygon polygon,Path path) {
        System.out.println("findClosestPoint");
        double minDistance = Double.MAX_VALUE;
        Coordinate closestPoint = null;

        for(Coordinate coord : polygon.getCoordinates()) {
            if(path.getCoordinates().contains(coord)) {
                System.out.println(coord +" is in the path");
                continue;
            }
            if(path.getAttemptedPath().contains(coord)) {
                System.out.println(coord +" already attempted");
                continue;
            }
            double dist = coord.distance(point);
            if(minDistance > dist) {
                minDistance = dist;
                closestPoint = coord;
            }
        }


        System.out.println(closestPoint);
        path.addAttemptedNode(closestPoint);
        return closestPoint;


    }

    static boolean isSharedEndpoint(Point p1, Point p2, Point e1, Point e2) {
        return (p1.equals(e1) || p1.equals(e2) || p2.equals(e1) || p2.equals(e2));
    }








}
