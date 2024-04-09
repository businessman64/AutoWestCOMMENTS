package Service;

import com.jcraft.jsch.JSchException;
import exceptions.NetworkException;
import exceptions.ObjectStateException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;
import model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sikuli.script.*;
import org.xml.sax.SAXException;
import util.CmdLine;
import util.GifSequenceWriter;
import util.SSHManager;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import java.nio.file.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

enum RouteState{
    routeSet,
    Navigation,
    LowSpeed,
    Approach,
    SPAD,
    Blocked,
    Disregard,
    Conflict
}

public class RouteService {
    public static RouteService routeService;

    public  static ScreenService screenService;
    private List<Route> routes;

    private final StringProperty textToUpdate = new SimpleStringProperty();
    private final StringProperty conflictTextToUpdate = new SimpleStringProperty();

    InterlockingService interlockingService;
    CoordinateService coordinateService;
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());
    boolean navigation=false ;
    boolean isConflictRoute=false;
    boolean spad = false;
    boolean lowSpeed = false;
    boolean approach = false;
    boolean stepping = false;

    SignalService signalService;

    TrackService trackService;
    View currentView;

    boolean isClear = false;
    Route additionalBRoute;
    Route additionalFRoute;
    SSHManager sshManager;
    SSHManager sshManager_sim;
    StationMode stationMode = StationMode.getInstance();

    public RouteService() throws ParserConfigurationException, IOException, SAXException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();
        signalService = new SignalService();
        trackService = new TrackService();
       // SM=new StationMode();
        generateRoutes();

            }

    private void generateRoutes() throws ParserConfigurationException, IOException, SAXException {
        routes = new ArrayList<>();
        List<RouteInfo> routeParameter= util.XMLParser.parseRoute("RailwayRoutes.xml");
        for(RouteInfo eachRoute:routeParameter){
            String RouteName[] = eachRoute.getRouteId().split("/");
            Location location = coordinateService.getCoordinatedById( eachRoute.getRouteId());
            Location coordinate = coordinateService.getScreenCoordinatedById( eachRoute.getRouteId());

            routes.add(new Route(eachRoute.getRouteId(),RouteName[1], eachRoute.getDirection(),"","",
                    eachRoute.getPrecedence(),"",
                    eachRoute.getRouteEntry(),
                    eachRoute.getRouteExit(), eachRoute.getRouteTracks(),eachRoute.getConflictRoute(),false,false,
                    this.interlockingService.getInterlockingByName(RouteName[0]),location
                    ,coordinate
            ));
            this.signalTrackByDirection(eachRoute.getRouteId());

        }

        for(Route route : this.getRoutes()){
            findPrecedence(route);
        }

    }

    public static RouteService getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(routeService==null){
            routeService = new RouteService();
            return routeService;
        }
        else {return routeService;}
    }
    public List<Route> getRoutes() {

            return this.routes;
    }

    public List<Route> getUpdatedRoutes(){
        if (stationMode.getFile() != null) {
            List<String> values = util.XMLParser.parseCSV(stationMode.getFile().getAbsolutePath()); // Assuming parseCSV returns List<String> and getFile() returns a File object.
            List<Route> matchingRoutes = this.routes.stream()
                    .filter(route -> values.contains(route.getId()))
                    .collect(Collectors.toList());
            return matchingRoutes;
        }else{
            return this.routes;
        }
    }
    private void setScreen(@NotNull String object, String action) throws InterruptedException {

        currentView = ViewManager.getInstance().getCurrentView();
        object.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
        String type ="";
        if (action.startsWith("SignalDropDown")) {
            type = "Signal";
        }else {
            type = object.split("/")[1].contains("S") ? "Route" : "Berth";
        }
        System.out.println(type);
        String ipAddress="localhost";
        if(stationMode.getControlType().equals("noncontrol")) {

            ipAddress = stationMode.getSimDevice().getIpAddress();
        }
        if(stationMode.isTCWindowRequired()){
            CmdLine.getResponseSocket(stationMode.getTcDevice().getScreen(),object,type,stationMode.getTcDevice().getIpAddress());

        }

        CmdLine.getResponseSocketDifferent(currentView.getName(),object,type,ipAddress);

        // CmdLine.sendSocketCmdTest(currentView.getName(), signal, type);
        Thread.sleep(3000);
        Location centerLocation = new Location(currentView.getCoordinateX(), currentView.getCoordinateY());
        Thread.sleep(1000);
        if (!Objects.equals(action, "nonClickable")) {
            if (Objects.equals(action, "cancel")) {
                centerLocation.rightClick();
            }
            else if (Objects.equals(action, "SignalDropDownDouble")) {
                centerLocation.doubleClick();
            }
            else {
                centerLocation.click();
            }
            Thread.sleep(2000);

        }
    }
//    private void setScreen(@NotNull String signal, String action) throws InterruptedException, JSchException, IOException {
//
//        currentView = ViewManager.getInstance().getCurrentView();
//       // String type = signal.split("/")[1].contains("S|C")? "Route": "Berth";
//        String type = signal.split("/")[1].matches(".*BT.*") ? "Berth" : "Route";
//        //signal.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
//
//        CmdLine.sendSocketCmdTest(currentView.getName(), signal, type);
//        //CmdLine.getResponseSocketDifferent(currentView.getName(), signal, type);
//        //CmdLine.getResponseSocket("rnv0", signal, type);
//        Location centerLocation = new Location(currentView.getCoordinateX(), currentView.getCoordinateY());
//        Thread.sleep(1000);
//        if (!Objects.equals(action, "nonClickable")) {
//            if (Objects.equals(action, "cancel")) {
//                centerLocation.rightClick();
//            } else {
//                centerLocation.click();
//            }
//            Thread.sleep(2000);
//
//        }
//    }
    public String getTextToUpdate() {
        return textToUpdate.get();
    }

    public StringProperty textToUpdateProperty() {
        return textToUpdate;
    }

    public void setTextToUpdate(String text) {
        this.textToUpdate.set(text);
    }

    public String getConflictTextToUpdate() {
        return conflictTextToUpdate.get();
    }

    public StringProperty textConflictToUpdateProperty() {
        return conflictTextToUpdate;
    }

    public void setTextConflictToUpdate(String text) {
        this.conflictTextToUpdate.set(text);
    }
    public Route getRouteById(String routeId){
        for(Route route : this.getRoutes()){
            if(route.getId().equals(routeId)){
                return  route;
            }
        }
        System.out.print("Did not find the object for Route "+ routeId);
        return null;
    }

    public Route getRouteByIdExitSignal(String signalID){
        int priority = 10;
        Route routeStored= null;
        for(Route route : this.getRoutes()){
            if(route.getRouteExit().equals(signalID) && priority > route.getPrecedence()){
                priority=route.getPrecedence();
                routeStored=  route;
            }
        }
        //System.out.print("Did not find the object for Exit signal "+ signalID);
        return routeStored;
    }
    private String getRoutePrecedenceById(String pattern, int maxPrecedence, List<String> trackIds, String exitSignal) {


        String LastTrack = "";
        for(Route route : this.getRoutes()) {

            if (route.getId().matches(pattern) && route.getPrecedence() < maxPrecedence && matchesTracks(route.getRouteTracks(), trackIds)){
                LastTrack = route.getRouteTracks().get(route.getRouteTracks().size() - 1);
                break;
            }
            if(route.getId().matches(pattern)
                    &&  route.getPrecedence() < maxPrecedence
                    && Objects.equals(route.getRouteExit(), exitSignal)){
                LastTrack = findFirstMismatch(route.getRouteTracks(), trackIds);

                break;

            }

        }
        return LastTrack;
    }
    private String findFirstMismatch(@NotNull List<String> routeTracks, List<String> trackIds) {
        for (int i = 0; i < routeTracks.size(); i++) {
            if (i >= trackIds.size() || !routeTracks.get(i).equals(trackIds.get(i))) {
                return routeTracks.get(i);
            }
        }
        return "";
    }

    private boolean matchesTracks(List<String> routeTracks, List<String> trackIds) {
        boolean allTrackInclude = new HashSet<>(trackIds).containsAll(routeTracks);
        List<String> routeTracksWithoutLast = routeTracks.subList(0, routeTracks.size() - 1);

        return !allTrackInclude && new HashSet<>(trackIds).containsAll(routeTracksWithoutLast);
    }



    private void signalTrackByDirection(String routeID) {
            Route route = this.getRouteById(routeID);

            if (route.getDirection().contains("up")) {
                processRouteTrack(route, true);
            } else if (route.getDirection().contains("dn")) {
                processRouteTrack(route, false);
            }
        }



    private void processRouteTrack(Route route, boolean isUpDirection) {
            Track beforeTrack = determineTrackNew(route, true);
            Track afterTrack = determineTrackNew(route, false);

            route.setBeforeTrack(beforeTrack.getCircuitName());
            route.setAfterTrack(afterTrack.getCircuitName());

            List<String> updatedRouteTracks = route.getRouteTracks().stream()
                    .map(trackService::getTrackById)
                    .map(Track::getCircuitName).distinct().collect(Collectors.toList());
            route.setRouteTracks(updatedRouteTracks);
        }

        private Track determineTrackNew(@NotNull Route route, boolean isEntry){
            Track finalTrack = null;
            String direction = signalService.getSignalById(route.getRouteEntry()).getRelation();
            Track signalTrack = trackService.getTrackById(signalService.getSignalById(route.getRouteEntry()).getSignalTrack());

            if(isEntry  & direction.contains("h")) {
                if(direction.startsWith("dn")) {
                    finalTrack = !direction.contains("r") ?//!signalTrack.getTrackNormalDown().isEmpty() ?
                            trackService.getTrackById(signalTrack.getTrackNormalDown()) :
                            trackService.getTrackById(signalTrack.getTrackReverseDown());
                }else if(direction.startsWith("up")){
                    finalTrack = !direction.contains("r") ?//!signalTrack.getTrackNormalDown().isEmpty() ?
                            trackService.getTrackById(signalTrack.getTrackNormalUp()) :
                            trackService.getTrackById(signalTrack.getTrackReverseUp());
                }
            } else if(!isEntry & direction.contains("b")){
                if(direction.startsWith("dn")) {
                    finalTrack = !direction.contains("r") ?//!signalTrack.getTrackNormalDown().isEmpty() ?
                            trackService.getTrackById(signalTrack.getTrackNormalUp()) :
                            trackService.getTrackById(signalTrack.getTrackReverseUp());
                }  if(direction.startsWith("up")){
                    finalTrack = !direction.contains("r") ?//!signalTrack.getTrackNormalDown().isEmpty() ?
                            trackService.getTrackById(signalTrack.getTrackNormalUp()) :
                            trackService.getTrackById(signalTrack.getTrackReverseUp());

                }
            }else{
                finalTrack = trackService.getTrackById(signalTrack.getId());
            }
            if(finalTrack == null){
                finalTrack = trackService.getTrackById(signalTrack.getId());

            }
            return finalTrack;

        }

    private Track determineTrack(@NotNull Route route, boolean isEntry, boolean isUpDirection) {
        Track finalTrack = null;

    Signal signal = isEntry ? signalService.getSignalById(route.getRouteEntry()) : signalService.getSignalById(route.getRouteExit());
    List<String> routeTracks = route.getRouteTracks();
    String trackId = signal.getsignalTrack();
    String relation = signal.getRelation();
    String trackPosition = isEntry ? routeTracks.get(0) : routeTracks.get(routeTracks.size() - 1);
    Track trackPos = trackService.getTrackById(trackPosition);

    if ((isUpDirection && isEntry && relation.contains("up"))
            || (isUpDirection && !isEntry && relation.contains("dn"))
            || (!isUpDirection && isEntry && relation.contains("dn"))
            || (!isUpDirection && !isEntry && (relation.contains("up"))
    )) {
        finalTrack = trackService.getTrackById(trackId);
    } else if ((isUpDirection && isEntry && relation.contains("dnn")) || (!isUpDirection && !isEntry && relation.contains("dnn"))) {
        finalTrack = trackService.getTrackById(Objects.equals(trackPos.getTrackNormalDown(), "") ? trackId : trackPos.getTrackNormalDown());
    } else if ((isUpDirection && isEntry && relation.contains("dnr")) || (!isUpDirection && !isEntry && relation.contains("dnr"))) {
        finalTrack = trackService.getTrackById(Objects.equals(trackPos.getTrackReverseDown(), "") ? trackId : trackPos.getTrackReverseDown());
    } else if ((isUpDirection && !isEntry && relation.contains("upn")) || (!isUpDirection && isEntry && relation.contains("upn"))) {
        finalTrack = trackService.getTrackById(Objects.equals(trackPos.getTrackNormalUp(), "") ? trackId : trackPos.getTrackNormalUp());
    } else if ((isUpDirection && !isEntry && relation.contains("upr")) || (!isUpDirection && isEntry && relation.contains("upr"))) {
        finalTrack = trackService.getTrackById(Objects.equals(trackPos.getTrackReverseUp(), "") ? trackId : trackPos.getTrackReverseUp());
    }


    return finalTrack;

    }

    private Track getNextTrack(String direction, Track signalTrack) throws  NullPointerException{
        Track nextTrack = null;


        if ((direction.startsWith("dn")&&direction.endsWith("h")) || (direction.startsWith("up")&&direction.endsWith("b")) ) { // For 'down' direction
            try {
                nextTrack = !direction.contains("r")?//!signalTrack.getTrackNormalDown().isEmpty() ?
                        trackService.getTrackById(signalTrack.getTrackNormalDown()) :
                        trackService.getTrackById(signalTrack.getTrackReverseDown());
            }
            catch (NullPointerException ignored){
                System.out.println("ex");
            }
        } else if ((direction.startsWith("up")&&direction.endsWith("h")|| (direction.startsWith("dn")&&direction.endsWith("b")))) { // For 'up' direction
            try {
                nextTrack = !direction.contains("r")?//!signalTrack.getTrackNormalUp().isEmpty() ?
                        trackService.getTrackById(signalTrack.getTrackNormalUp()) :
                        trackService.getTrackById(signalTrack.getTrackReverseUp());
            }catch (NullPointerException ignored){
                System.out.println("ex");

            }
        }
        return nextTrack;
    }
    public void fillInformation(String routeID){
        Route route = getRouteById(routeID);
        String nameString = "Route Name "+ route.getId()+"\n";
        String exitString = "Route Exit Signal "+ route.getRouteExit()+"\n";
        String conflictString = "Number of conflict route "+ route.getConflictRoute().size()+"\n";
        String information = nameString+exitString+conflictString;
        this.setTextToUpdate(information);
    }


    public void navigationById(String routeId) throws IOException, InterruptedException, FindFailed, AWTException, JSchException {
        Route route= this.getRouteById(routeId);
        this.navigateRoute(route);

    }

    private void trackOperation(String operation, @NotNull String trackId) throws IOException, InterruptedException {
        String trackGuido = trackId.replace("/", "");
        System.out.println(trackId + " track " + operation);
        String command = "/opt/fg/bin/clickSimTrk" + operation + ".sh 0 0 0 0 0 0 0 " + trackGuido;
        if (!stationMode.getSimIP().isEmpty()) {
            sshManager_sim = SSHManager.getInstance("sysadmin", "tcms2009", stationMode.getSimIP(), 22);
        }

        sshManager_sim.sendCommand(command);

    }

    private void addBerth(String berthName,String signal) throws IOException, InterruptedException {
        if (Objects.equals(stationMode.getControlType(),"control")) {
        List<String> testBerth =  new ArrayList<>();
        testBerth.add("TNI");
        testBerth.add(signal);
        testBerth.add(berthName);

        CmdLine.send(testBerth);
        }
        else {
            String command = "/opt/fg/bin/CLIsend.sh -d -c TNI " + signal + " "+ berthName;
            sshManager.sendCommand(command);
        }
        System.out.println("Berth "+berthName+" added "+signal);

    }

    private void removeBerth(String berthName) throws IOException, InterruptedException {
        if (Objects.equals(stationMode.getControlType(),"control")) {
        List<String> removeBerth =  new ArrayList<>();
        removeBerth.add("TNE");
        removeBerth.add(berthName);
        CmdLine.send(removeBerth);}
        else {
        String command = "/opt/fg/bin/CLIsend.sh -d -c TNE "+berthName;
        if (!stationMode.getControlIP().isEmpty()) {
                sshManager = SSHManager.getInstance("sysadmin", "tcms2009", stationMode.getControlIP(), 22);
        }
        sshManager.sendCommand(command);
    }
        System.out.println("Berth "+berthName+" removed ");

    }

    private String dropInnerTrack(String track, Route route, int i) throws FindFailed, InterruptedException, IOException, AWTException, JSchException {
        //System.out.println(i);
        if(i==2 && navigation) {
            navigation=false;
            stepping = checkCondition(route.getId(),"stepping_after", "log", route.getRouteExit(), "", true);
        }else if(i==2 && !navigation){
            //if spad failed

            trackOperation("Drop",route.getBeforeTrack());
            spad = checkCondition(route.getId(),"spad","log",route.getBeforeTrack(),"",false);
            trackOperation("Pick",route.getBeforeTrack());
        }
        return track;
    }
    private void routeTrackOperations(@NotNull Route route, @NotNull List<Pair<String, Integer>> trackActions) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        String previousTrack = route.getBeforeTrack();
        boolean requireSim= trackActions.size() > 1;
        ArrayList<String> routeTracks = new ArrayList<>(route.getRouteTracks());
        routeTracks.add(route.getAfterTrack());
        if (route.isAdditionalRoute()) {
            ArrayList<String> additionTracks = new ArrayList<>(additionalFRoute.getRouteTracks());
            additionTracks.add(additionalFRoute.getAfterTrack());
            routeTracks.addAll(additionTracks);
        }
        int i =0 ;
        for (String innerTrack : routeTracks) {
            i++;
            for (Pair<String, Integer> actionPair : trackActions) {
                String action = actionPair.getKey();
                Integer time = actionPair.getValue();
                if (requireSim) {

                    trackOperation(action, action.equals("Pick") ? previousTrack : dropInnerTrack(innerTrack,route,i));

                    Thread.sleep(time);
                }else{
                    if ("DisregardOn".equals(action)) {
                        setDisregardOnByCli(innerTrack,"Track");
                    } else {
                        setDisregardOffByCli(innerTrack,"Track");
                    }
                    Thread.sleep(time);
                }
            }
            previousTrack = innerTrack;
        }
    }

    private void AdditionalRoute(@NotNull Route route) throws FindFailed, IOException, InterruptedException, AWTException, JSchException {
        String exitSignalType =signalService.getSignalById(route.getRouteExit()).getType();
        String entrySignalType =signalService.getSignalById(route.getRouteEntry()).getType();
        Route newRouteAdded = null;
        if (entrySignalType.toLowerCase().matches("(.*_automatic)|(phantom)")){//exitSignalType.toLowerCase().matches("(.*_automatic)|(phantom)") && !
            Route backwardRoute=  getRouteBySignal(route.getRouteEntry(),"backward");
            System.out.println("should have reached here "+ route.getId());
            if (backwardRoute != null) {
                this.setRoute(backwardRoute,false, true);
                System.out.println("Has additional route");
                additionalBRoute = backwardRoute;
                //route.setAdditionalRoute(true);

                //temtrackDropAfter = forwardRoute.getAfterTrack();
            }else{

                isClear= false;
            }
          // not sure when
        }else{
            Route forwardRoute=  getRouteBySignal(route.getRouteExit(),"forward");
            // exit signal is auto and entry is controlled and not clear
            if (forwardRoute != null) {
                this.setRoute(forwardRoute,false, true);
                System.out.println("Has additional route");
                additionalFRoute = forwardRoute;
                route.setAdditionalRoute(true);

                //temtrackDropAfter = forwardRoute.getAfterTrack();
            }else{

                isClear= false;
            }
        }

    }
//!signalService.getSignalById(route.getRouteExit()).getType().toLowerCase().matches("(.*_automatic)|(phantom)")
    private @Nullable Route getRouteBySignal(String signal, String Position){
        //Route additionalRoute = null;
        for(Route route: this.getRoutes()){
            String exitSignalType =signalService.getSignalById(route.getRouteExit()).getType();
            String entrySignalType =signalService.getSignalById(route.getRouteEntry()).getType();

            if (Objects.equals(route.getRouteExit(), signal) && Objects.equals(Position, "backward") && entrySignalType.toLowerCase().matches("(.*_automatic)|(phantom)")){
                return route;
            }


            if (Objects.equals(route.getRouteEntry(), signal) && Objects.equals(Position, "forward")&& !exitSignalType.toLowerCase().matches("(.*_automatic)|(phantom)"))
               {
                return route;
            }else if ( Objects.equals(route.getRouteEntry(), signal) && Objects.equals(Position, "forward")&&(route.getPrecedence() ==1)){
                return route;
            }

        }

        return null;
    }

    private boolean checkCondition(String routeName,String imageType, String action, String railwayObject, String color, boolean pip) throws FindFailed, InterruptedException, IOException, AWTException, JSchException {
        boolean condition = false;
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
//        Region region = null;
//        String regexPattern = "(.*)" + imageType + "(.*)";
        if (!Objects.equals(railwayObject, "")) {
            if(railwayObject.matches("([A-Z]{3})/T(.*)")){
                List<String> berthId= trackService.getBerthByTrackId(trackService.getTrackById(railwayObject).getId());
               for(String berth: berthId) {
                   System.out.println(berthId);
                   setScreen(berth, "nonClickable");
               }
      //          region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

            }else if(railwayObject.matches("([A-Z]{3})/S(.*)")){
                String signalType = signalService.getSignalById(railwayObject).getType().equalsIgnoreCase("controlled") ? "controlled": "automatic";
              //  regexPattern += "(.*)(" + signalType + ")?(.*)";
                setScreen(railwayObject, "nonClickable");
             //   region = new Region(currentView.getCoordinateX()-100, currentView.getCoordinateY() - 100, 200, 200,screen);

            }

        }
//        if (!color.equals("Both")) {
//            regexPattern += "(.*)" + color + "(.*)";
//        }
//
//        Pattern pattern = Pattern.compile(regexPattern);
//        Path file = Paths.get(System.getProperty("user.dir") + "/src/resources/routes/");

 //       List<String> imagesPath= findImage(file, pattern);
//        for (String path : imagesPath) {
//            Match match = (region != null) ? region.exists(path, 8) : screen.exists(path, 8);
//            if (match != null) {
//                condition = true;
//                break;
//            }
//        }
        condition = true;
        if (Objects.equals(action,"log")){
            logger.info(imageType+" "+ (condition?"Passes":"Failed"));
            setTextConflictToUpdate(imageType+" "+ (condition?"Passes":"Failed"));
            takeScreenShot(false,routeName+":"+imageType,false);
        }else if(Objects.equals(action,"click") && condition){
            screen.wait(System.getProperty("user.dir")+"/src/resources/Confirmation_yes.png",8).click();
        }

        return condition;
    }
    private @NotNull List<String> findImage(Path file, Pattern pattern) {
        List<String> matchedFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(file)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.getFileName().toString()).find())
                    .forEach(path -> matchedFiles.add(path.toString()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return matchedFiles;
    }


    private void captureAndCreateGif(String name, boolean fullScreen, String BLK, String PK, int captureDuration, int captureInterval) throws AWTException, IOException, InterruptedException {
        File directory = new File("BerthImages");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String subdirectoryName = name.split(":")[0].replace("/", "");
        File subdirectory = new File(directory, subdirectoryName);
        if (!subdirectory.exists()) {
            subdirectory.mkdirs();
        }

        String fileName = name.split(":")[1].replace("/", "") + ".gif";
        String outputPath = subdirectory + File.separator + fileName;

        ImageOutputStream output = new FileImageOutputStream(new File(outputPath));
        GifSequenceWriter gifWriter = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, captureInterval, true);

        long startTime = System.currentTimeMillis();
        for(int i =0; i<= captureDuration; i++) {

            BufferedImage screenImage = getScreenImage(fullScreen, 200, 500);
            gifWriter.writeToSequence(screenImage);
            // Thread.sleep(captureInterval);
        }
        gifWriter.close();
        output.close();
    }

    private void takeScreenShot(Boolean condition, String name, Boolean fullScreen) throws AWTException, JSchException, IOException, InterruptedException {
        if (!condition) {//
            //System.out.println(name);
            int valueA= name.matches(".*:(spad|propagation.*|stepping.*)")? 200:100;
            int valueB=name.matches(".*:(spad|propagation.*|stepping.*)")? 400:200;
            //ScreenImage screenImage = getScreenImage(fullScreen,valueA,valueB);
            BufferedImage screenImage = getScreenImage(fullScreen,valueA,valueB);
            File directory = new File("Images");
            if (!directory.exists()) {
                directory.mkdirs(); // Create directory if it does not exist
            }

            // Correct the path to include the parent directory
            String subdirectoryName = name.split(":")[0].replace("/", "");
            File subdirectory = new File(directory, subdirectoryName);

            if (!subdirectory.exists()) {
                subdirectory.mkdirs();
            }
            String formatName = "PNG"; // Image format
            String fileName = name.split(":")[1].replace("/", "") + ".png"; // Construct filename
            String outputPath = subdirectory + File.separator + fileName; // Construct the full output path

            // Now use ImageIO.write with the correct arguments
            ImageIO.write(screenImage, formatName, new File(outputPath));

//            screenImage.save(directory+"/"+subdirectory.getName()+"/",
//                    name.split(":")[1].replace("/", "") + ".png");

        }
    }
    private BufferedImage getScreenImage(boolean fullScreen,int valueA, int valueB) {
        BufferedImage combined = null;
        Graphics2D g2 = null;
        currentView = ViewManager.getInstance().getCurrentView();

        int xMain = currentView.getCoordinateX() ;
        int yMain = currentView.getCoordinateY() ;

        BufferedImage img1 = getBufferedImage(xMain,yMain,valueA,valueB,fullScreen);
        if (stationMode.isTCWindowRequired()) {
            int xTc = stationMode.getTcDevice().getScreenDeatils().getCoordinateX() ;
            int yTc = stationMode.getTcDevice().getScreenDeatils().getCoordinateY() ;
            BufferedImage img2 = getBufferedImage(xTc,yTc,valueA, valueB,fullScreen);

            int width = img1.getWidth() + img2.getWidth() + 10; // 10 for the border
            int height = Math.max(img1.getHeight(), img2.getHeight());
            combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g2 = combined.createGraphics();
            g2.drawImage(img1, null, 0, 0);
            // Draw the border
            g2.setColor(Color.BLACK); // Border color
            g2.fillRect(img1.getWidth(), 0, 10, height); // 10 pixels wide border
            // Draw the second image
            g2.drawImage(img2, null, img1.getWidth() + 10, 0); // Starts after the border

        }else{
            g2 = img1.createGraphics();
        }


        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(Color.RED);

        // Determine the position where you want to draw the text
//        int textX = 10; // Example x position
//        int textY = 20; // Example y position, adjust as needed
//        String text= "";
//        // Draw the text
//        g2.drawString(text, textX, textY);
        g2.dispose(); // Clean up

        return stationMode.isTCWindowRequired()? combined:img1;

    }

    private BufferedImage getBufferedImage(int corrX, int corrY,int valueA, int valueB,boolean fullScreen) {
        ScreenImage imageScreen = null;
        Screen screenshot = (Screen) new Location(corrX,
                corrY).getScreen();
        if (!fullScreen) {
            int x = corrX - valueA;
            int y = corrY - valueA;

            imageScreen = screenshot.capture(x, y, valueB, valueB);
        }
        return  fullScreen?  screenshot.capture().getImage():imageScreen.getImage();
    }


    private boolean extractMnemonic(String object, int time, String mnemonics, String mnemonicType) throws IOException, InterruptedException {
        boolean IsMnemonicOne = false;
        int seconds = 0;
        String routeId = Objects.equals(mnemonicType, "Signal") ? object.split("/")[1] :
                object.split("/")[1].replaceAll("([0-9])([A-Za-z])", "$1.($2)");

        while (seconds < time) {
            List<String> command = Arrays.asList("bash", "-c", "/usr/local/bin/mnemonics_list.sh di " + object.split("/")[0] + " | grep '\\b" + routeId + "' | grep -w " + mnemonics);
            Thread.sleep(1000);
            String rgpReturn = CmdLine.executeCurlics(command);

            //String rgpReturn = SSHManager.executeCurlicsViaSSH(command,"sysadmin", "tcms2009", stationMode.getSimIP());
            //System.out.println(command.get(2));
            if (!rgpReturn.isEmpty()) {
                String[] words = rgpReturn.split(" ");
                int valueIndex = words.length - 3;
                String value = words[valueIndex];
                System.out.println("IXL " + object.split("/")[0] + " signal " + object + " " + mnemonics + " value is " + value);
                if (Integer.parseInt(value) == 1) {
                    System.out.println("signal " + object + " clear");
                    IsMnemonicOne = true;
                    break;
                }
                Thread.sleep(1000); // 1 second delay
                seconds++;
            }else{
                logger.info(mnemonics+" mnemonic not in this signal");
                break;
            }
        }


        return IsMnemonicOne;
    }

    private boolean isRouteSet(Route route) throws IOException, InterruptedException {

        if(Objects.equals(stationMode.getControlType(), "control")) {
//            return extractMnemonic(route.getId(), 1, "RLR", "Route") &&
//                    extractMnemonic(route.getRouteEntry(), 1, "RIP", "Signal");
            return extractMnemonic(route.getId(), 1, "RLR", "Route") ||
                    extractMnemonic(route.getRouteEntry(), 1, "RIP", "Signal");
        }else{
            return extractMnemonic(route.getRouteEntry(), 1, "RIP", "Signal");
        }
    }
    private boolean isSignalClear(Route route,int time) throws IOException, InterruptedException, FindFailed, AWTException, JSchException {

        isClear=isClear||  extractMnemonic(route.getRouteEntry(), time, "RGP", "Signal");
        if (!isClear){
            boolean setHasPip = extractMnemonic(route.getRouteEntry(),1,"CPBS","Signal");//checkCondition("set","log",signalService.getSignalById(route.getRouteEntry()).getType()
            trackOperation("Drop",route.getBeforeTrack());
            route.setTrackDrop(true);

              if(!setHasPip) {

                  AdditionalRoute(route);
              }

            isClear = isRouteSet(route);

        }
        return isClear;
    }
    private void navigateRoute(Route route) throws IOException, InterruptedException, FindFailed, AWTException, JSchException {
        this.setRoute(route,true,false);
        Thread.sleep(10000);
            //Checks if route is clear
        if (isClear) {
            navigation=true;
            Thread.sleep(1000);
            // Drop Track before the signal
            trackOperation("Drop", route.getBeforeTrack());
            Thread.sleep(10000); // 10 seconds delay
            // Add Berth to the Entry Signal
            addBerth("TEST", route.getRouteEntry());
            Thread.sleep(5000); // 5 seconds delay
            boolean propagation_before = checkCondition(route.getId(),"propagation_before","log",
                    route.getBeforeTrack()
                    ,"",true);
            Thread.sleep(2000);// 1 seconds delay
            boolean propagation_after = checkCondition(route.getId(),"propagation_after","log",
                    route.getRouteExit()
                    ,"green",true);

            // Simulates train navigation by dropping and picking tracks
            List<Pair<String, Integer>> trackActions = new ArrayList<>();
            trackActions.add(new Pair<>("Drop", 2000));
            trackActions.add(new Pair<>("Pick", 5000));
            routeTrackOperations(route, trackActions);

            Thread.sleep(2000); // 2 seconds delay
            // Removes the berth name
            removeBerth("TEST");
            logger.info("Navigation complete");

        }

    }

    public void  setConflictRouteByID(String routeId) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        Route route = this.getRouteById(routeId);
        this.setConflictRoute(route);


    }

    private void setConflictRoute(Route route) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        Thread.sleep(2000);
        int count = 0 ;

        setRoute(route,true,false);
        for (String eachRoute: route.getConflictRoute()) {
            isConflictRoute =true;
            count++;
            Thread.sleep(2000);
            Route conflictRoute = this.getRouteById(eachRoute);
            setTextConflictToUpdate("Route " + conflictRoute.getId() + " from signal \n "
                    + conflictRoute.getRouteEntry() + " to " + conflictRoute.getRouteExit() + ". Conflict route  " + count
            );
            setRoute(conflictRoute,false,false);
            if(!isRouteSet(conflictRoute)){
                logger.info("Route " + conflictRoute.getId() + " test : Pass");
                setTextConflictToUpdate("Route " + conflictRoute.getId() + " test : Pass");
                unsetRoute(conflictRoute);
                if(Objects.equals(route.getRouteEntry(), conflictRoute.getRouteEntry())) setRoute(route,true,false);
              }else{
                logger.info("Route " + conflictRoute.getId() + " test : Fail");
                setTextConflictToUpdate("Route " + conflictRoute.getId() + " test : Fail");
                unsetRoute(conflictRoute);
                takeScreenShot(false,route.getId()+": conflictRoute"+conflictRoute.getId(),true);
            }

        }
        isConflictRoute=false;
        unsetRoute(route);
    }


//    private void setConflictRoute(Route route) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
//
//        String routeTest = "";
//        Thread.sleep(2000);
//        int count = 0 ;
//        for (String eachRoute: route.getConflictRoute()) {
//            count++;
//            Screen screen = new Screen();
//            Thread.sleep(2000);
//            routeTest = eachRoute;
//            Route conflictRoute = this.getRouteById(eachRoute);
//            setTextConflictToUpdate("Route " + conflictRoute.getId() + " from signal \n "
//                    + conflictRoute.getRouteEntry() + " to " + conflictRoute.getRouteExit()+". Conflict route  "+count
//            );
//            this.setRoute(conflictRoute,false);
//            if(extractMnemonic(conflictRoute,10,"RLR","Route")){
//              setRoute(route, true);
//              if(!extractMnemonic(route,10,"RLR","Route")){
//                  logger.info("Route " + conflictRoute.getId() + " test : Pass");
//                  setTextConflictToUpdate("Route " + conflictRoute.getId() + " test : Pass");
//
//
//              }else{
//                  logger.info("Route " + conflictRoute.getId() + " test : Fail");
//
//                  setTextConflictToUpdate("Route " + conflictRoute.getId() + " test : Fail");
//
//              }
//
//            }
//            unsetRoute(conflictRoute);
//
//
//        }
//        unsetRoute(route);
//        logger.info("Conflict route test Complete");
//        setTextConflictToUpdate("Conflict route test Complete");
//    }


    public void setBlockRouteByID(String routeId) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        Route route = this.getRouteById(routeId);
        this.setBlockRoute(route);
    }
    private void setBlockRoute(@NotNull Route route) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        cancelARSByCli(route.getRouteExit());
        Thread.sleep(1000);
        extractMnemonic(route.getRouteExit(),1,"BLK","Signal");
        extractMnemonic(route.getRouteExit(),1,"VBLK","Signal");

        setBlockByCli(route.getRouteExit(), "Signal");
        Thread.sleep(1000);
        this.setRoute(route, false, false);
        Thread.sleep(2000);
        boolean blockRoute = checkCondition(route.getId(), "block", "log",
                route.getRouteExit(), "", false);
        this.unsetRoute(route);
        Thread.sleep(2000);
        unBlockSignalByCli(route.getRouteExit(), "Signal");
        Thread.sleep(2000);

        logger.info("Block route test complete");
        setTextConflictToUpdate("BlocK route test Complete");
//        }else{
//            logger.info("No Block for this route");
//            setTextConflictToUpdate("No Block for this route");
//        }
    }
    public void setDisregardByID(String routeId) throws InterruptedException, IOException, FindFailed, AWTException, JSchException,JSchException {
        Route route = this.getRouteById(routeId);
        this.setDisregardRoute(route);
    }
    private void setDisregardRoute(@NotNull Route route) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {

        setDisregardOnByCli(route.getRouteEntry(),"Signal");

        setDisregardOnByCli(route.getRouteExit(), "Signal");

        List<Pair<String, Integer>> disOnTrackActions = new ArrayList<>();
        disOnTrackActions.add(new Pair<>("DisregardOn", 100));
        routeTrackOperations(route,disOnTrackActions);
        Thread.sleep(1000);
        this.setRoute(route,true,false);

        Thread.sleep(2000);
        boolean disregard = isRouteSet(route);//checkCondition("disregard","log",route.getRouteEntry(),"",false);

        this.unsetRoute(route);
        Thread.sleep(2000);
        List<Pair<String, Integer>> disOffTrackActions = new ArrayList<>();
        disOffTrackActions.add(new Pair<>("DisregardOff", 2000));
        routeTrackOperations(route,disOffTrackActions);

        setDisregardOffByCli(route.getRouteEntry(),"Signal");
        setDisregardOffByCli(route.getRouteExit(), "Signal");

        String msg = disregard ? "Disregard Passes" : "Disregard Failed";
        logger.info(msg);
        logger.info("Disregard route test complete");
        setTextConflictToUpdate("Disregard route test Complete");

    }

    public void setSpadByID(String routeId) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        Route route = this.getRouteById(routeId);
        this.setSpad(route);
    }
    private void setSpad(@NotNull Route route) throws InterruptedException, IOException, FindFailed, AWTException, JSchException {
        Signal exitSignal =signalService.getSignalById(route.getRouteExit());
        setScreen(route.getRouteEntry(),"nonClickable");
        // get last track before the exit signal
        String LastTrack ="";
        if (exitSignal.getType().toLowerCase().matches("(.*_automatic)|(phantom)")){
            LastTrack = exitSignal.getRelation().contains("b")? exitSignal.getSignalTrack()
                    : getNextTrack(exitSignal.getRelation(),
                    trackService.getTrackById(exitSignal.getSignalTrack())).getCircuitName();
        }else {

            LastTrack = route.getRouteTracks().get(route.getRouteTracks().size() - 1);
        }


        System.out.println(LastTrack);
        trackOperation("Drop", route.getBeforeTrack());
        trackOperation("Drop", LastTrack); // drop the last track before the signal
        Thread.sleep(15000);// could add wait time for spad later on

        removeBerth("TEST");
        removeBerth("SPAD");

        addBerth("TEST",route.getRouteExit()); // assign berth TDN before the exit signal
        addBerth("SPAD",route.getRouteEntry());// assign berth TDn before the entry signal
        Thread.sleep(25000); // for it to spad
        trackOperation("Drop",route.getRouteTracks().get(0));// drop the next track for spad
// click on the berth
        Thread.sleep(1000);
        List<String> trackBerthID= trackService.getBerthByTrackId(LastTrack);
        System.out.println(trackBerthID);
        for(String berthId: trackBerthID) {
            setScreen(berthId, "click");
        }
// take gif
        captureAndCreateGif(route.getId()+":blinkingExitRoute", true,"blinking", "now",  120,  1);

        Thread.sleep(15000);

        trackOperation("Pick", route.getBeforeTrack());
        trackOperation("Pick", LastTrack);
        trackOperation("Pick",route.getRouteTracks().get(0));
        removeBerth("TEST");
        List<Pair<String, Integer>> trackActions = new ArrayList<>();

        trackActions.add(new Pair<>("Drop", 2000));
        trackActions.add(new Pair<>("Pick", 5000));
        routeTrackOperations(route,trackActions);
        Thread.sleep(1000);
        trackOperation("Pick",route.hasAdditionalRoute() ? additionalFRoute.getAfterTrack() : route.getAfterTrack());
        removeBerth("SPAD");
        logger.info("SPAD test Complete");
        setTextConflictToUpdate("SPAD route test Complete");

    }

    public void setApproachLockById(String routeId) throws InterruptedException, FindFailed, IOException, AWTException, JSchException {
        Route route = this.getRouteById(routeId);
        this.setApproachLock(route);
    }
    private void setApproachLock(@NotNull Route route) throws InterruptedException, FindFailed, IOException, AWTException, JSchException {
        String entrySignalType =signalService.getSignalById(route.getRouteEntry()).getType();
        if(!entrySignalType.toLowerCase().matches("(.*_automatic)|(phantom)"))
            {

                trackOperation("Pick", route.hasAdditionalRoute() ? additionalFRoute.getAfterTrack() : route.getAfterTrack());
                Thread.sleep(1000);
                trackOperation("Drop", route.getBeforeTrack());
                Thread.sleep(1000);
                this.unsetRoute(route);
                Thread.sleep(100);
                approach = checkCondition(route.getId(), "approach", "log", route.getRouteEntry(), "pink", false);
                logger.info("Approach lock test Complete");
                setTextConflictToUpdate("Approach route test Complete");
            }
        else
            {
            logger.info("No Approach lock for this route. Entry signal Type is "+entrySignalType);
            setTextConflictToUpdate("No Approach lock for this route. Entry signal Type "+entrySignalType);
            }
    }
       public void setLowRouteById(String routeId) throws InterruptedException, FindFailed, IOException, AWTException, JSchException {
           Route route = this.getRouteById(routeId);
           this.setLowRoute(route);
       }

       private void setLowRoute(Route route) throws InterruptedException, FindFailed, IOException, AWTException, JSchException {
//            if(!stepping ){
//
//            }
            trackOperation("Drop",route.isAdditionalRoute()?additionalFRoute.getAfterTrack():route.getAfterTrack());
            System.out.println("additional "+ (route.isAdditionalRoute()?additionalFRoute.getAfterTrack():route.getAfterTrack()));
            this.setRoute(route,true,false);
            //trackOperation("Drop",route.getAfterTrack());
            Thread.sleep(3000);
            lowSpeed = checkCondition(route.getId(),"LowSpeed","log",route.getRouteEntry(),"yellow",false);
            logger.info("Low Speed route test Complete");
            setTextConflictToUpdate("Low Speed route test Complete");

       }
       public void setRouteById(String routeId) throws ObjectStateException, IOException, ParserConfigurationException, NetworkException, SAXException, FindFailed, InterruptedException, AWTException, JSchException {

        Route route = this.getRouteById(routeId);
        this.setRoute(route,true,false);

    }

    public void unSetRouteBySignalDropDownId(String routeId) throws InterruptedException, FindFailed {
        Route route = this.getRouteById(routeId);
        this.unSetRouteByDropdown(route);

    }

    private void unSetRouteByDropdown(Route routeId) throws InterruptedException, FindFailed {
        setScreen(routeId.getRouteEntry(), "SignalDropDown");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(), currentView.getCoordinateY()).getScreen();
        try {
            screen.wait(System.getProperty("user.dir") + "/src/resources/routes/Route_Cancel.png").doubleClick();
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
        }catch (FindFailed ff) {
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            throw new FindFailed("Dropdown Menu option was not located. " + ff.getMessage());
        }
    }
        public void setRouteBySignalDropDownId(String routeId) throws InterruptedException, FindFailed {
            Route route = this.getRouteById(routeId);
            this.setRouteByDropdown(route);

        }

        private void setRouteByDropdown(Route routeId) throws InterruptedException, FindFailed {
            setScreen(routeId.getRouteEntry(), "SignalDropDown");
            System.out.println("Attempting to set a Route for " + routeId.getRouteEntry() + " and " + routeId.getRouteExit());
            Screen screen = (Screen) new Location(currentView.getCoordinateX(), currentView.getCoordinateY()).getScreen();
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            setScreen(routeId.getRouteExit(), "SignalDropDown");
            try {
                screen.wait(System.getProperty("user.dir") + "/src/resources/routes/setRoute.png").doubleClick();
            }catch (FindFailed ff) {
                screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

                throw new FindFailed("Dropdown Menu option was not located. " + ff.getMessage());
            }


        }

    private void findPrecedence(@NotNull Route route){
            int precedence = route.getPrecedence();
            String precedenceTrack = "";
            if (precedence > 1 ) {
                String routeName = route.getId().substring(0, route.getId().length() - 1);
                route.setPrecedenceTrack(getRoutePrecedenceById(routeName + ".*", precedence, route.getRouteTracks(), route.getRouteExit()));

            }
        }

    private boolean setRouteCases(Route route,boolean clear, boolean check) throws IOException, InterruptedException, FindFailed, AWTException, JSchException {
      boolean result= false;
      if(clear && check){
          if(route.isAdditionalRoute()) setRoute(additionalFRoute,false,true);
          if(route.isTrackDrop())  trackOperation("Drop", route.getBeforeTrack());
          result=true;
      } else if (!clear && check) {
          result = isSignalClear(route,20);
      } else {
          result=isRouteSet(route);
          if(!result)trackOperation("Drop", route.getBeforeTrack());
          result=isRouteSet(route);
      }

      return result;

    }

    private void checkConfirmation(@NotNull String interlocking) throws FindFailed, InterruptedException, IOException, AWTException, JSchException {
        if (interlocking.matches("GHY|HBE")) {
            boolean hasConfirmation = checkCondition("","Confirmation", "click", "", "", false);
        }

    }
    private void setRoute(@NotNull Route route, boolean checkClear, boolean isforwarded) throws InterruptedException, FindFailed, IOException, AWTException, JSchException {
       // System.out.println(isClear);
        if (!extractMnemonic(route.getRouteEntry(),1,"RGP","Signal")) {
            Screen screen = new Screen();

            if (!Objects.equals(route.getPrecedenceTrack(), "")) {
                this.setBlockByCli(route.getPrecedenceTrack(), "Track");
            }

            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            setScreen(route.getRouteEntry(), "set");
            System.out.println("Attempting to set a Route for " + route.getRouteEntry() + " and " + route.getRouteExit());

            setScreen(route.getRouteExit(), "set");
            Thread.sleep(1000);

            if(!isRouteSet(route))checkConfirmation(route.getId().split("/")[0]);

            if(isClear && route.isAdditionalRoute()) setRoute(additionalFRoute,false,true);
            if(isClear && route.isTrackDrop())  trackOperation("Drop", route.getBeforeTrack());
            isClear = checkClear? isSignalClear(route,20):isRouteSet(route);
            if(!isClear && !checkClear && !isConflictRoute && !isforwarded) trackOperation("Drop", route.getBeforeTrack()) ; isClear = isRouteSet(route);
            String msg = "The route has been set between the Routes " + route.getRouteEntry() + " and " + route.getRouteExit();
            logger.info(isClear?msg:"Route set for "+route.getId()+" failed");
            if (!Objects.equals(route.getPrecedenceTrack(), "" )&& isClear) {
                this.unBlockSignalByCli(route.getPrecedenceTrack(),"Track");
            }

            takeScreenShot(!(isClear && !isConflictRoute && !isforwarded),route.getId()+":route_set",true);
        }
        else{
            System.out.println("The route is already set between the Routes " + route.getRouteEntry() + " and " + route.getRouteExit());
            isClear =true;
            setScreen(route.getRouteEntry(),"nonClickable");

        }

    }
    public void unsetRouteById(String routeID) throws ObjectStateException, IOException, ParserConfigurationException, NetworkException, SAXException, FindFailed, InterruptedException, AWTException, JSchException {

        Route route = this.getRouteById(routeID);
        this.unsetRoute(route);
        //}
    }

    private void unsetRoute(@NotNull Route route) throws FindFailed, InterruptedException, IOException, AWTException, JSchException {
        Screen screen = new Screen();
        if(!Objects.equals(route.getPrecedenceTrack(), "")){
            this.unBlockSignalByCli(route.getPrecedenceTrack(),"Track");
        }
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
        checkConfirmation(route.getId().split("/")[0]);
        setScreen(route.getRouteEntry(),"cancel");

        if (route.hasAdditionalRoute()){
            setScreen(route.getRouteExit(),"cancel");
        }
        if(route.isTrackDrop())trackOperation("Pick",route.getBeforeTrack());

        System.out.println("The route has been cancelled for Route "+route.getId());
    }

    private void unsetRouteInRear(@NotNull Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SDD");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void blockByCli(@NotNull Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SLB");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void setFleetingOnByCli(@NotNull Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SFO");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void setFleetingOffByCli(Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SMO");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    private void unblockByCli(Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SLU");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void turnArsOnByCli(Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("STD");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void setRouteByCli(String entryRoute,String exitRoute) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SRO");
        al.add(entryRoute);
        al.add(exitRoute);
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    private void turnArsOffByCli(Route Route) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SMO");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    public void setDisregardOffByCli(String object,String objectType) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        String operation = Objects.equals(objectType, "Signal") ? "SRD" : "TCR";
        if (Objects.equals(stationMode.getControlType(),"control")) {
            al.add(operation);
            al.add(object);
            CmdLine.send(al);
        }else {
            String command = "/opt/fg/bin/CLIsend.sh -d -c" + operation + " "+ object;
            if (!stationMode.getControlDevice().getIpAddress().isEmpty()) {
                sshManager = SSHManager.getInstance("sysadmin", "tcms2009", stationMode.getControlDevice().getIpAddress(), 22);
            }
            sshManager.sendCommand(command);
        }
        Thread.sleep(3000);
    }
    public void setDisregardOnByCli(String object,String objectType) throws IOException, InterruptedException {

        String operation = Objects.equals(objectType, "Signal") ? "SDD" : "TCD";
        if (Objects.equals(stationMode.getControlType(),"control")) {
            ArrayList<String> al = new ArrayList<>();
            al.add(operation);
            al.add(object);
            CmdLine.send(al);
    }else {
        String command = "/opt/fg/bin/CLIsend.sh -d -c " + operation + " "+ object;
        if (!stationMode.getControlDevice().getIpAddress().isEmpty()) {
                sshManager = SSHManager.getInstance("sysadmin", "tcms2009", stationMode.getControlDevice().getIpAddress(), 22);
            }
        sshManager.sendCommand(command);
    }
        Thread.sleep(3000);
    }
    private void cancelARSByCli(String signal) throws IOException, InterruptedException {
//        if (Objects.equals(stationMode.getControlType(),"Control")) {
            ArrayList<String> al = new ArrayList<>();
            al.add("SMO");
            al.add(signal);
            CmdLine.send(al);

//        }else {
//            String command = "/opt/fg/bin/CLIsend.sh -d -c SMO "+ signal;
//
//            sshManager.sendCommand(command);
//        }
        Thread.sleep(3000);
    }
    private void unBlockSignalByCli(String object,String objectType) throws IOException, InterruptedException {
        String operation = Objects.equals(objectType, "Signal") ? "SLU" : "TKU";
//        if (Objects.equals(stationMode.getControlType(),"Control")) {
            ArrayList<String> al = new ArrayList<>();

            al.add(operation);
            al.add(object);
            CmdLine.send(al);
            Thread.sleep(3000);
//        }else {
//            String command = "/opt/fg/bin/CLIsend.sh -d -c " + operation + " "+ object;
//
//            sshManager.sendCommand(command);
//        }
    }
    private void setBlockByCli(String object,String objectType) throws IOException, InterruptedException, FindFailed {
        String operation = Objects.equals(objectType, "Signal") ? "SLB" : "TKB";
//        if (Objects.equals(stationMode.getControlType(),"Control")) {
           ArrayList<String> al = new ArrayList<>();

            al.add(operation);
            al.add(object);
            CmdLine.send(al);

//        }else {
//            String command = "/opt/fg/bin/CLIsend.sh -d -c " + operation + " "+ object;
//
//            sshManager.sendCommand(command);
//        }
        Thread.sleep(3000);








    }
    private void cancelRouteByCli(Route Route) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("CAN");
        al.add(Route.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
    }

    public void cleanRouteByID(String routeId) throws IOException, InterruptedException {
        Route route = this.getRouteById(routeId);
        isClear=false;
        additionalFRoute = null;
        additionalBRoute = null ;
        trackOperation("Pick",route.getAfterTrack());
        trackOperation("Pick",route.getBeforeTrack());




    }
}