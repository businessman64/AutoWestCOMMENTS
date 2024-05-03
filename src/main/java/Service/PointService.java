package Service;

import controller.PointController;
import controller.TrackOperations;
import exceptions.ImageNotConfiguredException;
import exceptions.InvalidOperationException;
import exceptions.NetworkException;
import exceptions.ObjectStateException;
import model.*;
import org.jetbrains.annotations.NotNull;
import org.sikuli.script.*;
import org.xml.sax.SAXException;
import util.CmdLine;
import config.AppConfig;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import config.AppConfig;
import util.Utility;


public class PointService {
    public static PointService pointService;
    View currentView;
    public  static ScreenService screenService;
    private ArrayList<Point> points;
    InterlockingService interlockingService;
    CoordinateService coordinateService;
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());

    private PointService() throws ParserConfigurationException, IOException, SAXException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();

        generateTracks();
    }


    private void setScreen(@NotNull String object, String action) throws InterruptedException {

        currentView = ViewManager.getInstance().getCurrentView();
        object.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
        String type = object.split("/")[1].contains("S")? "Route": "Berth";
        CmdLine.sendSocketCmdTest(currentView.getName(), object, "Point");

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


    private void generateTracks() throws ParserConfigurationException, IOException, SAXException {
        points = new ArrayList<Point>();
        List<String> al= util.XMLParser.parsePoints(new File(System.getProperty("user.dir") + "/src/main/java/data/RailwayData/RailwayTracks.xml"));
        for(String pointId:al){
            String interlockingName = pointId.split("/")[0];
            Location location = coordinateService.getCoordinatedById(pointId);
            Location coordinate = coordinateService.getScreenCoordinatedById(pointId);

            points.add(new Point(pointId,pointId,
                    System.getProperty("user.dir")+"/src/resources/points/"+pointId+"NormalState.PNG",
                    System.getProperty("user.dir")+"/src/resources/points/"+pointId+"CentreState.PNG",
                    System.getProperty("user.dir")+"/src/resources/points/"+pointId+"ReverseState.PNG",
                    this.interlockingService.getInterlockingByName(interlockingName),location
                    ,coordinate
            ));
        }
    }

    public static PointService getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(pointService==null){
            pointService = new PointService();
            return pointService;
        }
        else {return pointService;}
    }
    public List<Point> getPoints(){
        return this.points;
    }


    public Point getPointById(String pointId){
        Point currentPoint = null;
        for(Point point : this.getPoints()){
            if(point.getId() == pointId){
                currentPoint = point;
                break;
            }
        }
        return currentPoint;

    }
    public void reversePointById(String pointId) throws FindFailed, ObjectStateException, IOException, ParserConfigurationException, NetworkException, SAXException, InterruptedException {

        Point point = this.getPointById(pointId);
        this.reverse(point);
    }
    public void normalisePointById(String pointId) throws IOException, FindFailed, ObjectStateException, ParserConfigurationException, NetworkException, SAXException, InterruptedException {

        Point point = this.getPointById(pointId);
        this.normalise(point);
        Thread.sleep(8000);
        this.closePointWindow(pointId);

    }

    private void displayPointControlPanel(List result) throws InterruptedException {
        Match match = (Match)result.get(0);
        match.click();
        Thread.sleep(1000);
        match.click();
    }

    public void configurePointById(String pointId) throws IOException, FindFailed, InterruptedException {
        this.configurePoint(getPointById(pointId));
    }

    public void blockState(Screen screen, Point point) throws FindFailed {
        Match match;
        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/points/point_state_block.png",3).click();
            logger.info("Completed blocking point "+point.getId());
            screen.mouseMove(new Location(0,0));

        }catch(FindFailed ff){
            throw new FindFailed("Block option was not located. "+ff.getMessage());
        }
    }
    public void unblockState(Screen screen, Point point) throws  FindFailed {
        Match match;
        try{
            screen.wait(System.getProperty("user.dir")+"/src/resources/points/point_state_unblock.png",3).click();
            screen.wait(System.getProperty("user.dir")+"/src/resources/Confirmation_yes.png",8).click();
            logger.info("Completed unblocking point "+point.getId());
        }catch(FindFailed ff){
            throw new FindFailed("Unblock option was not located. "+ff.getMessage());
        }
    }

    public void normalise(Point point) throws InterruptedException, FindFailed {
//        Screen screen = (Screen) point.getLocation().getScreen();
        logger.info("Attempting to normalise point "+point.getId());
        //screenService.findScreen(screen,point.getScreenCoordinates());
        //Thread.sleep(4000);
//        point.getLocation().click();
//        Thread.sleep(2000);
//        point.getLocation().offset(0,-100).click();
//        Thread.sleep(2000);
        setScreen(point.getId(),"click");
       // Location pointLocation = new Location(currentView.getCoordinateX(),currentView.getCoordinateY());//point.getLocation()

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

        Thread.sleep(2000);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
        Thread.sleep(4000);

        try{

            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_control_button.png",8).click();
            Thread.sleep(8000);
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_normal_button.png",8).click();
            logger.info("Completed clicking the normal button for point "+point.getId());
        }catch(FindFailed ff){
            System.out.println("Normalise the point failed. ");
        }
    }
    public void reverse(Point point) throws FindFailed, InterruptedException {

        //Screen screen = (Screen) point.getLocation().getScreen();
        logger.info("Attempting to reverse point "+point.getId());
        //point.getLocation().click();
        //Thread.sleep(2000);
        //point.getLocation().offset(0,-100).click();
        //signal.getLocation().click();

        setScreen(point.getId(),"click");
       // Location pointLocation = new Location(currentView.getCoordinateX()-10,currentView.getCoordinateY());//point.getLocation()

       Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

        Thread.sleep(2000);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        Thread.sleep(2000);
        try{
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_reverse_button.png",8).click();
            Thread.sleep(8000);
            logger.info("Completed reversing a point: "+point.getId());
            //screen.wait(System.getProperty("user.dir")+"/src/resources/points/point_control_button.png",2).click();
        }catch(FindFailed ff){
            System.out.println("This point cannot be reversed:reverse button was not located");
        }
    }
    public void centre(Match match, Point point) throws InterruptedException, FindFailed {
//        Screen screen = (Screen) point.getLocation().getScreen();
        logger.info("Attempting to centralise point "+point.getId());
//        point.getLocation().click();
//        Thread.sleep(1000);
//        point.getLocation().click();

        setScreen(point.getId(),"click");
       // Location pointLocation = new Location(currentView.getCoordinateX()-10,currentView.getCoordinateY());//point.getLocation()

       Screen screen = (Screen) new Location(currentView.getCoordinateX()-10,currentView.getCoordinateY()).getScreen();
        Thread.sleep(1000);
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

        Thread.sleep(2000);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        try{
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_control_button.png",8).click();
            Thread.sleep(8000);
            logger.info("Completed centralising the point: "+point.getId());
        }catch(FindFailed ff){
            System.out.println("This track cannot be disregarded:Dropdown Menu option was not located");
        }
    }
    public void configurePoint(Point point) throws InterruptedException, IOException {
        int screenNumber=0;
        Screen s = this.screenService.getScreens().get(screenNumber);
        Region region = s.selectRegion();
        Image.reload(point.getImagePathCenterState());
        region.getImage().save(point.getImagePathCenterState());
        this.normalisePointByCli(point);
        Thread.sleep(6000);
        Image.reload(point.getImagePathNormalState());
        region.getImage().save(point.getImagePathNormalState());
        this.centrePointByCli(point);
        this.reversePointByCli(point);
        System.out.println("Started waiting for reverse");
        Thread.sleep(12000);
        Image.reload(point.getImagePathReverseState());
        region.getImage().save(point.getImagePathReverseState());
        this.centrePointByCli(point);
    }
    public void normalisePointByCli(Point point) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("PTN");
        al.add(point.getId());
        CmdLine.send(al);
        Thread.sleep(8000);
    }
    public void reversePointByCli(Point point) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("PTR");
        al.add(point.getId());
        CmdLine.send(al);
        Thread.sleep(8000);
    }
    public void centrePointByCli(Point point) throws IOException, InterruptedException {
        ArrayList<String> al = new ArrayList<>();
        al.add("PTC");
        al.add(point.getId());
        CmdLine.send(al);
        Thread.sleep(8000);
    }


    public boolean preCheckControl(PointOperations operation,String name) throws IOException, ParserConfigurationException, SAXException, NetworkException, InvalidOperationException {
        RsmMessage rsmMessage = RailwayStateManagerService.getInstance().getPointState(this.getPointById(name));
        if(rsmMessage.isNormal()){
            logger.info("The point is in normal state");
            return false;
        }
        if(PointOperations.NORMALISE.equals(operation)){
            if(rsmMessage.isCentered()){
                logger.info("The point is in center state");
                return false;
            }
        }else if (PointOperations.REVERSE.equals(operation)){
            if(rsmMessage.isReversed()){
                logger.info("The point is in reverse state");
                return false;
            }
        }else{
            throw new InvalidOperationException("Invalid Operation given for point");
        }
        return true;
    }

    public boolean preCheckTest(PointOperations operation,String name) throws InterruptedException, IOException, ParserConfigurationException, SAXException, NetworkException, InvalidOperationException {
        boolean check = preCheckControl(operation, name);
        if(check == false && operation == PointOperations.NORMALISE){
            this.reversePointByCli(this.getPointById(name));
            check=true;
        }else if(check == false && operation == PointOperations.REVERSE){
            this.normalisePointByCli(this.getPointById(name));
            check=true;
        }
        return check;
    }
    public void closePointWindow(String pointId) throws FindFailed {
        try{
            //Screen screen = (Screen) this.getPointById(pointId).getLocation().getScreen();
            Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

            Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

            screen.wait(System.getProperty("user.dir")+"/src/resources/points/point_dialog_close_button.png",8).click();
        }
        catch (FindFailed ff){
            throw new FindFailed("Couldnt close the window. Close button not found");
        }
    }
    public void centralisePoint(String pointId) throws FindFailed, InterruptedException {
        try{
            Point point = getPointById(pointId);
            //Screen screen = (Screen) point.getLocation().getScreen();
            Screen screen = new Screen();
            screen.wait(System.getProperty("user.dir")+"/src/resources/points/point_control_button.png",8).click();
            Thread.sleep(8000);
        }
        catch (FindFailed ff){
            throw new FindFailed("Couldnt close the window. Close button not found");
        }
    }

    public void testPoint(Set operations,String pointId)  {
        int count=0;
        logger.info("\nTest case "+count++);
        for(Object o :operations) {

            try {
                if (o.equals(PointOperations.REVERSE)) {
                    Point point =this.getPointById(pointId);
                    //Test reverse point
                    this.reversePointById(pointId);
                    //Match match = (Match)result.get(0);
                    //IPEngineService.getMatch(this.getPointById(pointId),this.getPointById(pointId).getImagePathReverseState(), (View) result.get(1));
                    logger.info("Completed reversing the point "+pointId);

                    this.blockState((Screen) point.getLocation().getScreen(),point);
                    Thread.sleep(8000);
                    //IPEngineService.getMatch(this.getPointById(pointId),Point.UNBLOCKPOINT, (View) result.get(1));
                    //logger.info("Completed blocking the reverse state of the point "+pointId);

                    this.unblockState((Screen) point.getLocation().getScreen(),point);
                    Thread.sleep(8000);
                    //IPEngineService.getMatch(this.getPointById(pointId),Point.BLOCKPOINT, (View) result.get(1));
                    logger.info("Completed unblocking the reverse state of the point "+pointId);

                    this.closePointWindow(pointId);

                }
                if (o.equals(PointOperations.NORMALISE)) {
                    Point point =this.getPointById(pointId);
                    //Test normal point
                    this.normalisePointById(pointId);

                    //IPEngineService.getMatch(this.getPointById(pointId),this.getPointById(pointId).getImagePathNormalState(), (View) result.get(1));
                    logger.info("Completed reversing the point "+pointId);

                    //match.click();
                    this.blockState((Screen) point.getLocation().getScreen(),this.getPointById(pointId));
                    Thread.sleep(8000);
                    //IPEngineService.getMatch(this.getPointById(pointId),Point.UNBLOCKPOINT, (View) result.get(1));
                    logger.info("Completed blocking the reverse state of the point "+pointId);

                    this.unblockState((Screen) point.getLocation().getScreen(),this.getPointById(pointId));
                    Thread.sleep(8000);
                    //IPEngineService.getMatch(this.getPointById(pointId),Point.BLOCKPOINT, (View) result.get(1));
                    logger.info("Completed unblocking the reverse state of the point "+pointId);

                    this.closePointWindow(pointId);

                }
            }catch (FindFailed e){
                logger.info(pointId+": "+e.getMessage()+". The object was not located on the screen");
            }catch (FileNotFoundException e){
                logger.info(pointId+": "+e.getMessage()+". This object is not configured in the system.");
            }catch (ObjectStateException | ParserConfigurationException | NetworkException | SAXException e){
                logger.info(pointId+": "+e.getMessage()+". Communication for this object might have been broken");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                logger.info("Test case:"+pointId+" failed.");
            }
        }
    }

    /*public ArrayList centrePointById(String pointId) throws FindFailed {
        Point point = this.getPointById(pointId);
        ArrayList result = null;
        try{
            result = this.getMatchForPoint(point,PointOperations.CENTRE);
            this.reverse((Match) result.get(0),point);

        } catch (FindFailed | InterruptedException e) {
            throw new FindFailed("Could not locate track:"+pointId+". "+e.getMessage());
        } //catch (NetworkException ne){
        catch (InvalidOperationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (NetworkException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        // throw new NetworkException("Track Operation failed"+ne.getMessage());
        //}
        return result;
    }*/
    public String getImageByCurrentState(Point point) throws IOException, ParserConfigurationException, NetworkException, SAXException, InvalidOperationException {
        RsmMessage rsmMessage = RailwayStateManagerService.getInstance().getPointState(this.getPointById(point.getId()));
        if(rsmMessage.isNormal()){
            logger.info("The point is in normal state");
            return point.getImagePathNormalState();
        }
        else if(rsmMessage.isCentered()){
                logger.info("The point is in center state");
                return point.getImagePathCenterState();
        }
        else if(rsmMessage.isReversed()){
                logger.info("The point is in reverse state");
                return point.getImagePathReverseState();
        }
        else{
            throw new InvalidOperationException("The passed operation is invalid");
        }
    }
}
