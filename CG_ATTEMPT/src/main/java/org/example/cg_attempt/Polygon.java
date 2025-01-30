package org.example.cg_attempt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Polygon {

    @JacksonXmlProperty(localName = "point")
    @JacksonXmlElementWrapper(useWrapping = false)
    private  List<Point> points;
    private List<Edge> edges;


    //Constructors
    public Polygon() {
        points = new ArrayList<>();
    }

    public Polygon(List<Point> p) {
        if (p.size() < 2) {
            throw new IllegalArgumentException("Polygon must have at least two points");
        }
        this.points = convexHull(p);

        createEdges();


    }

    public Polygon(Polygon p) {
        if (p.getPoints().size() < 2) {
            throw new IllegalArgumentException("Polygon must have at least two points");
        }
        this.points = convexHull(p.getPoints());
        createEdges();
    }

    private void createEdges() {
        this.edges = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Point start = points.get(i);
            Point end = points.get((i + 1) % points.size());
            edges.add(new Edge(start, end));
        }
    }

    public void translatePolygon(double dx, double dy) {
        List<Point> translatedPolygon = new ArrayList<>();
        for (Point p : this.points) {
            double newX = p.getX() + dx;
            double newY = p.getY() + dy;
            translatedPolygon.add(new Point(newX, newY));
        }
        points = translatedPolygon;

    }


    public void addPoint(Point point) {
        points.add(point);
    }

    @JsonProperty("point")
    public List<Point> getPoints() {
        return points;
    }
    public List<Edge> getEdges() {
        return edges;
    }

    public Point leftMostPoint(List<Point> points) {

        Point leftMostPoint = new Point(points.getFirst());


        for (Point point : points) {
            leftMostPoint = point.getSmallerPoint(leftMostPoint);
            }

        return leftMostPoint;
    }

    private double angleFromPoint(Point ref, Point p1) {
        return Math.atan2(p1.getY()-ref.getY(),p1.getX()-ref.getX());

    }


    //Attempt at polygons
    public List<Point> sortByPolarAngle(List<Point> points) {

        Point ref = leftMostPoint(points);

        points.sort((p1,p2) -> {

            double angle1 = angleFromPoint(ref, p1);
            double angle2 = angleFromPoint(ref, p2);
            System.out.println(p1+": "+angle1+" | "+p2+": " + angle2);

            return Double.compare(angle1,angle2);
        });

        return points;
    }

    //Ghraham Scan Algorithm
    public List<Point> convexHull(List<Point> points) {

        List<Point> convexHull = new ArrayList<>();

        if (points.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least three points");
        }

        Point left_most_point = new Point(leftMostPoint(points));
        int p = points.indexOf(left_most_point);
        int q;

        do{
            convexHull.add(points.get(p));

            q = (p+1)%points.size();

            for(int i = 0; i < points.size(); i++){
                if (Operations.orientation(points.get(p),points.get(i),points.get(q)) == -1)
                    q=i;
            }

            p = q;

        }while(p != points.indexOf(left_most_point));

        return convexHull;

    }


    @Override
    public String toString() {
        return points.toString();
    }

    public boolean isPointInPolygon(Point p) {
        for (Point point : points) {
            if(point.equals(p))
                return true;
        }
        return false;
    }


    //Collision logic from here idk

    //SOLVE PROJECTION FIRST
    //Slope l1: (y2-y1)/(x2-x1) = m
    //Slope l2: -1/m
    //Optional really


}
