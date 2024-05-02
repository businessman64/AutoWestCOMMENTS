package util;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.scene.control.SelectionMode;
import model.*;
import org.slf4j.ILoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLParser {
    private static final Logger logger = Logger.getLogger(XMLParser.class.getName());
    private static final File railwayRoutes = new File(System.getProperty("user.dir") + "/src/main/java/data/RailwayData/RailwayRoutes.xml");


    public static List<TrackInfo> parseTracks(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pathFinder(inputFile,"RailwayData"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Track");
        Set<TrackInfo> trackInfos = new HashSet<>();

        for (int temp = 0; temp < nList.getLength(); temp++) {
            String trackId = "";
            String trackName = "";
            String trackNormalDown ="";
            String trackNormalUp="";
            String trackReverseDown="";
            String TrackReverseUp="";
            String circuitName="";
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                trackId = eElement.getAttribute("id");
                circuitName = eElement.getAttribute("circuit_name");
                String[] name = circuitName.split("/");
                trackName = name[0];
                NodeList trackNodes = eElement.getElementsByTagName("tracks");
                if (trackNodes.getLength() == 1) {
                    Element trackElement = (Element) trackNodes.item(0);
                    trackNormalDown = trackElement.getAttribute("dnn");
                    trackNormalUp = trackElement.getAttribute("upn");
                    trackReverseDown = trackElement.getAttribute("dnr");
                    TrackReverseUp = trackElement.getAttribute("upr");

                }

            trackInfos.add(new TrackInfo(trackId,circuitName,trackNormalDown,trackNormalUp,trackReverseDown,TrackReverseUp));

            }
        }
        return new ArrayList<>(trackInfos);
    }


    public static List<BerthInfo> parseBerth(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(pathFinder(inputFile,"RailwayData"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Berth");
        Set<BerthInfo> berthInfo = new HashSet<>();

        for (int temp = 0; temp < nList.getLength(); temp++) {
            String berthId = "";
            String direction = "";
            int capacity =0;
            String trackId="";

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                berthId = eElement.getAttribute("id");
                direction = eElement.getAttribute("direction");
                capacity = Integer.parseInt(eElement.getAttribute("capacity"));

                NodeList trackNodes = eElement.getElementsByTagName("track");
                if (trackNodes.getLength() == 1) {
                    Element trackElement = (Element) trackNodes.item(0);
                    trackId = trackElement.getAttribute("id");


                }

                berthInfo.add(new BerthInfo(berthId,trackId,capacity,direction,"",null));

            }
        }
        return new ArrayList<>(berthInfo);
    }

    public static List<OverViewBerth> parseOverViewBerth(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(pathFinder(inputFile,"RailwayData"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("OverviewBerth");
        Set<OverViewBerth> overViewBerths = new HashSet<>();

        for (int temp = 0; temp < nList.getLength(); temp++) {
            List<String> berthId = new ArrayList<>();
            String overViewId = "";

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                overViewId = eElement.getAttribute("id");
                NodeList berthNodes = eElement.getElementsByTagName("berth");
                for (int i = 0; i < berthNodes.getLength(); i++) {
                    Element trackElement = (Element) berthNodes.item(i);
                    berthId.add(trackElement.getAttribute("id"));
                }

                overViewBerths.add(new OverViewBerth(overViewId,berthId));

            }
        }
        return new ArrayList<>(overViewBerths);
    }


    public static String parseGetRequest(String body) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(body);
        doc.getDocumentElement().normalize();
        logger.info("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("");
        List<String> al= new ArrayList<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if(eElement.hasAttribute("description")){
                    return eElement.getAttribute("description");
                }

            }
        }
        return null;
    }
    public static SignalRsmMessage parseRailwayStateManagerMessageForSignal(String body) throws ParserConfigurationException, IOException, SAXException {
        SignalRsmMessage rsmMessage = new SignalRsmMessage();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(body)));
        doc.getDocumentElement().normalize();
        System.out.println(doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("signal");
        List<String> al= new ArrayList<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if(eElement.hasAttribute("vital-blocked")){
                    if(eElement.getAttribute("vital-blocked").equals("1")){
                        rsmMessage.setVitalBlocked(true);
                    }else{
                        rsmMessage.setVitalBlocked(false);
                    }
                }
                if(eElement.hasAttribute("disregarded")){
                    if(eElement.getAttribute("disregarded").equals("1")){
                        rsmMessage.setDisregarded(true);
                    }else{
                        rsmMessage.setDisregarded(false);
                    }
                }
                if(eElement.hasAttribute("comms-failed")){
                    if(eElement.getAttribute("comms-failed").equals("1")){
                        rsmMessage.setCommsFailed(true);
                    }else{
                        rsmMessage.setCommsFailed(false);
                    }
                }
                if(eElement.hasAttribute("fleeting")){
                    if(eElement.getAttribute("fleeting").equals("1")){
                        rsmMessage.setFleetingOn(true);
                    }else{
                        rsmMessage.setFleetingOn(false);
                    }
                }
                if(eElement.hasAttribute("in-ars-mode")){
                    if(eElement.getAttribute("in-ars-mode").equals("1")){
                        rsmMessage.setArsOn(true);
                    }else{
                        rsmMessage.setArsOn(false);
                    }
                }
                if(eElement.hasAttribute("route-set")){
                    if(eElement.getAttribute("route-set").equals("1")){
                        rsmMessage.setArsOn(true);
                    }else{
                        rsmMessage.setArsOn(false);
                    }
                }
                if(eElement.hasAttribute("route-set-in-rear")){
                    if(eElement.getAttribute("route-set-in-rear").equals("1")){
                        rsmMessage.setRouteSetInRear(true);
                    }else{
                        rsmMessage.setRouteSet(false);
                    }
                }

            }
        }
        return rsmMessage;
    }
    public static List<String> parsePoints(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pathFinder(inputFile,"RailwayData"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Track");
        Set<String> hashset = new HashSet<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if(eElement.hasAttribute("point")){
                    hashset.add(eElement.getAttribute("id"));
                }
            }
        }
        return new ArrayList<>(hashset);
    }


    public static List<RouteSetInfo> parseControlled(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pathFinder(inputFile, "ControlTables"));
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("control_table");
        Set<RouteSetInfo> routeSetInfos = new HashSet<>();

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String routeId = eElement.getAttribute("id");
                String region = getTextContentOfTag(eElement, "region");
                String routeType = getTextContentOfTag(eElement, "route_type");
                String frontContactObjects = getTextContentOfTag(eElement, "front_contact_objects");
                List<PointRouteSetInfo> points = new ArrayList<>();

                // Handle points_normal
                extractPoints(points, eElement, "points_normal", "normal");

                // Handle points_reverse with unless_pseudo_reverse
                extractPointsReverse(points, eElement);

                routeSetInfos.add(new RouteSetInfo(routeId, "Controlled", region, routeType, points, frontContactObjects, ""));
            }
        }

        return new ArrayList<>(routeSetInfos);
    }

    private static void extractPointsReverse(List<PointRouteSetInfo> points, Element eElement) {
        NodeList pointsReverseList = eElement.getElementsByTagName("points_reverse");

        for (int i = 0; i < pointsReverseList.getLength(); i++) {
            NodeList pointList = pointsReverseList.item(i).getChildNodes();
            String unlessPseudoReverse = "";
            String id="";
            for (int j = 0; j < pointList.getLength(); j++) {
                Node pNode = pointList.item(j);
                if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element pElement = (Element) pNode;
                    if ("point".equals(pElement.getTagName())) {
                        id = pElement.getAttribute("id");
                        String state = pElement.getAttribute("state");
                        }

                    if("unless_pseudo_reverse".equals(pElement.getTagName())) {
                            NodeList unlessNodes = pElement.getElementsByTagName("detected");
                            if (unlessNodes.getLength() > 0) {
                                unlessPseudoReverse = getTextContentOfTag(eElement, "detected");
                                //unlessPseudoReverse = getTextContentOfTag(unlessElement, "detected");
                            }
                        }
                        points.add(new PointRouteSetInfo(id, "reverse", unlessPseudoReverse));
                    }
                }
            }
        }


    private static void extractPoints(List<PointRouteSetInfo> points, Element eElement, String tagName, String pointType) {
        NodeList pointsList = eElement.getElementsByTagName(tagName);
        for (int i = 0; i < pointsList.getLength(); i++) {
            NodeList pointList = pointsList.item(i).getChildNodes();
            for (int j = 0; j < pointList.getLength(); j++) {
                Node pNode = pointList.item(j);
                if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element pElement = (Element) pNode;
                    String id = pElement.getAttribute("id");
                    String state = pElement.getAttribute("state");
                    points.add(new PointRouteSetInfo(id, pointType, ""));
                }
            }
        }
    }

    private static String getTextContentOfTag(Element element, String tagName) {
        NodeList nl = element.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Node node = nl.item(0);
            if (node != null) {
                return node.getTextContent();
            }
        }
        return ""; // Return empty string if not found or null
    }
public static List<SignalInfo> parseSignals(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pathFinder(inputFile,"RailwayData"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Signal");
        Set<SignalInfo> signalInfos = new HashSet<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String signalId  = "";
                String trackID  = "";
                String relation = "";
                String type = "";
                String arsDisabled ="";

                Boolean hasArs = false;

                if(eElement.hasAttribute("type")){
                    signalId = eElement.getAttribute("id");
                    type = eElement.getAttribute("type");
                    arsDisabled = eElement.getAttribute("ars_disabled");
                    hasArs = !arsDisabled.equals("true");

                    NodeList trackNodes = eElement.getElementsByTagName("track");
                    if (trackNodes.getLength() == 1) {
                        Element trackElement = (Element) trackNodes.item(0);
                        trackID = trackElement.getAttribute("id");
                        relation = trackElement.getAttribute("relation");
                    }
                }
                signalInfos.add(new SignalInfo(signalId,type,trackID,relation,hasArs ));
            }

        }
        return new ArrayList<>(signalInfos);
    }

    public static List<String> parseCSV(String inputFile)  {
        List<String> values = new ArrayList<>();
        String line = "";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            // If there's a header and you want to skip it
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Assuming there's no comma since it's a single column
                values.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return values;
    }

    public static List<RouteInfo> parseRoute(String inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pathFinder(inputFile,"RailwayData"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Route");
        List<RouteInfo> routeInfos = new ArrayList<>();

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String routeId = eElement.getAttribute("id");
                String direction = eElement.getAttribute("direction");
                int precedence = Integer.parseInt(eElement.getAttribute("precedence"));
                String entrySignal = "";
                String exitSignal = "";
                List<String> routetracks = new ArrayList<>();
                List<String> conflictingRoutes = new ArrayList<>();

                // Process signals
                NodeList signalNodes = eElement.getElementsByTagName("signals");
                if (signalNodes.getLength() > 0) {
                    NodeList signalChildren = signalNodes.item(0).getChildNodes();
                    for (int i = 0; i < signalChildren.getLength(); i++) {
                        Node signalNode = signalChildren.item(i);
                        if (signalNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element signalElement = (Element) signalNode;
                            if ("entry".equals(signalElement.getTagName())) {
                                entrySignal = signalElement.getAttribute("id");
                            } else if ("exit".equals(signalElement.getTagName())) {
                                exitSignal = signalElement.getAttribute("id");
                            }
                        }
                    }
                }

                // Process tracks
                NodeList trackNodes = eElement.getElementsByTagName("track");
                for (int i = 0; i < trackNodes.getLength(); i++) {
                    Element trackElement = (Element) trackNodes.item(i);
                    routetracks.add(trackElement.getAttribute("id"));
                }

                // Process conflicting routes
                NodeList conflictingRouteNodes = eElement.getElementsByTagName("conflicting_routes");
                if (conflictingRouteNodes.getLength() > 0) {
                    NodeList conflictingRouteNodesChildren = conflictingRouteNodes.item(0).getChildNodes();
                    for (int i = 0; i < conflictingRouteNodesChildren.getLength(); i++) {
                        Node node = conflictingRouteNodesChildren.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element routeElement = (Element) node;
                            conflictingRoutes.add(routeElement.getAttribute("id"));
                        }
                    }
                }

                routeInfos.add(new RouteInfo(routeId, direction,precedence, entrySignal, exitSignal, routetracks, conflictingRoutes));
            }
        }

        return routeInfos;

    }

    public static RsmMessage parseRailwayStateManagerMessage(String body) throws ParserConfigurationException, IOException, SAXException {
        RsmMessage rsmMessage = new RsmMessage();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(body)));
        doc.getDocumentElement().normalize();
        System.out.println(doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("track");
        List<String> al= new ArrayList<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if(eElement.hasAttribute("vital-blocked")){
                    if(eElement.getAttribute("vital-blocked").equals("1")){
                        rsmMessage.setVitalBlocked(true);
                    }else{
                        rsmMessage.setVitalBlocked(false);
                    }
                }
                if(eElement.hasAttribute("disregarded")){
                    if(eElement.getAttribute("disregarded").equals("1")){
                        rsmMessage.setDisregarded(true);
                    }else{
                        rsmMessage.setDisregarded(false);
                    }
                }
                if(eElement.hasAttribute("comms-failed")){
                    if(eElement.getAttribute("comms-failed").equals("1")){
                        rsmMessage.setCommsFailed(true);
                    }else{
                        rsmMessage.setCommsFailed(false);
                    }
                }
                if(eElement.hasAttribute("keyed-normal")){
                    if(eElement.getAttribute("keyed-normal").equals("1")){
                        rsmMessage.setNormalState(true);
                    }else{
                        rsmMessage.setNormalState(false);
                    }
                }
                if(eElement.hasAttribute("keyed-centre")){
                    if(eElement.getAttribute("keyed-centre").equals("1")){
                        rsmMessage.setCentreState(true);
                    }else{
                        rsmMessage.setCentreState(false);
                    }
                }
                if(eElement.hasAttribute("keyed-reverse")){
                    if(eElement.getAttribute("keyed-reverse").equals("1")){
                        rsmMessage.setReverseState(true);
                    }else{
                        rsmMessage.setReverseState(false);
                    }
                }

            }
        }
        return rsmMessage;
    }

    public static File pathFinder(String objectFilename, String PathName){

        File primaryFile = new File("/opt/scada/data/"+PathName+"/"+objectFilename);
        File secondaryFile = new File(System.getProperty("user.dir") + "/src/main/java/data/"+PathName+"/"+objectFilename);

        File targetFile;

        if (primaryFile.exists()) {
            // If the primary file exists, use it
            targetFile = primaryFile;
        } else if (secondaryFile.exists()) {
            // If the primary file doesn't exist, but the secondary file does, use the secondary file
            targetFile = secondaryFile;
        } else {
            // Handle the case where neither file exists
            System.err.println("Neither file was found.");
            // Optionally, set targetFile to null or handle this case as needed
            targetFile = null;
        }

        if (targetFile != null) {
            // Proceed with using targetFile
            System.out.println("Using file: " + targetFile.getPath());
            // Your file processing logic here
        }

        return targetFile;
    }


    public static String parseExitSignal(String entrySignalId) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(railwayRoutes);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("signals");
        Set<String> hashset = new HashSet<>();
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);//Routes
            int foundFlag = -1;
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList children = nNode.getChildNodes();
                for(int i=0;i<children.getLength();i++){
                    Node node = children.item(i);
                    if(node.getNodeType() == Node.ELEMENT_NODE){
                        Element ele = (Element)node;
                        if(ele.getTagName().equals("entry") && ele.getAttribute("id").equals(entrySignalId)){
                           foundFlag = temp;
                        }
                    }
                }
            }
            if(temp == foundFlag){
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList children = nNode.getChildNodes();
                    for(int i=0;i<children.getLength();i++){
                        Node node = children.item(i);
                        if(node.getNodeType() == Node.ELEMENT_NODE){
                            Element ele = (Element)node;
                            if(ele.getTagName().equals("exit")){
                                foundFlag = temp;
                                return ele.getAttribute("id");
                            }
                        }
                    }
                }
            }

        }
        return null;
    }
}
