package Service;

import controller.SignalOperations;
import exceptions.NetworkException;
import exceptions.ObjectStateException;
import model.Signal;
import model.SignalInfo;
import model.View;
import model.ViewManager;
import org.jetbrains.annotations.NotNull;
import org.sikuli.script.*;
import org.xml.sax.SAXException;
import util.CmdLine;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


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
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());

    Boolean blockDisabled;
    Boolean fleetDisabled;
    Boolean arsDisabled;
    Boolean disregardDisabled;
    View currentView;
     SignalService() throws ParserConfigurationException, IOException, SAXException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();
        generateSignals();
    }

    private void generateSignals() throws ParserConfigurationException, IOException, SAXException {
        signals = new ArrayList<>();
        List<SignalInfo> al= util.XMLParser.parseSignals(new File(System.getProperty("user.dir") + "/src/main/java/data/RailwayData/RailwaySignals.xml"));
        for(SignalInfo eachSignal:al){
            String signalName[] = eachSignal.getSignalId().split("/");
            Location location = coordinateService.getCoordinatedById(eachSignal.getSignalId());
            Location coordinate = coordinateService.getScreenCoordinatedById(eachSignal.getSignalId());

            signals.add(new Signal(eachSignal.getSignalId(),signalName[1],eachSignal.getType() ,eachSignal.getsignalTrack(),
                    eachSignal.getRelation(),
                    blockDisabled, fleetDisabled,
                    arsDisabled, disregardDisabled,
                    this.interlockingService.getInterlockingByName(signalName[0]),location
                    ,coordinate
            ));
        }
    }

    public static SignalService getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(signalService==null){
            signalService = new SignalService();
            return signalService;
        }
        else {return signalService;}
    }
    public List<Signal> getSignals(){
        return this.signals;
    }

    public String getRandomSignal() {
        List<Signal> signals = getSignals();
        if (signals.isEmpty()) {
            return null; // or handle the empty list scenario
        }
        Random random = new Random();
        return signals.get(random.nextInt(signals.size())).getId();
    }
    private void setScreen(@NotNull String signal, String action) throws InterruptedException {

        currentView = ViewManager.getInstance().getCurrentView();
        signal.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
        String type = signal.split("/")[1].contains("S")? "Route": "Berth";
        CmdLine.sendSocketCmdTest(currentView.getName(), signal, "Signal");

        Location centerLocation = new Location(currentView.getCoordinateX(), currentView.getCoordinateY());
        Thread.sleep(1000);
        if (!Objects.equals(action, "nonClickable")) {
            if (Objects.equals(action, "cancel")) {
                centerLocation.rightClick();
            } else {
                centerLocation.click();
            }
            Thread.sleep(2000);

        }
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
    public void blockSignalById(String signalId) throws FindFailed, NetworkException, ParserConfigurationException, SAXException, ObjectStateException, FileNotFoundException, InterruptedException {
        Signal signal = this.getSignalById(signalId);
        List result = null;
            this.blockSignal(signal);
        //}
    }
    public void unblockSignalById(String signalId) throws FindFailed, ParserConfigurationException, SAXException, ObjectStateException, NetworkException, FileNotFoundException, InterruptedException {

        Signal signal = this.getSignalById(signalId);
        List result = null;
            this.unblockSignal(signal);
        //}
    }

    public List setDisregardOnById(String signalId) throws FindFailed, ObjectStateException, NetworkException, ParserConfigurationException, SAXException, FileNotFoundException, InterruptedException {

        Signal signal = this.getSignalById(signalId);
        List result = null;
            this.setDisregardOn(signal);
        //}
        return result;

    }
    public void setDisregardOffById(String signalId) throws FindFailed, ObjectStateException, NetworkException, ParserConfigurationException, SAXException, FileNotFoundException, InterruptedException {

        Signal signal = this.getSignalById(signalId);
        List result = null;

            this.setDisregardOff(signal);

    }
    public void setFleetingOffById(String signalId) throws ParserConfigurationException, NetworkException, SAXException, FileNotFoundException, ObjectStateException, FindFailed, InterruptedException {
        Signal signal = this.getSignalById(signalId);
            //result = IPEngineService.getMatch(signal,signal.getImagePathFleeting(),null);
            this.setFleetingOff(signal);
        //}


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

            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFleetingOnById(String signalId) throws ObjectStateException, ParserConfigurationException, NetworkException, SAXException, FileNotFoundException, FindFailed, InterruptedException {

        Signal signal = this.getSignalById(signalId);
            this.setFleetingOn(signal);

    }

    private void setFleetingOn(Signal signal) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to Block Signal "+signal.getId());
        //signal.getLocation().click();
        setScreen(signal.getId(),"click");
       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/signals/signal_fleeting_on.png",3).click();
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            Thread.sleep(1000);

        }catch(FindFailed ff){
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setArsOffById(String signalId) throws ObjectStateException, FileNotFoundException, FindFailed, ParserConfigurationException, NetworkException, SAXException, InterruptedException {
        Signal signal = this.getSignalById(signalId);
        this.setArsOff(signal);

    }

    private void setArsOff(Signal signal) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        logger.info("Attempting to set ARS off for Signal "+signal.getId());
        //signal.getLocation().click();
        setScreen(signal.getId(),"click");
       // Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/signals/signal_ars_off.png",3).click();
            new Location(1280, 740).click();
        }catch(FindFailed ff){
            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());
        }
    }

    public void setArsOnById(String signalId) throws ParserConfigurationException, NetworkException, SAXException, ObjectStateException, FileNotFoundException, FindFailed, InterruptedException {
        Signal signal = this.getSignalById(signalId);
        this.setArsOn(signal);
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

        }catch(FindFailed ff){
            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());
        }
    }

//    public void configureSignalById(String signalId) throws IOException, FindFailed, InterruptedException {
//        this.configureSignal(getSignalById(signalId));
//    }
    public void setRouteById(String signalId) throws ObjectStateException, IOException, ParserConfigurationException, NetworkException, SAXException, FindFailed {

        String exitSignalId = util.XMLParser.parseExitSignal(signalId);
        this.setRoute(this.getSignalById(signalId),this.getSignalById(exitSignalId));

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

    public void blockSignal(Signal signal) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) signal.getLocation().getScreen();
        //signal.getLocation().click();
        setScreen(signal.getId(),"click");
        //Location signalLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//signal.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
       try{
            screen.wait(System.getProperty("user.dir") + "/src/resources/track_block.png").click();
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").click();
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").click();

       }catch(FindFailed ff){
           screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
           System.out.println("Dropdown Menu option was not located");

       } catch (InterruptedException e) {
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

        }catch(FindFailed ff){
            System.out.println("Dropdown Menu option was not located");
        } catch (InterruptedException e) {
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
        System.out.println(CmdLine.getResponseSocket());
    }

//    public void configureSignal(Signal signal) {
//
//       try{
//           int screenNumber = 0;
//           Screen s = this.screenService.getScreens().get(screenNumber);
//           Region region = s.selectRegion();
//           Image.reload(signal.getImagePath());
//           region.getImage().save(signal.getImagePath());
//
//           logger.info("Blocking Signal");
//           this.blockByCli(signal);
//           Thread.sleep(4000);
//           Image.reload(signal.getImagePathBlockedState());
//           region.getImage().save(signal.getImagePathBlockedState());
//           logger.info("Calibration for blocked track complete");
//           this.unblockByCli(signal);
//
//           logger.info("Turning on disregard for the signal ");
//           this.setDisregardOnByCli(signal);
//           Thread.sleep(4000);
//           Image.reload(signal.getImagePathDisregardedState());
//           region.getImage().save(signal.getImagePathDisregardedState());
//           logger.info("Calibration for disregarded track complete");
//           this.setDisregardOffByCli(signal);
//
//           logger.info("Turning on ARS for the signal ");
//           this.turnArsOnByCli(signal);
//           Thread.sleep(4000);
//           Image.reload(signal.getImagePathArs());
//           region.getImage().save(signal.getImagePathArs());
//           logger.info("Calibration for ARS complete");
//           this.turnArsOffByCli(signal);
//
//           //TODO: Configure Signal fleeting image
//           logger.info("Setting the route for signal ");
//           this.setRouteByCli(signal.getId(),XMLParser.parseExitSignal(signal.getId()));
//           Thread.sleep(8000);
//           Image.reload(signal.getImagePathRouteSet());
//           region.getImage().save(signal.getImagePathRouteSet());
//           logger.info("Calibration for route set complete");
//
//           logger.info("Setting Fleeting on for the signal ");
//           this.setFleetingOnByCli(signal);
//           Thread.sleep(4000);
//           Image.reload(signal.getImagePathFleeting());
//           region.getImage().save(signal.getImagePathFleeting());
//           logger.info("Calibration for fleeting on complete");
//           this.setFleetingOffByCli(signal);
//           this.cancelRouteByCli(signal);
//
//       }catch (NullPointerException ne){
//           throw new RuntimeException(ne);
//            //logger.info("There was an error configuring the object."+ne.getMessage()+" Please try again");
//        } catch (ParserConfigurationException e) {
//           logger.info("There was an error configuring the object."+e.getMessage()+" Please try again");
//       } catch (SAXException | InterruptedException | IOException e) {
//           logger.info("There was an error configuring the object."+e.getMessage()+" Please try again");
//       }
//    }
    public void testSignal(Set operations,String signalId) throws Exception {
        int count=0;
        logger.info("\n==================================Test case "+count+++"=========================================");

        for(Object o :operations) {

            try {
                List result;
                if (o.equals(SignalOperations.BLOCKANDUNBLOCK)) {

                    //Test Blocked signal
                    logger.info("\nBlock Operation ");
                    this.blockSignalById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePathBlockedState() , (View) result.get(1),8);
                    logger.info("Completed Blocking Signal " + signalId+"\n");

                    //Test Unblocked signal
                    logger.info("\nUnBlock Operation ");
                    this.unblockSignalById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePath(), (View) result.get(1),8);
                    logger.info("Completed Unblocking Signal "+signalId);

                    logger.info("\n\nTest for Block/Unblock passed");
                }
                if (o.equals(SignalOperations.DISREGARDONOFF)) {
                    // Test Disregarded signal
                    logger.info("\nSet Disregard On Operation ");
                    result = this.setDisregardOnById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePathDisregardedState(), (View) result.get(1),8);
                    logger.info("Completed Disregarding Signal "+signalId);

                    //Test Disregard off
                    logger.info("\nSet Disregard Off Operation ");
                    this.setDisregardOffById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePath(), (View) result.get(1),8);
                    logger.info("Completed setting disregard off for Signal "+signalId);

                    logger.info("\n\nTest for Disregard/Regard passed");

                }
                if (o.equals(SignalOperations.ARSONOFF)) {
                    // Test Disregarded signal
                    logger.info("\nSet Disregard On Operation ");
                    this.setArsOnById(signalId);
                    Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePathArs(), (View) result.get(1),8);
                    logger.info("Completed Disregarding Signal "+signalId);

                    //Test Disregard off
                    logger.info("\nSet Disregard Off Operation ");
                    this.setArsOffById(signalId);
                    Thread.sleep(5000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePath(), (View) result.get(1),8);
                    logger.info("Completed setting disregard off for Signal "+signalId);

                    logger.info("\n\nTest for ARS On/Off passed");

                }
                if (o.equals(SignalOperations.FLEETINGONOFF)) {
                    // Test Disregarded signal

                    logger.info("\nSetting route On Operation");
                    this.setRouteById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePathRouteSet(), (View) result.get(1),8);
                    logger.info("Completed setting the route "+signalId);

                    logger.info("\nSet fleeting On Operation");
                    this.setFleetingOnById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePathFleeting(), (View) result.get(1),8);
                    logger.info("Completed turning the fleeting on Signal "+signalId);

                    //Test Disregard off
                    logger.info("\nSet fleeting Off Operation ");
                    this.setFleetingOffById(signalId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePathRouteSet(), (View) result.get(1),8);
                    logger.info("Completed turning the fleeting off for Signal "+signalId);

                    //Test Disregard off
                    logger.info("\nSet fleeting Off Operation ");
                    this.unsetRouteById(signalId);
                    //Thread.sleep(6000);
                    //IPEngineService.getMatch(this.getSignalById(signalId),this.getSignalById(signalId).getImagePath(), (View) result.get(1),8);
                    logger.info("Completed turning the fleeting off for Signal "+signalId);

                }
            }catch (FindFailed e){
                throw new FindFailed(signalId+": "+e.getMessage()+". The object was not located on the screen");
            }catch (FileNotFoundException e){
                throw new FileNotFoundException(signalId+": "+e.getMessage()+". This object is not configured in the system.");
            }catch (ObjectStateException | ParserConfigurationException | NetworkException | SAXException | InterruptedException | IOException e) {
                throw new Exception(signalId + ": " + e.getMessage() + ". Communication for this object might have been broken");
            }
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
        al.add(entrySignal);
        al.add(exitSignal);
        CmdLine.send(al);
        Thread.sleep(1000);
    }

    private void turnArsOffByCli(Signal signal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SMO");
        al.add(signal.getId());
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