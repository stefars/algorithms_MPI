package org.example.cg_attempt;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class Path {

    private List<Coordinate> coordinates = new ArrayList<>();
    private List<Coordinate> attemptedPath = new ArrayList<>();


    //Updates the first node (car)
    public void updatePathStart(Coordinate start){
        Coordinate node = new Coordinate(start);
        this.coordinates.removeFirst();
        this.coordinates.addFirst(node);
    }
    //Updates the last node (flag)
    public void updatePathEnd(Coordinate end){
        Coordinate node = new Coordinate(end);
        this.coordinates.removeLast();
        this.coordinates.add(node);
    }

    public List<Coordinate> modifyPath(Coordinate new_coord, int pos){


        Coordinate node = new Coordinate(new_coord);
        if(attemptedPath.contains(node)){

        }

        this.coordinates.add(pos, node);
        System.out.println(this.coordinates);
        return getCoordinates();
    }

    public List<Coordinate> getAttemptedPath(){
        return attemptedPath;
    }

    public void addAttemptedNode(Coordinate node){
        this.attemptedPath.add(node);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> newPath) {
        this.coordinates = new ArrayList<>(newPath);
        this.attemptedPath.clear();
    }
}