package org.example.cg_attempt;

import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;


public class Player {

    private double x;
    private double y;
    private double dx;
    private double dy;
    public final static double WIDTH = 20;
    public final static double HEIGHT = 10;
    public final static double SPEED = 0.25;

    private double orientation;
    private static Flag flag;
    public int currentNode = 0;
    private org.locationtech.jts.geom.Polygon body;
    private org.locationtech.jts.geom.Polygon transformedBody;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final double half_width = 0.5;
    private final double half_height = 1;
    private Polygon playerBody;


    public Player(double x, double y, double orientation,Flag flag) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
        this.flag = flag;


        //Here are two ways to create the body.
        getPlayerModel(); //Custom body (from the xml file)
        //initBody(); //Default body
    }



    //Get player model from xml file
    public void getPlayerModel(){
        XMLParser parser = new XMLParser();
        GeometryFactory gf = new GeometryFactory();
        org.example.cg_attempt.Polygon model = parser.getPlayerModel();

        Coordinate[] coords = new Coordinate[model.getPoints().size()];
        for(int i = 0; i < model.getPoints().size(); i++){
            Coordinate new_coordinate = new Coordinate(model.getPoints().get(i).getX(), model.getPoints().get(i).getY());
            coords[i] = new_coordinate;
        }

        MultiPoint points = gf.createMultiPoint(coords);

        ConvexHull convexHull = new ConvexHull(points);
        Geometry convexHullGeometry = convexHull.getConvexHull();

        if(convexHullGeometry instanceof Polygon){
            body = (Polygon) convexHullGeometry;
            transformedBody = (Polygon) convexHullGeometry;
        }


        setPosition(x, y);

    }


    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }



    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getDx() {
        return dx;
    }
    public double getDy() {
        return dy;
    }
    public double getOrientation() {
        return orientation;
    }

    public Flag getFlag() {
        return flag;
    }


    // Construct template body and transformBody
    private void initBody(){
        body = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(half_height, half_width),
                new Coordinate(half_height, -half_width),
                new Coordinate(-half_height,-half_width),
                new Coordinate(-half_height,half_width),
                new Coordinate(half_height,half_width),

        });

        transformedBody = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(half_width, half_height),
                new Coordinate(half_width, -half_height),
                new Coordinate(-half_width,-half_height),
                new Coordinate(-half_width,half_height),
                new Coordinate(half_width,half_height),

        });

        setPosition(x, y);


    }

    // Rotate transform body
    public void rotate(){

        transformedBody = Operations.rotate(body, getOrientation());
    }

    // Move basic & transformed body (with rotation)
    public void setPosition(double x, double y) {

        body = Operations.setPosition(x,y,body);
        rotate();

    }

    public org.locationtech.jts.geom.Polygon getBody() {
        return body;
    }

    public org.locationtech.jts.geom.Polygon getTransformedBody() {
        return transformedBody;
    }

    // Player orientation towards flag
    public void findFlagOrientation() {
        dx = flag.getX() - this.x; // Change in X
        dy = flag.getY() - this.y; // Change in Y

        orientation = Math.atan2(dy, dx);
    }

    // Player orientation towards a node
    public void findNodeOrientation(Coordinate node) {
        dx = node.getX() - this.x;
        dy = node.getY() - this.y;

        orientation = Math.atan2(dy, dx);
    }

    //Get the centroid of the object collided with
    public double findCollisionOrientation(Polygon polygon) {
        Coordinate centroid = polygon.getCentroid().getCoordinate();
        double cdx = centroid.getX() - this.x;
        double cdy = centroid.getY() - this.y;

        return Math.atan2(cdy, cdx);
    }



    //Function for player to navigate the path
    public void navigatePath(Path path) {
        //If next destination is flag, move towards it
        if(currentNode == path.getCoordinates().size()) {
            move();
            return;
        }

        //if next destination is node move towards node
        Coordinate node = path.getCoordinates().get(currentNode);
        if(reachedNode(node)==1){
            if(currentNode!=0){
                path.getCoordinates().remove(currentNode-1);
            }else{
                currentNode++;
            }
        }
        findNodeOrientation(node);
        moveForwardByAngle();

    }

    //Move along the orientation
    public void moveForwardByAngle(){
        x+= SPEED*Math.cos(orientation);
        y+= SPEED*Math.sin(orientation);
        setPosition(this.x, this.y);
    }



    public void moveBackwardByAngle(){
        x-= 1*Math.cos(orientation);
        y-= 1*Math.sin(orientation);
        setPosition(this.x, this.y);
    }


    public int reachedNode(Coordinate node){
        if(x < node.getX()+0.5 && x > node.getX()-0.5 && y < node.getY()+0.5 && y > node.getY()-0.5){
            System.out.println("reached node");
            return 1;
        }
        return 0;
    }

    public int reachedDestination(){
        if (x <flag.getX()+1 && x > flag.getX()-1 && y < flag.getY()+1 && y > flag.getY()-1){
            System.out.println("DESTINATION REACHED");
            return 1;
        }
        return 0;
    }

    //Move towards flag
    public void move(){
        if(reachedDestination()==1) {
            System.out.println("Reached Flag");
            return;
        }

        findFlagOrientation();
        moveForwardByAngle();

    }

    public void onCollision(){
       moveBackwardByAngle();

        //Move
        setPosition(this.x, this.y);

    }

    public String toString(){
        return "PLAYER ["+x+", "+y+"]" + "\nOrientation: [" + orientation+"]";
    }


}
