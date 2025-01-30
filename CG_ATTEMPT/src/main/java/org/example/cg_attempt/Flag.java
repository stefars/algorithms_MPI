package org.example.cg_attempt;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class Flag {
    private double x;
    private double y;
    private double deltaX;
    private double deltaY;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    public static final double SPEED = 0.5;
    private double half_size = 0.5;
    private Polygon body;
    GeometryFactory geometryFactory = new GeometryFactory();

    public Flag(double x, double y) {

        this.x = x;
        this.y = y;

        initBody();
        setPosition(x, y);
    }

    private void initBody(){
        body = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(half_size, half_size),
                new Coordinate(half_size, -half_size),
                new Coordinate(-half_size,-half_size),
                new Coordinate(-half_size,half_size),
                new Coordinate(half_size,half_size),

        });
    }


    public void setPosition(double x, double y) {

        Coordinate center = body.getCentroid().getCoordinate();
        double xDiff = x-center.getX();
        double yDiff = y-center.getY();

        Coordinate[] coords = body.getCoordinates();
        Coordinate[] newCoords = new Coordinate[coords.length];

        for (int i = 0; i < coords.length; i++) {
            newCoords[i] = new Coordinate(coords[i].getX()+xDiff, coords[i].getY()+yDiff);
        }

        Polygon translatedPolygon = geometryFactory.createPolygon(newCoords);

        body = translatedPolygon;

    }

    public void move(){
        this.x += this.deltaX;
        this.y += this.deltaY;
        if(this.x > 39.4){
            this.x = 39.4;
        }else if(this.x < -39.4){
            this.x = -39.4;
        }
        if(this.y > 39.4){
            this.y = 39.4;
        }
        if(this.y < -39.4){
            this.y = -39.4;
        }
        setPosition(this.x, this.y);
    }

    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }
    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }


    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    void setX(double x) {
        this.x = x;
    }
    void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "FLAG: [" + x + ", " + y + "]";
    }

    public Polygon getBody() {
        return body;
    }
}
