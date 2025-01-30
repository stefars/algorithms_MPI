package org.example.cg_attempt;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Point implements Comparable<Point> {
    private double x;
    private double y;

    public Point() {}

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    //Getters
    @JsonProperty("x")
    public double getX() {
        return x;
    }
    @JsonProperty("y")
    public double getY() {
        return y;
    }

    //Setters
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    //Comparable
    @Override
    public int compareTo(Point o) {
        if (this.x > o.x) {
            return 1;
        }
        if (this.x < o.x) {
            return -1;
        }
        return Double.compare(this.y, o.y);
    }

    public Point getBiggerPoint(Point other) {
        return this.compareTo(other) >= 0 ? this : other;
    }

    public Point getSmallerPoint(Point other) {
        return this.compareTo(other) <= 0 ? this : other;
    }

    //Simluating vector thoruh a point
    public Point translationVector(Point other){
        Point p = new Point(other.x - this.x, other.y - this.y);
        return p;
    }


    // Override equals
    @Override
    public boolean equals(Object obj) {
        //Identical object
        if (this == obj)
            return true;
        //Different types, can't compare
        if (obj == null || getClass() != obj.getClass())
            return false;

        //Compare
        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
