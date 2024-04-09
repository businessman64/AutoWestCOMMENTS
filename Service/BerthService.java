package Service;

import com.jcraft.jsch.JSchException;
import model.*;
import java.awt.*;
import org.jetbrains.annotations.NotNull;
import org.python.indexer.ast.NContinue;
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
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class BerthService {
    public static BerthService BerthService;
    StationMode stationMode = StationMode.getInstance();
    SSHManager sshManager;
    SSHManager sshManager_sim;
    public static ScreenService screenService;
    public List<OverViewBerth> overViewBerths;
    public List<BerthSignal> berthsSignals;
    InterlockingService interlockingService;
    CoordinateService coordinateService;
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());
    String lastSignal;
    Boolean blockDisabled;
    Boolean fleetDisabled;
    Boolean arsDisabled;
    Boolean disregardDisabled;
    private static final String ALPHANUMERIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random random = new Random();
    private static final Set<String> generatedTokens = new HashSet<>();
    View currentView;
    TrackService trackService;
    SignalService signalService;
    RouteService routeService;
    boolean isBerthCheckPossible = false;

    BerthService() throws ParserConfigurationException, IOException, SAXException, JSchException, InterruptedException, AWTException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();
        signalService = new SignalService();
        trackService = new TrackService();
        routeService = new RouteService();
        generateBerth();

    }

    private void generateBerth() throws ParserConfigurationException, IOException, SAXException, JSchException, InterruptedException, AWTException {
        berthsSignals = new ArrayList<>();
        overViewBerths = util.XMLParser.parseOverViewBerth("RailwayOverviews.xml");
        for (BerthInfo berth : trackService.berths) {
            List<String> listOfOverview = new ArrayList<>();
            for (OverViewBerth overViewBerth : overViewBerths) {
                for (String berths : overViewBerth.getBerthIds()) {
                    if (berths.equals(berth.getTrackId())) {
                        listOfOverview.add(overViewBerth.getOverViewId());

                    }
                }
                berth.setOverView(listOfOverview);
            }
        }
    }

    private void setScreen(@NotNull String object, String action) throws InterruptedException {

        currentView = ViewManager.getInstance().getCurrentView();
        object.replaceFirst("[A-Z]{3}/S(.*)", "[A-Z]{3}S\1");
        String type = object.split("/")[1].startsWith("S") ? "Signal" : "Berth";


        String ipAddress = "localhost";
        if (stationMode.getControlType().equals("noncontrol")) {

            ipAddress = stationMode.getSimDevice().getIpAddress();
        }
        if (stationMode.isTCWindowRequired()) {
            CmdLine.getResponseSocket(stationMode.getTcDevice().getScreen(), object, type, stationMode.getTcDevice().getIpAddress());

        }

        CmdLine.getResponseSocketDifferent(currentView.getName(), object, type, ipAddress);

        // CmdLine.sendSocketCmdTest(currentView.getName(), signal, type);
        Thread.sleep(3000);
        Location centerLocation = new Location(currentView.getCoordinateX(), currentView.getCoordinateY());
        Thread.sleep(3000);
        if (!Objects.equals(action, "nonClickable")) {
            if (Objects.equals(action, "cancel")) {
                centerLocation.rightClick();
            } else {
                centerLocation.click();
            }
            Thread.sleep(2000);

        }
    }



    public static BerthService getInstance() throws ParserConfigurationException, IOException, SAXException, JSchException, InterruptedException, AWTException {
        if (BerthService == null) {
            BerthService = new BerthService();
            return BerthService;
        } else {
            return BerthService;
        }
    }


    public void moveTDNBySignalID(String signalId) throws FindFailed, InterruptedException, IOException {
        Signal signal = signalService.getSignalById(signalId);
        moveTDN(signal);

    }

    private void moveTDN(Signal signal) throws InterruptedException, FindFailed, IOException {
        logger.info("Attempting to set Move Berth " + signal.getId());
        addBerth("MOVE", signalService.getRandomSignal());
        setScreen(signal.getId(), "click");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(), currentView.getCoordinateY()).getScreen();


        try {

            screen.wait(System.getProperty("user.dir")+"/src/resources/berth/Signal_train_number.png",3).click();
            Thread.sleep(1000);
            for (int i = 0; i < 4; i++) {
                screen.type(Key.DOWN);
            }
            screen.type(null,Key.ENTER);
           // screen.wait(System.getProperty("user.dir") + "/src/resources/berth/TDN_Move.png", 3).click();
            Thread.sleep(1000);
            //screen.type(Key.DOWN, KeyModifier.NONE)

            screen.wait(System.getProperty("user.dir")+"/src/resources/berth/Enter_Train_Num.png",3).click();
            screen.type("MOVE");
            Thread.sleep(2000);
            screen.type(null,Key.ENTER);


        } catch (FindFailed ff) {
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            throw new FindFailed("Dropdown Menu option was not located. " + ff.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


        public void exchangeTDNBySignalID(String signalId) throws FindFailed, InterruptedException {
            List<String> berthId = trackService.getBerthByTrackId(signalService.getSignalById(signalId).getSignalTrack());

            exchangeTDN(berthId);

        }
        private void exchangeTDN(List<String>  berthId) throws InterruptedException, FindFailed {
            logger.info("Attempting to exchange TDN from the object "+berthId);

            for(String berth : berthId) {
                setScreen(berth, "click");
            }
            Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();


            try{

                screen.wait(System.getProperty("user.dir")+"/src/resources/berth/TDN_Exchange.png",3).click();
                Thread.sleep(1000);
                removeBerth("EXCH");
                screen.wait(System.getProperty("user.dir")+"/src/resources/berth/Enter_Train_Num.png",3).click();
                screen.type("EXCH");
                Thread.sleep(2000);
                screen.type(null,Key.ENTER);

                screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            }catch(FindFailed ff){
                screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

                throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    public void eraseTDNBySignalID(String signalId, boolean isEraseBySignal) throws FindFailed, InterruptedException {
        if (isEraseBySignal) {
            Signal signal = signalService.getSignalById(signalId);

            eraseBerthBySignalDropdown(signal.getId(),new ArrayList<>());
        }else{
            List<String> berthId = trackService.getBerthByTrackId(signalService.getSignalById(signalId).getSignalTrack());

            eraseBerthBySignalDropdown("",berthId);
        }

    }
    private void eraseBerthBySignalDropdown(String object,List<String> berthId) throws FindFailed, InterruptedException {
        logger.info("Attempting to Erase TDN from the object "+object);
           if(!berthId.isEmpty()) {
               for(String berth: berthId) {
                   setScreen(berth, "click");
               }
           }else{
               setScreen(object, "click");
           }
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();


        try{
            if (berthId.isEmpty()){
                screen.wait(System.getProperty("user.dir")+"/src/resources/berth/Signal_train_number.png",3).click();
                Thread.sleep(100);
            }
            screen.wait(System.getProperty("user.dir")+"/src/resources/berth/TDN_Erase.png",3).click();
            Thread.sleep(1000);

            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        }catch(FindFailed ff){
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void insertBerthBySignalID(String signalId) throws FindFailed, InterruptedException {



       Signal  signal = signalService.getSignalById(signalId);

       insertBerth(signal);

    }

    private void insertBerth(Signal signal) throws InterruptedException, FindFailed {
        logger.info("Attempting to insert berth"+signal.getId());

        setScreen(signal.getId(),"click");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/berth/Signal_train_number.png",3).click();
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir")+"/src/resources/berth/TDN_Insert.png",3).click();
            Thread.sleep(1000);
            screen.wait(System.getProperty("user.dir")+"/src/resources/berth/Enter_Train_Num.png",3).click();
            removeBerth("TEST");
            screen.type("TEST");
            Thread.sleep(1500);
            screen.type(null,Key.ENTER);

        }catch(FindFailed ff){
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            System.out.println(ff.getMessage());
            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void twoBerthBySignalId(String signalId) throws InterruptedException, IOException, JSchException, AWTException {
        Route route = null;
        Signal signal =null;

        try {
            if(!signalId.split("/")[1].startsWith("C")) {
                lastSignal=null;
                route = routeService.getRouteByIdExitSignal(signalId);
                if (route == null) signal = signalService.getSignalById(signalId);
            }


        }catch (Exception e){
            System.out.println(e);
        }


        this.twoBerthTest(route,signal,signalId );
    }

    public void RemoveBerthSignal(String signal)
    {
        BerthSignal object = getberthSignalById(signal);
        berthsSignals.remove(object);

        System.out.println("removed");
    }
    private void setRouteByCli(String entrySignal,String exitSignal) throws InterruptedException, IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add("SRO");
        al.add(entrySignal);
        al.add(exitSignal);
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    private void twoBerthTest(Route route, Signal signal, String signalId ) throws InterruptedException, IOException, JSchException, AWTException {


        if(route != null && route.getRouteTracks().size() > 1){
            setScreen(route.getRouteEntry(),"click");
            Thread.sleep(1000);
            setScreen(route.getRouteExit(),"click");
            Thread.sleep(4000);
            setScreen(route.getRouteEntry(),"cancel");
            Thread.sleep(2000);
            Signal exitSignal =signalService.getSignalById(route.getRouteExit());
            String LastTrack ="";
            if (exitSignal.getType().toLowerCase().matches("(.*_automatic)|(phantom)")){
                LastTrack = exitSignal.getRelation().contains("b")? exitSignal.getSignalTrack()
                        : getNextTrack(exitSignal.getRelation(),
                        trackService.getTrackById(exitSignal.getSignalTrack())).getCircuitName();
            }else {

                LastTrack = route.getRouteTracks().get(route.getRouteTracks().size() - 1);
            }
            System.out.println(" track before entry: "+route.getBeforeTrack()+" entry signal"+route.getRouteEntry()+" the berth to click track"+LastTrack+" test signal:"+route.getRouteExit()+" spad track:"+route.getRouteTracks().get(0));
             berthsSignals.add(new BerthSignal(route.getRouteExit(),route.getRouteEntry(),route.getBeforeTrack(),LastTrack,route.getRouteTracks().get(0),""));
            isBerthCheckPossible= true;
        }else if( signal != null){
            if (findTrackNumber(signal).size()>2 && lastSignal!=null){
                List<Track> tracksList = findTrackNumber(signal);

                //setScreen(signal.getId(),"nonClickable");
                if(tracksList.size()<12) {
                    Track LastTrack = tracksList.get(tracksList.size() - 1);
                    Track LastSecondTrack = tracksList.get(tracksList.size() - 2);
                    System.out.println(" track before entry:" + LastTrack.getId() + " entry signal:" + lastSignal + " berth track:" + tracksList.get(0).getId() + " signal to be tested:" + signal.getId() + " spad track: " + LastSecondTrack.getId());
                    berthsSignals.add(new BerthSignal(signal.getId(), lastSignal, LastTrack.getCircuitName(), tracksList.get(0).getCircuitName(), LastSecondTrack.getCircuitName(), ""));
                    isBerthCheckPossible= true;

                }else{
                    logger.info("Too many tracks before the exit signal:"+signal.getId());

                }
            }
            else{
                logger.info("Number of track is between two signals is one for signal:"+signal.getId());
            }

        }else{
            logger.info("Number of track is between two signals is one for signal:"+signalId);
        }

        }
        private List<Track> findTrackNumber(Signal signal)
    {
        Track signalTrack= trackService.getTrackById(signal.getsignalTrack());
        boolean signalRelationMatched=false;
        List<Track> trackList = new ArrayList<>();
        Signal LastSignal = null;
        String direction = signal.getRelation().substring(0,2);
        String compDir= signal.getRelation();

        boolean excludeThisTrack = signal.getRelation().contains("h");
        if(!excludeThisTrack) {
            trackList.add(signalTrack); // Add the initial track to the list
        }

        Track nextTrack = null;

        while (true) {
            if(signalTrack != null) {
                nextTrack = getNextTrack(signal.getRelation(), signalTrack);
            }else {
                System.out.println("end signal "+signal.getId());
                break;
            }

            if (trackList.size() > 13) { // Adjust this limit based on your application's needs
                logger.info("Warning: Stopping due to excessive track processing "+signal.getId() );
                isBerthCheckPossible= false;
                break;

            }
            List<Signal> foundSignal= new ArrayList<>();

            if(nextTrack != null && signalService.getSignalByTrack(nextTrack.getId()) != null) {
                foundSignal=signalService.getSignalByTrack(nextTrack.getId());
                for (Signal TrackSignal : foundSignal) {
                    signalRelationMatched = findDirection(TrackSignal.getRelation(), compDir);
                    if (signalRelationMatched) {

                        trackList.add(nextTrack);
                        if(!TrackSignal.getRelation().contains("b")) {
                            Track finalTrack = getNextTrack(TrackSignal.getRelation(), nextTrack);
                            if (finalTrack != null) trackList.add(finalTrack);
                        }
                        lastSignal =TrackSignal.getId() ;
                        break;

                    }

                }
            } else if (nextTrack == null) {
                System.out.println("Signal does not have a entry signal");
                break;
            }

            if(signalRelationMatched){
                break;
            }
            if (nextTrack != null && !trackList.contains(nextTrack)) {
                trackList.add(nextTrack);
            }
            signalTrack = nextTrack;

        }

return trackList;
    }

    private boolean findDirection(String newDirection, String direction) {

        if (direction.matches("dn[nr]h")) {
            return newDirection.matches("up[nr]b") || newDirection.matches("dn[nr]h");
        } else if (direction.matches("dn[nr]b")) {
            // Interpret and adjust your patterns as needed
            return newDirection.matches("up[nr]h") || newDirection.matches("dn[nr]b");
        } else if (direction.matches("up[nr]b")) {
            // Adjust according to your logic
            return newDirection.matches("up[nr]b") || newDirection.matches("dn[nr]h");
        } else if (direction.matches("up[nr]h")) {
            // Adjust according to your logic
            return newDirection.matches("up[nr]h") || newDirection.matches("dn[nr]b");
        }
        return false;
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

    public void trackOperation(String operation, @NotNull String trackId) throws IOException, InterruptedException {
        String trackGuido = trackId.replace("/", "");
        System.out.println(trackId + " track " + operation);
        String command = "/opt/fg/bin/clickSimTrk" + operation + ".sh 0 0 0 0 0 0 0 " + trackGuido;
        if (!stationMode.getControlDevice().getIpAddress().isEmpty()) {
            sshManager_sim = SSHManager.getInstance("sysadmin", "tcms2009", stationMode.getControlDevice().getIpAddress(), 22);
        }

        sshManager_sim.sendCommand(command);

    }

    public List<BerthSignal> getBerthSignals(){
        return this.berthsSignals;
    }

    public BerthSignal getberthSignalById(String signalID){
        for(BerthSignal berthSignal : this.getBerthSignals()){
            if(berthSignal.getSignal().equals(signalID)){
                return  berthSignal;
            }
        }
        System.out.print("Did not find the object for signal "+ signalID);
        return null;
    }
    private static String generateUniqueToken() {
        while (true) {
            StringBuilder token = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                int index = random.nextInt(ALPHANUMERIC_CHARACTERS.length());
                char randomChar = ALPHANUMERIC_CHARACTERS.charAt(index);
                token.append(randomChar);
            }
            if (!generatedTokens.contains(token.toString())) {
                generatedTokens.add(token.toString());
                return token.toString();
            }
        }
    }
    public void dropAllTracks(String signal) throws IOException, InterruptedException, JSchException, AWTException {
        this.twoBerthBySignalId(signal);
        setScreen(signal,"nonClickable");
        Thread.sleep(2000);
        if(isBerthCheckPossible) {
            for (BerthSignal signals : this.getBerthSignals()) {

                if (signals.getSignal().equals(signal)) {
                    trackOperation("Drop", trackService.getTrackById(signals.getBehindTrack()).getCircuitName());
                    trackOperation("Drop", trackService.getTrackById(signals.getBerthTrack()).getCircuitName());
                }
            }
            Thread.sleep(15000);

            for (BerthSignal signals : this.getBerthSignals()) {
                if (signals.getSignal().equals(signal)) {
                    String token1 = generateUniqueToken();
                    String token2 = generateUniqueToken();
                    signals.setBerthStored(token1 + ":" + token2);

                    addBerth(token2, signals.getSignal());
                    Thread.sleep(100);
                    addBerth(token1, signals.getEntrySignal());
                    System.out.println("berth added");
                }
            }
        }else{
            logger.info("Berth cannot be checked for signal:"+signal);
        }
    }

    public void dropSPADTracks(String signal,boolean one_by_one) throws IOException, InterruptedException, AWTException {
        String  trackName= "";

        System.out.println("print: "+one_by_one);
        if(isBerthCheckPossible) {
            for (BerthSignal signals : this.getBerthSignals()) {

                if (signals.getSignal().equals(signal)) {
                    trackName = trackService.getTrackById(signals.getSpadTrack()).getCircuitName();
                    trackOperation("Drop", trackService.getTrackById(signals.getSpadTrack()).getCircuitName());
                    Thread.sleep(1000);
                    if (!one_by_one) {
                        removeBerth(signals.getBerthStored().split(":")[0]);
                        removeBerth(signals.getBerthStored().split(":")[1]);
                    }
                    if (one_by_one) {
                        System.out.println("print 2:" + one_by_one);
                        Thread.sleep(1000);
                        List<String> trackValue = trackService.getBerthByTrackId(signals.getBerthTrack());
                        if (!trackValue.isEmpty()) {
                            for(String berthId: trackValue) {
                                setScreen(berthId, "click");
                            }
                         //   System.out.println(trackService.getBerthByTrackId(signals.getBerthTrack()));
                        } else {
                            logger.info("berth not found for signal:" + signals.getSignal());
                        }
                        Thread.sleep(1000);
                        captureAndCreateGif(signal + ":blinking", true, "blinking", "now", 60, 1);
                    }
                }
            }
        }
    }

    public void pickTrack(String signal, boolean one_by_one) throws IOException, InterruptedException, FindFailed {
        if(isBerthCheckPossible) {
            for (BerthSignal signals : this.getBerthSignals()) {
                if (signals.getSignal().equals(signal)) {
                    if (one_by_one) {
                        removeBerth(signals.getBerthStored().split(":")[0]);
                        removeBerth(signals.getBerthStored().split(":")[1]);
                        Thread.sleep(1000);
                    }

                    trackOperation("Pick", trackService.getTrackById(signals.getSpadTrack()).getCircuitName());
                    trackOperation("Pick", trackService.getTrackById(signals.getBerthTrack()).getCircuitName());
                    trackOperation("Pick", trackService.getTrackById(signals.getBehindTrack()).getCircuitName());
                    Thread.sleep(1000);
                    Screen screen = (Screen) new Location(currentView.getCoordinateX(), currentView.getCoordinateY()).getScreen();

                    screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

                }
            }
        }
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
            if (!stationMode.getControlDevice().getIpAddress().isEmpty()) {
                sshManager = SSHManager.getInstance("sysadmin", "tcms2009", stationMode.getControlDevice().getIpAddress(), 22);
            }
            sshManager.sendCommand(command);
        }
        System.out.println("Berth "+berthName+" removed ");
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

            BufferedImage screenImage = getScreenImage(fullScreen, 200, 500, BLK, PK);
            gifWriter.writeToSequence(screenImage);
           // Thread.sleep(captureInterval);
        }
        gifWriter.close();
        output.close();
    }
    private void takeScreenShot( String name, Boolean fullScreen, String BLK , String PK) throws AWTException, JSchException, IOException, InterruptedException {
        int valueA = 200;
        int valueB = 600;
        //ScreenImage screenImage = getScreenImage(fullScreen,valueA,valueB);
        BufferedImage screenImage = getScreenImage(fullScreen, valueA, valueB, BLK, PK);
        File directory = new File("BerthImages");
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
            List<Signal> matchingSignal = this.signalService.signals.stream()
                    .filter(signal -> values.contains(signal.getId()))
                    .collect(Collectors.toList());
            return matchingSignal;
        }else{
            return this.signalService.signals;
        }
    }

}