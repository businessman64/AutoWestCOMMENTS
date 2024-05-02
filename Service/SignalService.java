package Service;

import com.jcraft.jsch.JSchException;
import controller.SignalOperations;
import exceptions.NetworkException;
import exceptions.ObjectStateException;
import model.*;
import org.jetbrains.annotations.NotNull;
import org.sikuli.script.*;
import org.xml.sax.SAXException;
import util.CmdLine;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


enum SignalState{
    NORMAL,
    BLOCKED,
    DISREGARDED,
}

public class SignalService {
    public static SignalService signalService;

    public  static ScreenService screenService;
    public List<Signal> signals;
    InterlockingService interlockingService;
    CoordinateService coordinateService;
    StationMode stationMode = StationMode.getInstance();
    RouteService routeService;
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());

    Boolean isBlock;
    Boolean isFleet;
    Boolean isArs;
    Boolean isDisregard;
    private Deps deps;
    Boolean isLowSpeed;
    View currentView;


     SignalService() throws ParserConfigurationException, IOException, SAXException, NetworkException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();
        generateSignals();



    }

    private void generateSignals() throws ParserConfigurationException, IOException, SAXException, NetworkException {

             signals = new ArrayList<>();
             List<SignalInfo> al = util.XMLParser.parseSignals("RailwaySignals.xml");
            String state="";
            if (stationMode != null && stationMode.getLocalServer() != null) {
                if ("sim".equalsIgnoreCase(stationMode.getLocalServer())) {
                    state = "SIMTCMS";
                } else {
                    state = "TCMS";
                }
            }else {
                state = "TCMS";
            }
        System.out.println(state);
             for (SignalInfo eachSignal : al) {
                 String signalName[] = eachSignal.getSignalId().split("/");
                 Location location = coordinateService.getCoordinatedById(eachSignal.getSignalId());
                 Location coordinate = coordinateService.getScreenCoordinatedById(eachSignal.getSignalId());

                 String rsmMessage = RailwayStateManagerService.getInstance().getSignalState(eachSignal.getSignalId(),state);
                 try {

                     isBlock = rsmMessage.charAt(6) != 'x' || rsmMessage.charAt(7) != 'x' || rsmMessage.charAt(8) != 'x';
//                     isArs = rsmMessage.charAt(22) == '0'||rsmMessage.charAt(22) == '1';
                     isFleet = rsmMessage.charAt(19) == '0'|| rsmMessage.charAt(19) == '1';
                     isDisregard = rsmMessage.charAt(5) == '0'||rsmMessage.charAt(5) == '1';
                     isLowSpeed = rsmMessage.charAt(17) == '0' || rsmMessage.charAt(17) == '1';

                 } catch (Exception e) {
                     isBlock = false;
                     isFleet = false;
                     isDisregard = false;
                     isLowSpeed = false;
                 }

                 signals.add(new Signal(eachSignal.getSignalId(), signalName[1], eachSignal.getType(), eachSignal.getsignalTrack(),
                         eachSignal.getRelation(),
                         isBlock, isFleet,
                         eachSignal.getHasArs(), isDisregard, isLowSpeed,
                         this.interlockingService.getInterlockingByName(signalName[0]), location
                         , coordinate
                 ));
             }
         }


    public static SignalService getInstance() throws ParserConfigurationException, IOException, SAXException, NetworkException {
        if(signalService==null){
            signalService = new SignalService();
            return signalService;
        }
        else {return signalService;}
    }
    public List<Signal> getSignals(){
        return this.signals;
    }

    public String getRandomSignal(boolean railwayData) {
        List<Signal> signals =null;
         if(railwayData){
              signals = getSignals();
         }else {
              signals = getUpdatedSignal();
         }
        List<Signal> filteredSignals = signals.stream()
                .filter(signal -> signal.getId().contains("/S"))
                .collect(Collectors.toList());

        if (filteredSignals.isEmpty()) {
            return null; // Handle the case where no signals meet the criteria
        }

        Random random = new Random();
        return filteredSignals.get(random.nextInt(filteredSignals.size())).getId();
//        if (signals.isEmpty()) {
//            return null; // or handle the empty list scenario
//        }
//        Random random = new Random();
//        return signals.get(random.nextInt(signals.size())).getId();
    }
    private void setScreen(String object, String action) throws InterruptedException {

        currentView = ViewManager.getInstance().getCurrentView();
        object.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
        // String type ="";

        String ipAddress="localhost";
        if(stationMode.getControlType().equals("noncontrol")) {

            ipAddress = stationMode.getSimDevice().getIpAddress();
        }
        if(stationMode.isTCWindowRequired()){
            CmdLine.getResponseSocket(stationMode.getTcDevice().getScreen(),object,"Signal",stationMode.getTcDevice().getIpAddress());

        }

        CmdLine.getResponseSocketDifferent(currentView.getName(),object,"Signal",ipAddress,"control");

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
    public   List<Signal> getSignalByTrack(String trackId){
         List<Signal> signFound = new ArrayList<>();
        for(Signal signal : this.getSignals()){
            if(signal.getSignalTrack().equals(trackId) && !signal.getId().split("/")[1].startsWith("C")){
                signFound.add(signal);
            }
        }
        //System.out.print("Did not find the signal for track "+ trackId);
        return signFound;
    }
    public  Signal getSignalById(String signalId){
        for(Signal signal : this.getSignals()){
            if(signal.getId().equals(signalId)){
                return  signal;
            }
        }
        System.out.print("Did not find the object for signal"+ signalId);
        return null;
    }
    public void blockSignalById(String signalId) throws FindFailed, NetworkException, ParserConfigurationException, SAXException, ObjectStateException, IOException, InterruptedException, JSchException, AWTException {
        Signal signal = this.getSignalById(signalId);
        if(signal.getBlock()){
        this.blockSignal(signal); }else{
        System.out.println("no block");
    }


}
    public void unblockSignalById(String signalId) throws FindFailed, ParserConfigurationException, SAXException, ObjectStateException, NetworkException, FileNotFoundException, InterruptedException {

        Signal signal = this.getSignalById(signalId);
        if (signal.getBlock()) {
            this.unblockSignal(signal);
        }else{
            System.out.println("no block");
        }

    }

    public void setDisregardOnById(String signalId) throws FindFailed, ObjectStateException, NetworkException, ParserConfigurationException, SAXException, FileNotFoundException, InterruptedException {

        Signal signal = this.getSignalById(signalId);
            this.setDisregardOn(signal);




    }
    public void setDisregardOffById(String signalId) throws FindFailed, ObjectStateException, NetworkException, ParserConfigurationException, SAXException, FileNotFoundException, InterruptedException {

        Signal signal = this.getSignalById(signalId);
            this.setDisregardOff(signal);
    }
    public void setFleetingOffById(String signalId) throws ParserConfigurationException, NetworkException, SAXException, FileNotFoundException, ObjectStateException, FindFailed, InterruptedException {
        Signal signal = this.getSignalById(signalId);
        if(signal.getFleet()){
            this.setFleetingOff(signal);
    }else{
        System.out.println("no Fleeting");
    }

    }

    private void setFleetingOff(Signal signal) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to set fleeting Signal off"+signal.getId());

        setScreen(signal.getId(),"click");
       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
       // Thread.sleep(4000);


        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/signals/signal_fleeting_off.png",3).click();
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        }catch(FindFailed ff){
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            System.out.println("Dropdown Menu option was not located. "+ff.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }





    public void setFleetingOnById(String signalId, RouteService routeServiceDeps) throws ObjectStateException, ParserConfigurationException, NetworkException, SAXException, FileNotFoundException, FindFailed, InterruptedException {

        Signal signal = this.getSignalById(signalId);
//        this.setFleetingOn(signal,routeServiceDeps);
        if(signal.getFleet()){
            this.setFleetingOn(signal,routeServiceDeps);
    }else{
        System.out.println("no Fleeting");
    }
    }

    private void setFleetingOn(Signal signal,RouteService routeServiceDeps) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to Block Signal "+signal.getId());
        //signal.getLocation().click();
        setScreen(signal.getId(),"nonClickable");

       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        try{
            Route route = routeServiceDeps.getRouteBySignal(signal.getId(),"forward");

            routeServiceDeps.setRouteById(route.getId(),true);


            Thread.sleep(2000);

            setScreen(signal.getId(),"click");
            Thread.sleep(1000);

            if (stationMode.getLocalServer().equals("noncontrol")){
                screen.wait(System.getProperty("user.dir")+"/src/resources/signals/signal_fleeting_on.png",3).click();

            }else {
                for (int i = 0; i < 4; i++) {
                    screen.type(Key.DOWN);
                    Thread.sleep(500);
                }
            }
            screen.type(null,Key.ENTER);

            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            Thread.sleep(2000);
            takeScreenShot(signal.getId()+":fleeting",false,"","");
            Thread.sleep(1000);
//            routeServiceDeps.unsetRouteById(route.getId());
        }catch(FindFailed ff){
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            System.out.println("Dropdown Menu option was not located. "+ff.getMessage());

        } catch (InterruptedException | JSchException | IOException | AWTException e) {
            throw new RuntimeException(e);
        } catch (ObjectStateException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (NetworkException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

public void setLowSpeedByID(String signalID,RouteService routeServiceDeps) throws FindFailed, InterruptedException {
    Signal signal = this.getSignalById(signalID);
    if(signal.getLowSpeed()){
        this.setLowSpeedOn(signal,routeServiceDeps);
    }else{
        System.out.println("no LowSpeed");
    }
}
    private void setLowSpeedOn(Signal signal,RouteService routeServiceDeps) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to Block Signal "+signal.getId());
        //signal.getLocation().click();
        setScreen(signal.getId(),"nonClickable");
        // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        try{
            Route route = routeServiceDeps.getRouteBySignal(signal.getId(),"forward");

            routeServiceDeps.setRouteById(route.getId(),true);
            Thread.sleep(2000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            routeServiceDeps.trackOperation("Drop",route.getAfterTrack());
            Thread.sleep(1000);
            routeServiceDeps.trackOperation("Drop",route.getBeforeTrack());
            setScreen(signal.getId(),"click");

            screen.wait(System.getProperty("user.dir")+"/src/resources/signals/signal_lowSpeed.png",3).click();
            Thread.sleep(20000);
            takeScreenShot(signal.getId()+":LowSpeedPip",false,"","");
            Thread.sleep(20000);
            takeScreenShot(signal.getId()+":LowSpeed",false,"","");
            Thread.sleep(500);
            routeServiceDeps.unsetRouteById(route.getId());
            routeServiceDeps.trackOperation("Pick",route.getAfterTrack());
            Thread.sleep(1000);
            routeServiceDeps.trackOperation("Pick",route.getBeforeTrack());
            routeServiceDeps.unsetRouteById(route.getId());


            routeServiceDeps.unsetRouteById(route.getId());
        }catch(FindFailed ff){
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            System.out.println("Dropdown Menu option was not located. "+ff.getMessage());

        } catch (InterruptedException | JSchException | IOException | AWTException e) {
            throw new RuntimeException(e);
        } catch (ObjectStateException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (NetworkException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setArsOffById(String signalId) throws ObjectStateException, IOException, FindFailed, ParserConfigurationException, NetworkException, SAXException, InterruptedException {
        Signal signal = this.getSignalById(signalId);
        if(signal.getArs()) {
           // this.setArsOff(signal);
            this.turnArsOffByCli(signal.getId());
        }
        else{
            System.out.println("no ARS");
        }

    }



    private void setArsOff(Signal signal) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to set ARS off for Signal "+signal.getId());
        //signal.getLocation().click();
        setScreen(signal.getId(),"click");
       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        screen.type(Key.DOWN);
        Thread.sleep(500);
        screen.type(Key.DOWN);
        Thread.sleep(500);
        screen.type(Key.DOWN);
        Thread.sleep(500);
        screen.type(null,Key.ENTER);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();


    }

    public void setArsOnById(String signalId) throws ParserConfigurationException, NetworkException, SAXException, ObjectStateException, FileNotFoundException, FindFailed, InterruptedException {
        Signal signal = this.getSignalById(signalId);
        System.out.println(signal.getArs());
        if(signal.getArs()) {
            this.setArsOn(signal);
        }else{
            System.out.println("no ARS");
        }
    }

    private void setArsOn(Signal signal) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to set ARS on for Signal "+signal.getId());
        setScreen(signal.getId(),"click");
        //Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/signals/signal_ars_on.png",3).click();
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            Thread.sleep(1000);
            takeScreenShot(signal.getId()+":ARS",false,"","");

        }catch(FindFailed ff){
            System.out.println("Dropdown Menu option was not located. "+ff.getMessage());
        } catch (JSchException | IOException | AWTException e) {
            throw new RuntimeException(e);
        }
    }

//    public void configureSignalById(String signalId) throws IOException, FindFailed, InterruptedException {
//        this.configureSignal(getSignalById(signalId));
//    }
    public void setRouteById(String signalId) throws ObjectStateException, IOException, ParserConfigurationException, NetworkException, SAXException, FindFailed {

        String exitSignalId = util.XMLParser.parseExitSignal(signalId);
       // this.setRoute(this.getSignalById(signalId),this.getSignalById(exitSignalId));

    }

    private void setRoute(Signal entrySignal, Signal exitSignal) throws FindFailed {

        logger.info("Attempting to set a Route for "+entrySignal.getId()+ " and "+exitSignal.getId());
        entrySignal.getLocation().click();
        exitSignal.getLocation().click();
        logger.info("The route has been set between the signals "+entrySignal.getId()+" and "+exitSignal.getId());
    }

    public void unsetRouteById(String signalId) throws ObjectStateException, FileNotFoundException, ParserConfigurationException, NetworkException, SAXException, FindFailed {
        Signal signal = this.getSignalById(signalId);

        this.unsetRoute(signal);
        //}
    }

    private void unsetRoute(Signal signal) throws FindFailed {

        signal.getLocation().rightClick();
        logger.info("The route has been cancelled for signal "+signal.getId());
    }

    public void blockSignal(Signal signal) throws FindFailed, InterruptedException, IOException, JSchException, AWTException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        //signal.getLocation().click();
        setScreen(signal.getId(),"click");
        takeScreenShot(signal.getId()+":dropdown",false,"","");

        this.turnArsOffByCli(signal.getId());
        //Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
       try{
//           screen.exists(System.getProperty("user.dir") + "/src/resources/signals/blockInit.png").click();
//           screen.type(null,Key.ENTER);

           screen.wait(System.getProperty("user.dir") + "/src/resources/track_block.png").click();

           Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
           Thread.sleep(1000);
           takeScreenShot(signal.getId()+":block",false,"","");
       }catch(FindFailed ff){
           System.out.println("Dropdown Menu option was not located. "+ff.getMessage());
       } catch (JSchException | AWTException e) {
           throw new RuntimeException(e);
       }
    }
    public void unblockSignal(Signal signal) throws FindFailed, InterruptedException {

//        Screen screen = (Screen) signal.getLocation().getScreen();
          System.out.println("Attempting to unblock Signal "+signal.getId());
//        screenService.findScreen(screen,signal.getScreenCoordinates());
//        Thread.sleep(4000);

        //signal.getLocation().click();


        setScreen(signal.getId(),"click");
        //Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Thread.sleep(4000);

        try{
            screen.wait(System.getProperty("user.dir") + "/src/resources/track_unblock.png").click();
            screen.wait(System.getProperty("user.dir") + "/src/resources/Confirmation_yes.png", 4).click();

            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            Thread.sleep(1000);

        }catch(FindFailed ff){
            System.out.println("Dropdown Menu option was not located");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void setDisregardOn(Signal signal) throws FindFailed, InterruptedException {


//        Screen screen = (Screen) signal.getLocation().getScreen();
        System.out.println("Attempting to set the disregard on for Signal: "+signal.getId());
//        signal.getLocation().click();
        setScreen(signal.getId(),"click");
       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/track_disregard.png",2).click();
            logger.info("Completed turning on disregard for Signal "+signal.getId());
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            takeScreenShot(signal.getId()+":disregard",false,"","");

        }catch(FindFailed ff){
            System.out.println("Dropdown Menu option was not located. "+ff.getMessage());
        } catch (JSchException | AWTException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setDisregardOff(Signal signal) throws FindFailed, InterruptedException {
       // Screen screen = (Screen) signal.getLocation().getScreen();
        System.out.println("Attempting to set the disregard off for Signal: " + signal.getId());
        //signal.getLocation().click();


        setScreen(signal.getId(),"click");
       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();


        try {
            screen.wait(System.getProperty("user.dir")+"/src/resources/track_disregard.png", 2).click();
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        }catch(FindFailed ff){
            System.out.println("Dropdown Menu option was not located");
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    public void configureSignalById() throws IOException {
        System.out.println("getting response from Socket");
//        System.out.println(CmdLine.getResponseSocket());
    }

    private void takeScreenShot(String name, Boolean fullScreen, String BLK , String PK) throws AWTException, JSchException, IOException, InterruptedException {
        int valueA= 200;
        int valueB=500;
        //ScreenImage screenImage = getScreenImage(fullScreen,valueA,valueB);
        BufferedImage screenImage = getScreenImage(fullScreen,valueA,valueB, BLK, PK);
        File directory = new File("SignalImages");
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



    }

    private BufferedImage getScreenImage(boolean fullScreen,int valueA, int valueB,String BLK , String PK) {
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
        int textX = 10; // Example x position
        int textY = 20; // Example y position, adjust as needed
        String text= BLK +" " + PK;
        // Draw the text
        g2.drawString(text, textX, textY);
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

    public List<Signal> getUpdatedSignal(){
        if (stationMode.getFile() != null) {
            List<String> values = util.XMLParser.parseCSV(stationMode.getFile().getAbsolutePath()); // Assuming parseCSV returns List<String> and getFile() returns a File object.
            List<Signal> matchingSignal = this.signals.stream()
                    .filter(signal -> values.contains(signal.getId()))
                    .collect(Collectors.toList());
            return matchingSignal;
        }else{
            return this.signals;
        }
    }

    private void unsetRouteInRear(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SDD");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(1000);

    }
    private void blockByCli(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SLB");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void setFleetingOnByCli(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SFO");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void setFleetingOffByCli(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SMO");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    private void unblockByCli(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SLU");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void turnArsOnByCli(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("STD");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void setRouteByCli(String entrySignal,String exitSignal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SRO");
        al.add(entrySignal.trim());
        al.add(exitSignal.trim());
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    private void turnArsOffByCli(String signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SMO");
        al.add(signal);
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    public void setDisregardOffByCli(Signal signal) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SRD");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
    }
    public void setDisregardOnByCli(Signal signal) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SDD");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
    }
    private void cancelRouteByCli(Signal signal) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("CAN");
        al.add(signal.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
    }
}