package org.example.cg_attempt;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class HelloApplication extends Application {

    //Grid options
    private static final int width = 800;
    private static final int height = 800;
    private static final int SCALE = 10;

    private static final int FRAME_DURATION = 30;

    //Polygon utilities
    private static final XMLParser xmlParser = new XMLParser();
    private static Polygons parser_polygons = new Polygons();
    public static List<Polygon> polygons = new ArrayList<>();
    private static VisibilityGraph visibilityGraph = new VisibilityGraph();
    private static GeometryFactory geometryFactory = new GeometryFactory();
    public static List<Polygon> resizedPolygons = new ArrayList<>();


    //For Print and Intersections
    private static int algorithmPicker = 0;
    public static boolean is_in_buffer = false;
    public static int flagIntersectionIndex = -1;
    public static int flagBufferIntersectionIndex = -1;
    public static int playerBufferIntersectionIndex = -1;
    public static int playerIntersectionIndex = -1;
    public static boolean buffer_or_polygons = false;//False for buffer, true for polygons
    private static boolean show_visibility_graph = false;

    private int[] pressedCounter = {0, 0, 0, 0}; //Used to detect key activations

    //UI
    private VBox infoPanel;
    private Label Alg1Time;
    private Label Alg2Time;
    private java.time.Duration alg1Duration;
    private java.time.Duration alg2Duration;
    private Label visibilityTypeLabel;
    private Label nrOfVerticesLabel;
    private Label nrOfPathNodesLabel;

    //Relevant Objects
    public static Player player;
    public static Flag flag;
    private Path path;
    private Coordinate startPoint;
    private Coordinate endPoint;

    //Executes thread
    private ExecutorService executor;

    @Override
    public void start(Stage primaryStage) throws Exception {
        executor = Executors.newFixedThreadPool(3); // Initialize executor

        // Initialize objects that take part in the simulation

        flag = new Flag(35, 22);
        player = new Player(-30, 0, 0, flag);

        path = new Path();

        primaryStage.setTitle("Simulation");
        simulation(primaryStage);
    }


    //UI
    public Canvas setUI(Stage stage) {
        Canvas canvas = new Canvas(width, height);
        infoPanel = new VBox();
        Label flagPos = new Label(flag.toString());
        Label playerPos = new Label(player.toString());
        infoPanel.getChildren().add(flagPos);
        infoPanel.getChildren().add(playerPos);

        HBox hbox = new HBox(canvas);
        hbox.getChildren().add(infoPanel);
        Scene scene = new Scene(hbox, 1200, height);


        Button Pause = new Button("Pause");
        Pause.setOnAction(e -> {
            algorithmPicker = 0;
        });
        //for Dijkstra
        HBox Alg1 = new HBox();
        Alg1Time = new Label("Exec. Time: ");

        Button Djistrka = new Button("Dijkstra's Algorithm");
        Djistrka.setOnAction(event -> {
            algorithmPicker = 1;
            System.out.println(algorithmPicker);
            checkAndRegeneratePath();
        });

        //For A*
        HBox Alg2 = new HBox();
        Alg2Time = new Label("Exec. Time: ");
        Button AStar = new Button("A* Algorithm");
        AStar.setOnAction(event -> {
            algorithmPicker = 2;
            System.out.println(algorithmPicker);
            checkAndRegeneratePath();
        });

        //For visibility map generation
        HBox visibilityType = new HBox();
        this.visibilityTypeLabel = new Label("Visibility Type: Buffer with Buffer");
        Button visibilityTypeButton = new Button("Change Type");
        visibilityTypeButton.setOnAction(event -> {
            if(buffer_or_polygons) {
                buffer_or_polygons = false;
                this.visibilityTypeLabel.setText("Visibility Type: Buffer with Buffer");

            }else{
                buffer_or_polygons = true;
                this.visibilityTypeLabel.setText("Visibility Type: Buffer with Polygons");
            }
        });


        Button visibilityGraphButton = new Button("Show Visibility Graph");
        visibilityGraphButton.setOnAction(event -> {
            if(show_visibility_graph){
                show_visibility_graph = false;
                visibilityGraphButton.setText("Show Visibility Graph");
            }else{
                show_visibility_graph = true;
                visibilityGraphButton.setText("Hide Visibility Graph");
            }
        });

        nrOfVerticesLabel = new Label("Number of Vertices: ");
        nrOfPathNodesLabel = new Label("Number of Node Paths: ");

        Button resetPositionButton = new Button("Reset Position");
        resetPositionButton.setOnAction(event -> {
            player.setX(-30);
            player.setY(0);
            flag.setX(30);
            flag.setY(22);
            player.setPosition(-30,0);
        });

        Alg1.getChildren().add(Djistrka);
        Alg1.getChildren().add(Alg1Time);
        Alg2.getChildren().add(AStar);
        Alg2.getChildren().add(Alg2Time);

        visibilityType.getChildren().add(visibilityTypeButton);
        visibilityType.getChildren().add(visibilityTypeLabel);

        infoPanel.getChildren().add(Pause);
        infoPanel.getChildren().add(Alg1);
        infoPanel.getChildren().add(Alg2);
        infoPanel.getChildren().add(visibilityType);
        infoPanel.getChildren().add(visibilityGraphButton);
        infoPanel.getChildren().add(nrOfVerticesLabel);
        infoPanel.getChildren().add(nrOfPathNodesLabel);
        infoPanel.getChildren().add(resetPositionButton);

        stage.setScene(scene);
        stage.show();
        addKeyListeners(scene);
        return canvas;
    }

    public void simulation(Stage stage) {
        Canvas canvas = setUI(stage);

        // Get unrefined polygons
        parser_polygons = xmlParser.getPolygons();
        int nr_of_vertices = 0;

        // Refine them for good use (Add them to the actual polygon list)
        for (int i = 0; i < parser_polygons.getPolygons().size(); i++) {
            //I get una bucata polygon styled by me (with points and edges), I have to take each point, and make it a coordinate.
            List<Point> points = parser_polygons.getPolygons().get(i).getPoints();

            // Create a Coordinate array for the exterior ring
            Coordinate[] exteriorCoordinates = new Coordinate[points.size()+1];

            // Populate the Coordinate array with the points
            for (int j = 0; j < points.size(); j++) {
                Point point = points.get(j);
                exteriorCoordinates[j] = new Coordinate(point.getX(), point.getY());
            }

            exteriorCoordinates[points.size()] = new Coordinate(points.get(0).getX(), points.get(0).getY());

            MultiPoint multiPoint = geometryFactory.createMultiPoint(exteriorCoordinates);

            ConvexHull convexHull = new ConvexHull(multiPoint);
            Geometry convexHullGeometry = convexHull.getConvexHull();

            if(convexHullGeometry instanceof Polygon polygon) {
                polygons.add(polygon);
                nr_of_vertices+= polygon.getCoordinates().length-1;
            }



            System.out.println(Arrays.toString(exteriorCoordinates));
        }


        startPoint = new Coordinate(player.getX(), player.getY());
        endPoint = new Coordinate(flag.getX(), flag.getY());

        nrOfVerticesLabel.setText("Number of Vertices: " + nr_of_vertices);
        GraphicsContext context = canvas.getGraphicsContext2D();
        RenderView view = new RenderView(context);
        synchronized (visibilityGraph){
            visibilityGraph.createGraph(startPoint,endPoint);
        }

        int bufferNodes = 0;
        for(Polygon polygon : resizedPolygons){
            bufferNodes += polygon.getCoordinates().length-1;
        }
        nrOfPathNodesLabel.setText("Number of Node Paths: " + bufferNodes);


        // Main simulation loop
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(FRAME_DURATION), e -> {
            simulationRunner(context, view);
            updateUI();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void addKeyListeners(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> {
                    flag.setDeltaY(Flag.SPEED);
                    if (pressedCounter[0] == 0) {
                        pressedCounter[0]++;
                    }
                }
                case S -> {
                    flag.setDeltaY(-Flag.SPEED);
                    if (pressedCounter[1] == 0) {
                        pressedCounter[1]++;
                    }
                }
                case A -> {
                    flag.setDeltaX(-Flag.SPEED);
                    if (pressedCounter[2] == 0) {
                        pressedCounter[2]++;
                    }
                }
                case D -> {
                    flag.setDeltaX(Flag.SPEED);
                    if (pressedCounter[3] == 0) {
                        pressedCounter[3]++;
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W -> {
                    flag.setDeltaY(0);
                    pressedCounter[0] = 0;
                    checkAndRegeneratePath();

                }
                case S -> {
                    flag.setDeltaY(0);
                    pressedCounter[1] = 0;
                    checkAndRegeneratePath();

                }
                case A -> {
                    flag.setDeltaX(0);
                    pressedCounter[2] = 0;
                    checkAndRegeneratePath();
                }
                case D -> {
                    flag.setDeltaX(0);
                    pressedCounter[3] = 0;
                    checkAndRegeneratePath();
                }
            }
        });
    }

    private void checkAndRegeneratePath() {
        for (int nr : pressedCounter) {
            if (nr != 0) {
                return;
            }
        }
        if (algorithmPicker!=0) {
            regeneratePath();
        }


    }

    //Collision thread
    private void handleCollisions(){

        executor.submit(() ->{
            synchronized (path) {

                //Verify if flag is inside a polygon
                for (int i = 0; i < polygons.size(); i++) {
                    if (visibilityGraph.isShapeInPolygon(HelloApplication.flag.getBody(), polygons.get(i))) {
                        System.out.println("Flag hit polygon index: " + i);
                        flagIntersectionIndex = i;
                        break;
                    }
                    flagIntersectionIndex = -1;
                }


                //Verify if player is inside a polygon
                for (int i = 0; i < polygons.size(); i++) {
                    if (visibilityGraph.isShapeInPolygon(HelloApplication.player.getTransformedBody(), polygons.get(i))) {
                        System.out.println("Player hit polygon: " + i);
                        playerIntersectionIndex = i;
                        player.onCollision();
                        visibilityGraph.createGraph(startPoint, endPoint);

                        System.out.println("Do i Get here?");

                        Coordinate closestPoint = Operations.findClosestPoint(startPoint, resizedPolygons.get(playerIntersectionIndex), path);
                        System.out.println(closestPoint);
                        boolean flag;
                        //Duct tape patch for infinte loop
                        if(closestPoint==null) {
                            flag = true;
                        } else {
                            flag = false;
                        }

                        Platform.runLater(() -> {
                            if(flag){
                                regeneratePath();
                            }else{
                                path.modifyPath(closestPoint, 1);
                            }

                            visibilityGraph.printShortestPath(startPoint,endPoint);
                        });

                    }
                    playerIntersectionIndex = -1;
                }
            }
        });
    }

    //Path regeneration thread
    private void regeneratePath() {
        executor.submit(() -> {
            try {
                synchronized (visibilityGraph) {
                    //If flag is inside a polygon
                    if (visibilityGraph.getVisibilityGraph().isEmpty()) {
                        path.setCoordinates(new ArrayList<>());
                        return;
                    }


                    // Recalculate the shortest path
                    List<Coordinate> newPath = null;
                    Instant start = Instant.now();

                    visibilityGraph.createGraph(startPoint, endPoint);

                    //For dijsktra
                    if (algorithmPicker == 1) {

                        newPath = visibilityGraph.DijkstraAlgorithm(startPoint, endPoint);
                        Instant end = Instant.now();
                        alg1Duration = java.time.Duration.between(start, end);
                    }

                    //A*
                    if (algorithmPicker == 2) {

                        newPath = visibilityGraph.AStarAlgorithm(startPoint, endPoint);
                        Instant end = Instant.now();
                        alg2Duration = java.time.Duration.between(start, end);
                    }



                    //Update the path in javafx thread
                    List<Coordinate> finalNewPath = newPath;
                    Platform.runLater(() -> {
                        path.setCoordinates(finalNewPath);
                        visibilityGraph.printShortestPath(startPoint, endPoint);
                    });
                }} catch(Exception e){
                    e.printStackTrace();
                }
            });
    }

    private void simulationRunner(GraphicsContext context, RenderView view) {
        synchronized (visibilityGraph) {
          visibilityGraph.createGraph(startPoint, endPoint);
            //visibilityGraph.addStartEndPoint(startPoint, endPoint); //Trying to make it more efficent
        }

        context.clearRect(0, 0, width, height);

        // Update start/end points
        startPoint.setCoordinate(new Coordinate(player.getX(), player.getY()));
        endPoint.setCoordinate(new Coordinate(flag.getX(), flag.getY()));



        if(algorithmPicker!=0){
        if(!path.getCoordinates().isEmpty()) {
                path.updatePathEnd(endPoint);
                path.updatePathStart(startPoint);
                playerMove();
        }}


        handleCollisions();



        // Redraw everything
        view.drawSystem();
        for (int i = 0; i < polygons.size(); i++) {
            if(i == flagIntersectionIndex){
                view.drawPolygon(polygons.get(i), Color.ORANGERED);
                continue;
            }
            view.drawPolygon(polygons.get(i), Color.BLACK);
        }
        for (Polygon polygon : resizedPolygons) {
            view.drawPolygon(polygon, Color.DARKCYAN);
        }


        if((!(visibilityGraph.getVisibilityGraph() == null) && show_visibility_graph && flagIntersectionIndex==-1)){
            synchronized (visibilityGraph) {
                view.drawVisibilityMap(visibilityGraph.getVisibilityGraph());
            }

        }

        view.drawPlayer(player);
        view.drawFlag(flag);
        view.drawPathNode(path);


        // Flag movement
        flag.move();



    }

    public void updateUI() {
        Label flagPos = (Label) infoPanel.getChildren().get(0);
        Label playerPos = (Label) infoPanel.getChildren().get(1);
        flagPos.setText(flag.toString());
        playerPos.setText(player.toString());
        if(alg1Duration != null){
            Alg1Time.setText("Execution time: " + alg1Duration.toNanos()+" nsec");
        }
        if(alg2Duration != null){
            Alg2Time.setText("Execution time: " + alg2Duration.toNanos()+" nsec");
        }


    }


    public void playerMove(){
        for(int nr : pressedCounter) {
            if (nr != 0) {
                return;
            }
        }
        player.navigatePath(path);

    }

    @Override
    public void stop() {
        executor.shutdown(); //clear threads
    }

    public static void main(String[] args) {
        launch(args);
    }
}
