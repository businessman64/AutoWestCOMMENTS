package Service;

import controller.TrackOperations;
import exceptions.ImageNotConfiguredException;
import exceptions.NetworkException;
import exceptions.ObjectStateException;
import model.*;
import org.jetbrains.annotations.NotNull;
import org.sikuli.script.*;
import org.xml.sax.SAXException;
import util.CmdLine;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;


enum TrackState{
     NORMAL,
    BLOCKED,
    DISREGARDED,
}

public class TrackService {
    public static TrackService trackService;

    public  static ScreenService screenService;
    public List<Track> tracks;

    public List<BerthInfo> berths;
    InterlockingService interlockingService;
    View currentView;
    CoordinateService coordinateService;
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());

    TrackService() throws ParserConfigurationException, IOException, SAXException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();
        generateTracks();
    }

    private void generateTracks() throws ParserConfigurationException, IOException, SAXException {
        tracks = new ArrayList<Track>();
        berths= util.XMLParser.parseBerth(new File(System.getProperty("user.dir") + "/src/main/java/data/RailwayData/RailwayBerths.xml"));

        List<TrackInfo> al= util.XMLParser.parseTracks(new File(System.getProperty("user.dir") + "/src/main/java/data/RailwayData/RailwayTracks.xml"));
        for(TrackInfo eachTrack:al){
            if (!Objects.equals(eachTrack.getCircuitName(), "")) {
                String trackName[] = eachTrack.getCircuitName().split("/");
                Location location = coordinateService.getCoordinatedById(eachTrack.getCircuitName());
                Location coordinate = coordinateService.getScreenCoordinatedById(eachTrack.getCircuitName());
                tracks.add(new Track(eachTrack.getTrackId(), trackName[1], eachTrack.getCircuitName(),
                        eachTrack.getTrackNormalDown(),
                        eachTrack.getTrackNormalUp(),
                        eachTrack.getTrackReverseDown(),
                        eachTrack.getTrackReverseUp(),
                        this.interlockingService.getInterlockingByName(trackName[0]), location,coordinate ));
            }
        }
    }

    public static TrackService getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(trackService==null){
            trackService = new TrackService();
            return trackService;
        }
        else {return trackService;}
    }
    public List<Track> getTracks(){

        return this.tracks;
    }

private void setScreen(@NotNull String signal, String action) throws InterruptedException {
        currentView = ViewManager.getInstance().getCurrentView();
    signal.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
    String type = signal.split("/")[1].contains("S")? "Route": "Berth";
    CmdLine.sendSocketCmdTest(currentView.getName(), signal, "Track");

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
    public String getBerthByTrackId(String trackId){
        String currentBerth = "";
        for(BerthInfo berth : berths){
            if(Objects.equals(berth.getTrackId(), trackId)){
                currentBerth = berth.getId();
                break;
            }
        }
        return currentBerth;

    }

    public Track getTrackById(String trackId){
        Track currentTrack = null;
        for(Track track : this.getTracks()){
            if(Objects.equals(track.getCircuitName(), trackId) || Objects.equals(track.getId(), trackId)){
                currentTrack = track;
                break;
            }
        }
        return currentTrack;

    }

    public Location getCoordinateById(String trackId){
        Location currentCorrdinate = null;
        for(Track track : this.getTracks()){
            if(track.getCircuitName() == trackId){
                currentCorrdinate = track.getScreenCoordinates();
                break;
            }
        }
        return currentCorrdinate;

    }


    public List blockTrackById(String trackId) throws ImageNotConfiguredException, FindFailed, NetworkException, ParserConfigurationException, SAXException, ObjectStateException, IOException, InterruptedException {
        Track track = this.getTrackById(trackId);
        Location OriginalLocation = this.getCoordinateById(trackId);
        List result = null;
        this.blockTrack(track, OriginalLocation);
        return result;

    }



    public void unblockTrackById(String trackId) throws FindFailed, ParserConfigurationException, SAXException, ObjectStateException, NetworkException, FileNotFoundException, InterruptedException {

        Track track = this.getTrackById(trackId);
        this.unblockTrack(track);

    }

    public List setDisregardOnTrackById(String trackId) throws FindFailed, ObjectStateException, NetworkException, ParserConfigurationException, SAXException, FileNotFoundException, InterruptedException {

        Track track = this.getTrackById(trackId);
        List result = null;
        //result = IPEngineService.getMatch(track,track.getImagePath(),null);
        this.setDisregardOn(track);
        return result;

    }
    public List setDisregardOffTrackById(String trackId) throws FindFailed, ObjectStateException, NetworkException, ParserConfigurationException, SAXException, FileNotFoundException, InterruptedException {

        Track track = this.getTrackById(trackId);
        List result = null;
        //result = IPEngineService.getMatch(track,track.getImagePathDisregardedState(),null);
        this.setDisregardOff(track);
        return result;

    }
    public void configureTrackById(String trackId) throws IOException, FindFailed, InterruptedException {
        this.configureTrack(getTrackById(trackId));
    }
    public void blockTrack(Track track, Location ScreenCoordinate) throws FindFailed, InterruptedException, IOException {
        //Screen screen = (Screen) track.getLocation().getScreen();
        logger.info("Attempting to Block Track "+track.getId());
        //screenService.findScreen(screen,ScreenCoordinate);
        setScreen(track.getId(),"click");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        try{// in build
            Match match = screen.wait(System.getProperty("user.dir")+"/src/resources/track_block.png",3);
            match.click();
        }catch(FindFailed ff){
            throw new FindFailed("Dropdown Menu option was not located. "+ff.getMessage());
        }
    }
    public void unblockTrack(Track track) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) track.getLocation().getScreen();
        logger.info("Attempting to unblock Track "+track.getId());
        //track.getLocation().click();
        setScreen(track.getId(),"click");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        try{
            Match match1 =screen.wait(System.getProperty("user.dir")+"/src/resources/track_unblock.png");
            match1.click();
            Thread.sleep(2);
            screen.wait(System.getProperty("user.dir")+"/src/resources/Confirmation_yes.png",4).click();
        }catch(FindFailed ff){
            logger.warning("This track cannot be unblocked:Dropdown Menu option was not located");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Completed unblocking Track "+track.getId());
    }
    public void setDisregardOn(Track track) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) track.getLocation().getScreen();
        logger.info("Attempting to set the disregard on for Track "+track.getId());
        //track.getLocation().click();
        setScreen(track.getId(),"click");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        try{
            Match match = screen.wait(System.getProperty("user.dir")+"/src/resources/track_disregard.png",2);
            match.click();
            logger.info("Completed turning on disregard for Track "+track.getId());
        }catch(FindFailed ff){
            System.out.println("This track cannot be disregarded:Dropdown Menu option was not located");
        }
    }
    public void setDisregardOff(Track track) throws FindFailed, InterruptedException {
        //Screen screen = (Screen) track.getLocation().getScreen();
        logger.info("Attempting to set the disregard off for Track:  "+track.getId());
        //track.getLocation().click();
        setScreen(track.getId(),"click");
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        try{
            Match match = screen.wait(System.getProperty("user.dir")+"/src/resources/track_disregard.png",2);
            match.click();
            logger.info("Completed turning off the disregard for Track: "+track.getId());
        }catch(FindFailed ff){
            System.out.println("This track cannot be disregarded:Dropdown Menu option was not located");
        }
    }
    public void configureTrack(Track track) throws IOException, InterruptedException {
        try {
            int screenNumber = 0;
            Screen s = this.screenService.getScreens().get(screenNumber);
            Region region = s.selectRegion();
            Image.reload(track.getImagePath());
            region.getImage().save(track.getImagePath());
            logger.info("Blocking Track");
            //this.blockTrackByCli(track);
            Thread.sleep(4000);
            region.getImage().save(track.getImagePathBlockedState());
            logger.info("Calibration for blocked track complete");
            this.ublockTrackByCli(track);
            logger.info("Turning on disregard for the track ");
            this.setDisregardOnTrackByCli(track);
            Thread.sleep(4000);
            region.getImage().save(track.getImagePathDisregardedState());
            logger.info("Calibration for disregarded track complete");
            this.setDisregardOffByCli(track);
        }catch (NullPointerException ne){
            logger.info("There was an error configuring the object. PLease try again");
        }
    }

    public void testTrack(Set operations,String trackId)  {
        int count=0;
        logger.info("\nTest case "+count++);
        for(Object o :operations) {

            try {
                List result;
                if (o.equals(TrackOperations.BLOCKANDUNBLOCK)) {

                    //Test Blocked track
                    logger.info("\nBlock Operation ");
                    this.blockTrackById(trackId);
                    //Thread.sleep(8000);
                    //IPEngineService.getMatch(this.getTrackById(trackId),this.getTrackById(trackId).getImagePathBlockedState() , (View) result.get(1),8);
                    logger.info("Completed Blocking Track " + trackId+"\n");

                    //Test Unblocked track
                    logger.info("\nUnBlock Operation ");
                    this.unblockTrackById(trackId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getTrackById(trackId),this.getTrackById(trackId).getImagePath(), (View) result.get(1),8);
                    logger.info("Completed Unblocking Track "+trackId);

                }
                if (o.equals(TrackOperations.SETDISREGARDONOFF)) {
                    // Test Disregarded track
                    logger.info("\nSet Disregard On Operation ");
                    result = this.setDisregardOnTrackById(trackId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getTrackById(trackId),this.getTrackById(trackId).getImagePathDisregardedState(), (View) result.get(1),8);
                    logger.info("Completed Disregarding Track "+trackId);

                    //Test Disregard off
                    logger.info("\nSet Disregard Off Operation ");
                    this.setDisregardOffTrackById(trackId);
                    //Thread.sleep(4000);
                    //IPEngineService.getMatch(this.getTrackById(trackId),this.getTrackById(trackId).getImagePath(), (View) result.get(1),8);
                    logger.info("Completed setting disregard off for Track "+trackId);
                }
            }catch (FindFailed e){
                logger.info(trackId+": "+e.getMessage()+". The object was not located on the screen");
            }catch (ImageNotConfiguredException | IOException e){
                logger.info(trackId+": "+e.getMessage()+". This object is not configured in the system.");
            }catch (ObjectStateException | ParserConfigurationException | NetworkException | SAXException e){
                logger.info(trackId+": "+e.getMessage()+". Communication for this object might have been broken");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                logger.info("Test case:"+trackId+" failed.");
            }
        }
    }
    public void blockTrackByCli(Track track) throws IOException, InterruptedException, FindFailed {
        ArrayList<String> al = new ArrayList<>();
        al.add("TKB");
        al.add(track.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    public void setDisregardOnTrackByCli(Track track) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("TCD");
        al.add(track.getId());
        CmdLine.send(al);
        Thread.sleep(1000);
    }
    public void ublockTrackByCli(Track track) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("TKUI");
        al.add(track.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
        al.clear();
        al.add("TKU");
        al.add(track.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
    }
    public void setDisregardOffByCli(Track track) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("TCR");
        al.add(track.getId());
        CmdLine.send(al);
        Thread.sleep(3000);
    }


    public void dropSimTrackById(String o) {
    }

    public void pickSimTrackById(String o) {
    }

    public void failTrackById(String o) {
    }

    public void unfailTrackById(String o) {
    }
}