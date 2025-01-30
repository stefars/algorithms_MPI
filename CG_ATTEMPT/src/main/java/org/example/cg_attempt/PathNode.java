package org.example.cg_attempt;

public class PathNode {
    private double x;
    private double y;

    PathNode(double x, double y) {
        this.x = x;
        this.y = y;
    }

    PathNode(Point point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    PathNode(PathNode other) {
        this.x = other.x;
        this.y = other.y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }



}
