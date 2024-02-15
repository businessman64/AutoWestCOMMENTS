package Service;

import model.Signal;
import org.sikuli.script.Location;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class CoordinateService {
    private HashMap<String,Location> objectCoordinates;

    private HashMap<String,Location> objectScreenCoordinates;
    public static CoordinateService coordinateService;
    private CoordinateService() throws ParserConfigurationException, IOException, SAXException {
        //generateCoordinates();
          generateCoordinatesWrangle();
    }
    public static CoordinateService getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(coordinateService==null){
            coordinateService = new CoordinateService();
            return coordinateService;
        }
        else {return coordinateService;}
    }
    private void generateCoordinates() throws ParserConfigurationException, IOException, SAXException {
        objectCoordinates= new HashMap<String,Location>();

        try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/src/main/java/config/coordinates.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the CSV line by commas
                String[] values = line.split(",");

                // Access individual columns by index
                String column1 = values[0];
                String column2 = values[1];
                String column3 = values[2];


                objectCoordinates.put(column1,new Location(Double.parseDouble(column2),Double.parseDouble(column3)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractObjectType(String ObjectType) {
        String regex = "createCtrlSig(Rt|Lt).*?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ObjectType);

        return matcher.matches() ? matcher.group(1) : "";
    }


    private String convertTrainId(String originalTrainId) {
        String regex = ".{3}([A-Z]{3})([A-Z0-9]*).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(originalTrainId);

        if (matcher.matches()) {
            String group1 = matcher.group(1);
            String group2 = matcher.group(2);
            return group1 + "/" + group2;
        } else {
            return originalTrainId;
        }
    }

//    private void gerenateBerthName(){
//        objectCoordinates = new HashMap<>();
//        String regex = "(create.*)\\(cTARGET_WIDGET, (\\w+), (\\d+\\.?\\d*), (\\d+\\.?\\d*)\\)";
//        Pattern pattern = Pattern.compile(regex);
//    }

    private void generateCoordinatesWrangle() throws ParserConfigurationException, IOException, SAXException {
        objectCoordinates = new HashMap<>();
        objectScreenCoordinates = new HashMap<>();
        String regex = "(create.*)\\(cTARGET_WIDGET, (\\w+), (\\d+\\.?\\d*), (\\d+\\.?\\d*)\\)";
        Pattern pattern = Pattern.compile(regex);

        try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/src/main/java/config/coordinates.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {
                    String ObjectType = matcher.group(1);
                    String originalTrainId = matcher.group(2);

                    String trainId = convertTrainId(originalTrainId);


                    double param1 = Double.parseDouble(matcher.group(3));
                    double param2 = Double.parseDouble(matcher.group(4));

                    int calculateScreenX = (int) Math.floor(param1 / 2560.0);
                    double calculateX = Math.abs(Math.abs(calculateScreenX * 2560 - param1) - 733);

                    int calculateScreenY = (int) Math.ceil(param2 / 1440.0) + 1;
                    calculateScreenY = (calculateScreenY >= 2) ? 2 : 1;

                    double calculateY;
                    String objectType = extractObjectType(ObjectType);
                    if (objectType.isEmpty()) {
                        calculateY = Math.abs(calculateScreenY * 1440 - param2) + 890;
                    } else if (objectType.equals("Lt")) {
                        calculateY = Math.abs(calculateScreenY * 1440 - param2) + 880;
                    } else {
                        calculateY = Math.abs(calculateScreenY * 1440 - param2) + 900;
                    }
                    objectScreenCoordinates.put(trainId, new Location(calculateScreenX*2560, calculateScreenY*1440));
                    objectCoordinates.put(trainId, new Location(calculateX, calculateY));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Location getCoordinatedById(String objectId) throws ParserConfigurationException, IOException, SAXException {
        return this.objectCoordinates.get(objectId);
    }

    public Location getScreenCoordinatedById(String objectId) throws ParserConfigurationException, IOException, SAXException {
        return this.objectScreenCoordinates.get(objectId);
    }
}
