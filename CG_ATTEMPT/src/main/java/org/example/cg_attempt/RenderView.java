package org.example.cg_attempt;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.Map;

public class RenderView {
    private final GraphicsContext context;
    double scaleFactor = 10;
    double xOffset;
    double yOffset;
    double height;
    double width;


    public RenderView(GraphicsContext p) {
        this.context = p;
        this.height = p.getCanvas().getHeight();
        this.width = p.getCanvas().getWidth();
        this.xOffset = p.getCanvas().getWidth()/ 2;
        this.yOffset = p.getCanvas().getHeight() / 2;


    }

    //Draws a point
    public void drawPoint(Coordinate point,double size) {


        double screenX = point.getX()*scaleFactor+xOffset;
        double screenY = -point.getY()*scaleFactor+yOffset;


        //Circle
        context.setFill(Color.BLUE);
        context.fillOval(screenX-size/2, screenY-size/2, size, size);

    }

    //Draws a line between two points
    public void drawLine(Coordinate startPoint, Coordinate endPoint, Color color) {
        double screenX1 = startPoint.getX() * scaleFactor + xOffset;
        double screenY1 = -startPoint.getY() * scaleFactor + yOffset;

        double screenX2 = endPoint.getX() * scaleFactor + xOffset;
        double screenY2 = -endPoint.getY() * scaleFactor + yOffset;

        context.setStroke(color);
        context.strokeLine(screenX1, screenY1, screenX2, screenY2);
    }


    //Draw x and y axis
    public void drawSystem(){
        context.setStroke(Color.DIMGRAY);
        context.strokeLine(xOffset,height,xOffset,0);
        context.strokeLine(0, yOffset, width, yOffset);

        context.setStroke(Color.BLACK);
        context.strokeLine(1,1,width-1,0);
        context.strokeLine(1,1,1,height-1);
        context.strokeLine(width-1,1,width-1,height-1);
        context.strokeLine(1,height-1,width-1,height-1);
    }

    public void drawPolygon(Polygon polygon,Color color) {
        for (Coordinate point : polygon.getCoordinates()) {
            drawPoint(point,5);
        }


        for (int i = 0; i < polygon.getCoordinates().length; i++) {

          Coordinate p1 = polygon.getCoordinates()[i];
          Coordinate p2 = polygon.getCoordinates()[((i + 1) % polygon.getCoordinates().length)];
            drawLine(p1, p2,color);
        }

    }

    public void drawPlayer(Player player) {
        double screenX = player.getX()*scaleFactor+xOffset;
        double screenY = -player.getY()*scaleFactor+yOffset;

        // Calculate screen coordinates with scaling and offsets
        for(Coordinate coord : player.getTransformedBody().getCoordinates()){
            drawPoint(coord,2);
        }

        for (int i = 0; i < player.getBody().getCoordinates().length; i++) {


            Coordinate p1 = player.getTransformedBody().getCoordinates()[i];
            Coordinate p2 = player.getTransformedBody().getCoordinates()[((i + 1) % player.getTransformedBody().getCoordinates().length)];
            drawLine(p1, p2,Color.GREEN);

        }


        //Center point
        context.setFill(Color.RED);
        context.fillOval(screenX - (double) 5/2, screenY - (double) 5/2, 5, 5);

    }

    public void drawFlag(Flag f) {
        double screenX = f.getX()*scaleFactor+xOffset;
        double screenY = -f.getY()*scaleFactor+yOffset;

        for(Coordinate coord : f.getBody().getCoordinates()){
            drawPoint(coord,2);
        }

        for (int i = 0; i < f.getBody().getCoordinates().length; i++) {


            Coordinate p1 = f.getBody().getCoordinates()[i];
            Coordinate p2 = f.getBody().getCoordinates()[((i + 1) % f.getBody().getCoordinates().length)]; // Loop back to the first point
            drawLine(p1, p2,Color.GREEN);

            context.setFill(Color.GREEN);
        }
        context.fillRect(screenX-2.5, screenY-2.5, 5, 5);

        //Center point
        context.setFill(Color.RED);
        context.fillOval(screenX - (double) 5/2, screenY - (double) 5/2, 5, 5);
    }



    public void drawPathNode(Path path) {

        synchronized (path) {
            for (int i = 0; i < path.getCoordinates().size()-1; i++) {
                double screenX = path.getCoordinates().get(i).getX()*scaleFactor+xOffset;
                double screenY = -path.getCoordinates().get(i).getY()*scaleFactor+yOffset;

                context.setFill(Color.RED);
                context.fillOval(screenX-2.5, screenY-2.5, 5, 5);

                Coordinate p1 = path.getCoordinates().get(i);
                Coordinate p2 = path.getCoordinates().get((i + 1)); // Loop back to the first point


                drawLine(p1, p2,Color.GREENYELLOW);
            }
        }



    }

    //Draws the visibility connections
    public void drawVisibilityMap(Map<Coordinate,Map<Coordinate,Double>> visibilityMap) {
        for (Coordinate coord1 : visibilityMap.keySet()) {
            if(!(visibilityMap.get(coord1) == null)){
                for(Coordinate coord2 : visibilityMap.get(coord1).keySet()) {
                    drawLine(coord1,coord2,Color.VIOLET);
                }
            }

        }
    }





//    public void drawPath(Path path) {
//
//        for (int i = 0; i < path.getNodes().size()-1; i++) {
//
//            Point p1 = path.getNodes().get(i);
//            Point p2 = path.getNodes().get(i + 1);// Loop back to the first point
//
//            // Apply the same scaling and translation to the points
//            double screenX1 = p1.getX() * scaleFactor + xOffset;
//            double screenY1 = -p1.getY() * scaleFactor + yOffset;
//
//            double screenX2 = p2.getX() * scaleFactor + xOffset;
//            double screenY2 = -p2.getY() * scaleFactor + yOffset;
//
//            context.setFill(Color.GREEN);
//            context.strokeLine(screenX1, screenY1, screenX2, screenY2);
//        }
//    }


    //Clear function for individual objects are useless, keeping them for rememberence of my own incompetence
    public void clearPlayer(Player player) {

        double screenX = player.getX()*scaleFactor+xOffset;
        double screenY = -player.getY()*scaleFactor+yOffset;
        double orientation = player.getOrientation();

        double angleRadians = Math.toRadians(orientation);


        // Calculate rectangle corners manually
        double halfWidth = Player.WIDTH / 2.0;
        double halfHeight = Player.HEIGHT / 2.0;

        double cosA = Math.cos(angleRadians);
        double sinA = Math.sin(angleRadians);

        //Adjust to delete
        double offset = 0.7;
        double[] xO = {-halfWidth-offset, halfWidth+offset, halfWidth+offset, -halfWidth-offset};
        double[] yO = {-halfHeight-offset, -halfHeight-offset, halfHeight+offset, halfHeight+offset};

        double[] xPoints = new double[4];
        double[] yPoints = new double[4];

        for (int i = 0; i < 4; i++) {
            xPoints[i] = screenX + xO[i] * cosA - yO[i] * sinA;
            yPoints[i] = screenY + xO[i] * sinA + yO[i] * cosA;
        }


        //Draw Player
        context.setFill(Color.WHITE);
        context.fillPolygon(xPoints, yPoints, 4);



    }

    public void clearFlag(Flag f) {

        //Adjust Position
        double screenX = f.getX()*scaleFactor+xOffset;
        double screenY = -f.getY()*scaleFactor+yOffset;

        //Rectangle
        context.setFill(Color.WHITE);
        context.fillRect(screenX - (double) 10/2, screenY - (double) 10/2, 10, 10);

    }


    public void cleanContext() {
        context.setFill(Color.WHITE);
        context.fill();
    }

    public GraphicsContext getContext() {
        return context;
    }

}
