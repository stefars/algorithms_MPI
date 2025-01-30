package org.example.cg_attempt;

public class Edge {
    private Point start, end;

    public Edge(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStart() {
        return start;
    }
    public Point getEnd() {
        return end;
    }
}
