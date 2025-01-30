package org.example.cg_attempt;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;

public class XMLParser {

    private final XmlMapper xmlMapper = new XmlMapper();

    public XMLParser() {
    }


    public Polygons getPolygons() {
        try {

            File xmlFile = new File("src/main/resources/polygons3.xml");
            Polygons polygons = xmlMapper.readValue(xmlFile, Polygons.class);
            System.out.println(polygons);
            return polygons;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Polygons getTestPolygons() {
        try {

            File xmlFile = new File("src/main/resources/squareTest.xml");
            Polygons polygons = xmlMapper.readValue(xmlFile, Polygons.class);
            System.out.println(polygons);
            return polygons;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public Polygon getPlayerModel(){
        try {
            File xmlFile = new File("src/main/resources/playerModel2.xml");
            Polygon polygon = xmlMapper.readValue(xmlFile, Polygon.class);
            return polygon;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


}
