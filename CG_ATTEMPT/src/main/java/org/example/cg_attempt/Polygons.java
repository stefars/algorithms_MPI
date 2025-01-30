package org.example.cg_attempt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "polygon")
public class Polygons {

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Polygon> polygons;

    public Polygons() {}

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    @JsonProperty("polygon")
    public List<Polygon> getPolygons() {
        return polygons;
    }

    public Polygon getPolygon(int index) {
        return polygons.get(index);
    }

    public void setPolygon(Polygon polygon, int index) {
        polygons.remove(index);
        polygons.add(index, polygon);
    }

    @Override
    public String toString() {
        return "Polygons "+ polygons;
    }
}
