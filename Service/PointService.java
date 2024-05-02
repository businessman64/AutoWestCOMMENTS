package Service;

import com.jcraft.jsch.JSchException;
import exceptions.InvalidOperationException;
import exceptions.NetworkException;
import exceptions.ObjectStateException;
import model.*;
import model.Point;
import org.jetbrains.annotations.NotNull;
import org.sikuli.script.*;
import org.sikuli.script.Image;
import org.xml.sax.SAXException;
import util.CmdLine;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


public class PointService {
    public static PointService pointService;
    View currentView;
    boolean openTCPanel;
    public  static ScreenService screenService;
    private ArrayList<Point> points;
    InterlockingService interlockingService;
    CoordinateService coordinateService;
    boolean bitBLKBool =false;
    boolean bitPKBool=false;
    Device controldevice;
    Device nonControldevice;
    Device tcdevice;
    private static final Logger logger = Logger.getLogger(TrackService.class.getName());
    StationMode stationMode = StationMode.getInstance();
    private PointService() throws ParserConfigurationException, IOException, SAXException, InterruptedException {
        screenService = ScreenService.getInstance();
        interlockingService = InterlockingService.getInstance();
        coordinateService = CoordinateService.getInstance();

        generateTracks();
    }

    public void initialHouseKeeping(String point) throws FindFailed, InterruptedException {
        currentView = ViewManager.getInstance().getCurrentView();

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 600, 600,screen);

        Match match_close = region.exists(System.getProperty("user.dir") + "/src/resources/points/point_dialog_close_button.png", 8);
        Match match_confirm = screen.exists(System.getProperty("user.dir")+"/src/resources/Confirmation_yes.png",8);

        if(match_close != null) closePointWindow(point);
        Thread.sleep(1000);
        if(match_confirm != null) screen.wait(System.getProperty("user.dir")+"/src/resources/Confirmation_yes.png",8).click();

    }

    private void setScreen(@NotNull String object, String action) throws InterruptedException {

        currentView = ViewManager.getInstance().getCurrentView();
        String ipAddress="localhost";
        object.replaceFirst("[A-Z]{3}/S(.*)","[A-Z]{3}S\1");
//        CmdLine.sendSocketCmdTest(currentView.getName(), object, "Point");

            if(stationMode.getControlType().equals("noncontrol")) {

                ipAddress = stationMode.getSimDevice().getIpAddress();
            }
            if(stationMode.isTCWindowRequired()){
                CmdLine.getResponseSocket(stationMode.getTcDevice().getScreen(),object,"Point",stationMode.getTcDevice().getIpAddress());
                if(openTCPanel) {
                    Location centerLocation = new Location(stationMode.getTcDevice().getScreenDeatils().getCoordinateX(), stationMode.getTcDevice().getScreenDeatils().getCoordinateY());
                    Thread.sleep(1000);
                    centerLocation.click();
                }
            }

        CmdLine.getResponseSocketDifferent(currentView.getName(),object,"Point",ipAddress,"control");
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


    private void generateTracks() throws ParserConfigurationException, IOException, SAXException, InterruptedException {
        points = new ArrayList<Point>();
        List<String> al= util.XMLParser.parsePoints("RailwayTracks.xml");
        for(String pointId:al){
            String interlockingName = pointId.split("/")[0];
            Location location = coordinateService.getCoordinatedById(pointId);
            Location coordinate = coordinateService.getScreenCoordinatedById(pointId);


            points.add(new Point(pointId,pointId,
                    bitPKBool,
                    bitBLKBool,
                    this.interlockingService.getInterlockingByName(interlockingName),location
                    ,coordinate
            ));
        }
    }

    private String findPointNumber(String object){
        Pattern pattern = Pattern.compile("[A-Z]{3}/P(\\d{1,4})");
        String extractedValue="";
        // Create a Matcher to find matches of the pattern in the text
        Matcher matcher = pattern.matcher(object.trim());

        // Check if the pattern was found
        if (matcher.find()) {
            // Group 1 contains the digits after
            extractedValue = matcher.group(1);
        } else {
            System.out.println("Pattern not found.");
        }
        return extractedValue;
    }


    public boolean extractMnemonic(String object, String mnemonics) throws IOException, InterruptedException {
        boolean isBitPresent=false;
        String interlockingName = object.split("/")[0];
        String pointNumber = findPointNumber(object);
        String commandString = "grep -nir "+pointNumber+"* /opt/scada/var/TCMS/*Fep/A/ | grep -oP \""+interlockingName+"(/P)?"+pointNumber+"(.*)\\."+mnemonics+ "\"";
        System.out.println(commandString);
        List<String> sshCommand = Arrays.asList("bash", "-c", "ssh -t tcmss004 '" + commandString + "'");
        String shhReturn = CmdLine.executeCurlics(sshCommand);
        Thread.sleep(2000);
        String[] lines = shhReturn.split("\n", -1);
        if (lines.length > 3) {
            isBitPresent=true;
        }
        System.out.println(isBitPresent);
        return isBitPresent;
    }

    public static PointService getInstance() throws ParserConfigurationException, IOException, SAXException, InterruptedException {
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


    public void reversePointById(String pointId, boolean bitPKBool, boolean bitBLKBool) throws FindFailed, ObjectStateException, IOException, ParserConfigurationException, NetworkException, SAXException, InterruptedException, JSchException, AWTException {

        Point point = this.getPointById(pointId);
        this.reverse(point,bitPKBool);
        Thread.sleep(8000);
        this.closePointWindow(pointId);
        takeScreenShot(point,point.getId()+":reverseWindow",false,"reverse","Point");

        Thread.sleep(2000);
        takeScreenShot(point,point.getId()+":reverse",false,"reverse","Point");
        if (bitBLKBool) {
            this.blockState(point);
            Thread.sleep(8000);
            this.closePointWindow(pointId);
            Thread.sleep(2000);
            takeScreenShot(point, point.getId() + ":reverseBlock", false, "reverse", "Block");

            this.unblockState(point,true);
            this.closePointWindow(pointId);
        }
    }
    public void normalisePointById(String pointId, boolean bitPKBool, boolean bitBLKBool) throws IOException, FindFailed, ObjectStateException, ParserConfigurationException, NetworkException, SAXException, InterruptedException, JSchException, AWTException {

        Point point = this.getPointById(pointId);

        this.normalise(point, bitPKBool);
        takeScreenShot(point,point.getId()+":normalWindow",false,"normal","Point");

        Thread.sleep(8000);
        this.closePointWindow(pointId);
        Thread.sleep(2000);
        takeScreenShot(point,point.getId()+":normal",false,"normal","Point");

        Thread.sleep(2000);
        if (bitBLKBool) {
            this.blockState(point);
            Thread.sleep(8000);
            this.closePointWindow(pointId);
            Thread.sleep(2000);
            takeScreenShot(point, point.getId() + ":normalBlock", false, "normal", "Block");
            Thread.sleep(1000);
            this.unblockState(point,true);
            this.closePointWindow(pointId);
        }


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


    public void CheckPointById(String pointId) throws IOException, FindFailed, InterruptedException, JSchException, AWTException {
        this.checkPoint(getPointById(pointId));
    }

    public void blockState(Point point) throws FindFailed, InterruptedException {
        setScreen(point.getId(),"click");

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 600, 600,screen);

        Thread.sleep(2000);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
        Thread.sleep(4000);

        try{
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_state_block.png",3).click();
            logger.info("Completed blocking point "+point.getId());
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        }catch(FindFailed ff){
            throw new FindFailed("Block option was not located. "+ff.getMessage());
        }
    }
    public void unblockState(Point point, boolean openPanel) throws FindFailed, InterruptedException {
        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();

        if(openPanel) {
            setScreen(point.getId(), "click");
            Thread.sleep(2000);
            screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
            Thread.sleep(4000);
        }
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 600, 600,screen);


        try{
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_state_unblock.png",3).click();
            screen.wait(System.getProperty("user.dir")+"/src/resources/Confirmation_yes.png",8).click();
            logger.info("Completed unblocking point "+point.getId());
        }catch(FindFailed ff){
            throw new FindFailed("Unblock option was not located. "+ff.getMessage());
        }
    }

    public void normalise(Point point, boolean bitPKBool) throws InterruptedException, FindFailed, JSchException, IOException, AWTException {
        logger.info("Attempting to normalise point "+point.getId());
        openTCPanel=true;
        setScreen(point.getId(),"click");


        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

        Thread.sleep(2000);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
        Thread.sleep(4000);
        takeScreenShot(point,point.getId()+":before",false,"without","function");

        try{
            if(bitPKBool) {
                region.wait(System.getProperty("user.dir") + "/src/resources/points/point_control_button.png", 8).click();
            }
            Thread.sleep(8000);
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_normal_button.png",8).click();
            Thread.sleep(10000);
            logger.info("Completed clicking the normal button for point "+point.getId());

        }catch(FindFailed ff){
            System.out.println("Normalise the point failed. ");
        }
    }
    public void reverse(Point point, boolean bitPKBool) throws FindFailed, InterruptedException {

        logger.info("Attempting to reverse point "+point.getId());

        openTCPanel=true;
        setScreen(point.getId(),"click");

       Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
       Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 500, 500,screen);

        Thread.sleep(2000);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

        Thread.sleep(2000);
        try{
            if(bitPKBool) {
                region.wait(System.getProperty("user.dir") + "/src/resources/points/point_control_button.png", 8).click();
            }
            Thread.sleep(8000);
            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_reverse_button.png",8).click();
            Thread.sleep(10000);
            logger.info("Completed reversing a point: "+point.getId());

        }catch(FindFailed ff){
            System.out.println("This point cannot be reversed:reverse button was not located");
        }
    }
    public void centre(Match match, Point point) throws InterruptedException, FindFailed {
        logger.info("Attempting to centralise point "+point.getId());


        setScreen(point.getId(),"click");

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

    private void takeScreenShot(Point point, String name, Boolean fullScreen, String BLK , String PK) throws AWTException, JSchException, IOException, InterruptedException {
            int valueA= 200;
            int valueB=500;
            //ScreenImage screenImage = getScreenImage(fullScreen,valueA,valueB);
            BufferedImage screenImage = getScreenImage(fullScreen,valueA,valueB, BLK, PK);
            File directory = new File("PointImages");
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

    private BufferedImage getScreenImageNew(Point point, boolean fullScreen,int valueA, int valueB, String BLK , String PK) {
        int xSc = currentView.getCoordinateX() ;
        int ySc = currentView.getCoordinateY() ;
        int x = currentView.getCoordinateX()-valueA;
        int y = currentView.getCoordinateY()-valueA;
        Screen screenshotSc =  (Screen) new Location(xSc,ySc).getScreen();
        ScreenImage imagScreenSC = screenshotSc.capture(x, y, valueB, valueB);
        // Get graphics context
        BufferedImage image = imagScreenSC.getImage();

        Graphics2D g2d = image.createGraphics();
        // Set properties (font, color, etc.)
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.setColor(Color.RED);

        // Determine the position where you want to draw the text
        int textX = 10; // Example x position
        int textY = 20; // Example y position, adjust as needed
        String text= BLK +" " + PK;
        // Draw the text
        g2d.drawString(text, textX, textY);
        g2d.dispose(); // Clean up
        return  image;
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


    public void checkPoint(Point pointId) throws InterruptedException, IOException, JSchException, AWTException, FindFailed {
        String bitPK = "([A-Z]PK)";
        String bitBLK="([A-Z]?BLK)";
        bitBLKBool = extractMnemonic(pointId.getId(),bitBLK);
        bitPKBool =extractMnemonic(pointId.getId(),bitPK);


        setScreen(pointId.getId(),"click");

        Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 500, 500,screen);
        screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();


        String BLK = bitBLKBool ? "Block" : "Block-no";
        String PK = bitPKBool ? "C" : "C-no";
        // Location pointLocation = new Location(currentView.getCoordinateX()-10,currentView.getCoordinateY());//point.getLocation()
        Match matchBlock = region.exists(System.getProperty("user.dir") + "/src/resources/points/point_state_block.png", 8);
        Match matchC = region.exists(System.getProperty("user.dir") + "/src/resources/points/point_control_button.png", 8);
        Match matchSleeve = region.exists(System.getProperty("user.dir") + "/src/resources/points/sleeve.png", 8);

        if(bitBLKBool && bitPKBool) {

            if (matchBlock != null && matchC != null && matchSleeve == null) {
                logger.info("point "+pointId.getId() +" panel is correct - block with C" );

            }else{
                logger.info("point "+pointId.getId() +" did not match panel, with blk: "+bitBLKBool +" and PK: "+ bitPKBool);

            }
        }
        else if(!bitBLKBool && !bitPKBool){
            if (matchSleeve != null && matchC == null) {
                logger.info("point "+pointId.getId() +" panel is correct - Sleeve with no C" );

            }else if (matchSleeve == null && matchC == null){

                logger.info("point "+pointId.getId() +" panel is correct - no Sleeve with no C" );
            }else{
                logger.info("point "+pointId.getId() +" did not match panel, with blk: "+bitBLKBool +" and PK: "+ bitPKBool);
            }

        }else if(!bitBLKBool && bitPKBool){
            logger.info("point "+pointId.getId() +" panel is correct - no block or sleeve with C" );
        }
        else{
            logger.info("point "+pointId.getId() +" Check");
        }

        takeScreenShot(pointId, pointId.getId()+":check",false, BLK, PK);
        Thread.sleep(2000);
        this.closePointWindow(pointId.getId());
    }

    public void configurePoint(Point point) throws InterruptedException, IOException {
        int screenNumber=0;
        Screen s = this.screenService.getScreens().get(screenNumber);
        Region region = s.selectRegion();
        Image.reload(point.getImagePathCenterState());
        region.getImage().save(point.getImagePathCenterState());
        this.normalisePointByCli(point);
        Thread.sleep(6000);

//        this.centrePointByCli(point);
//        this.reversePointByCli(point);
        System.out.println("Started waiting for reverse");
        Thread.sleep(12000);

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

            Screen screen = (Screen) new Location(currentView.getCoordinateX(),currentView.getCoordinateY()).getScreen();
            if(stationMode.isTCWindowRequired() && openTCPanel)
            {
               Screen screen2 = (Screen) new Location(stationMode.getTcDevice().getScreenDeatils().getCoordinateX(),
                        stationMode.getTcDevice().getScreenDeatils().getCoordinateY()).getScreen();
                 Region region_tc = new Region(stationMode.getTcDevice().getScreenDeatils().getCoordinateX()-200,
                         stationMode.getTcDevice().getScreenDeatils().getCoordinateY() - 200, 400, 400,screen2);
                region_tc.wait(System.getProperty("user.dir")+"/src/resources/points/point_dialog_close_button.png",8).click();
                screen2.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();

                openTCPanel=false;
            }
            Region region = new Region(currentView.getCoordinateX()-200, currentView.getCoordinateY() - 200, 400, 400,screen);

            region.wait(System.getProperty("user.dir")+"/src/resources/points/point_dialog_close_button.png",8).click();
        }
        catch (FindFailed ff){
            throw new FindFailed("Couldnt close the window. Close button not found");
        }
    }

    public List<Point> getUpdatedPoint(){
        if (stationMode.getFile() != null) {
            List<String> values = util.XMLParser.parseCSV(stationMode.getFile().getAbsolutePath()); // Assuming parseCSV returns List<String> and getFile() returns a File object.
            List<Point> matchingPoint = this.points.stream()
                    .filter(point -> values.contains(point.getId()))
                    .collect(Collectors.toList());
            return matchingPoint;
        }else{
            return this.points;
        }
    }
    public void centralisePoint(String pointId, boolean openPointPanel, boolean bitPKBool) throws FindFailed, InterruptedException {
        Screen screen = (Screen) new Location(currentView.getCoordinateX(), currentView.getCoordinateY()).getScreen();
        Region region = new Region(currentView.getCoordinateX() - 200, currentView.getCoordinateY() - 200, 400, 400, screen);
        Match match = region.exists(System.getProperty("user.dir") + "/src/resources/points/point_control_button.png", 8);

        if(match != null) {
            Point point = getPointById(pointId);



            if (openPointPanel) {
                setScreen(point.getId(), "click");
                Thread.sleep(2000);
                screen.wait(System.getProperty("user.dir") + "/src/resources/black.png").doubleClick();
                Thread.sleep(4000);
            }
            try {

                region.wait(System.getProperty("user.dir") + "/src/resources/points/point_control_button.png", 8).click();
                Thread.sleep(8000);
                if (openPointPanel) {
                    closePointWindow(pointId);
                    System.out.println("Centered and closed");
                }
            } catch (FindFailed ff) {
                throw new FindFailed("Couldnt close the window. Close button not found");
            }
        }else {
            logger.info("*PK bit not present");
        }
    }

//    public void testPoint(Set operations,String pointId)  {
//        int count=0;
//        logger.info("\nTest case "+count++);
//        for(Object o :operations) {
//
//            try {
//                if (o.equals(PointOperations.REVERSE)) {
//                    Point point =this.getPointById(pointId);
//                    //Test reverse point
//                    this.reversePointById(pointId);
//                    //Match match = (Match)result.get(0);
//                    //IPEngineService.getMatch(this.getPointById(pointId),this.getPointById(pointId).getImagePathReverseState(), (View) result.get(1));
//                    logger.info("Completed reversing the point "+pointId);
//
//                    this.blockState((Screen) point.getLocation().getScreen(),point);
//                    Thread.sleep(8000);
//                    //IPEngineService.getMatch(this.getPointById(pointId),Point.UNBLOCKPOINT, (View) result.get(1));
//                    //logger.info("Completed blocking the reverse state of the point "+pointId);
//
//                    this.unblockState((Screen) point.getLocation().getScreen(),point);
//                    Thread.sleep(8000);
//                    //IPEngineService.getMatch(this.getPointById(pointId),Point.BLOCKPOINT, (View) result.get(1));
//                    logger.info("Completed unblocking the reverse state of the point "+pointId);
//
//                    this.closePointWindow(pointId);
//
//                }
//                if (o.equals(PointOperations.NORMALISE)) {
//                    Point point =this.getPointById(pointId);
//                    //Test normal point
//                    this.normalisePointById(pointId);
//
//                    //IPEngineService.getMatch(this.getPointById(pointId),this.getPointById(pointId).getImagePathNormalState(), (View) result.get(1));
//                    logger.info("Completed reversing the point "+pointId);
//
//                    //match.click();
//                    this.blockState((Screen) point.getLocation().getScreen(),this.getPointById(pointId));
//                    Thread.sleep(8000);
//                    //IPEngineService.getMatch(this.getPointById(pointId),Point.UNBLOCKPOINT, (View) result.get(1));
//                    logger.info("Completed blocking the reverse state of the point "+pointId);
//
//                    this.unblockState((Screen) point.getLocation().getScreen(),this.getPointById(pointId));
//                    Thread.sleep(8000);
//                    //IPEngineService.getMatch(this.getPointById(pointId),Point.BLOCKPOINT, (View) result.get(1));
//                    logger.info("Completed unblocking the reverse state of the point "+pointId);
//
//                    this.closePointWindow(pointId);
//
//                }
//            }catch (FindFailed e){
//                logger.info(pointId+": "+e.getMessage()+". The object was not located on the screen");
//            }catch (FileNotFoundException e){
//                logger.info(pointId+": "+e.getMessage()+". This object is not configured in the system.");
//            }catch (ObjectStateException | ParserConfigurationException | NetworkException | SAXException e){
//                logger.info(pointId+": "+e.getMessage()+". Communication for this object might have been broken");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } finally {
//                logger.info("Test case:"+pointId+" failed.");
//            }
//        }
//    }

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

         if(rsmMessage.isCentered()){
                logger.info("The point is in center state");
                return point.getImagePathCenterState();
        }

        else{
            throw new InvalidOperationException("The passed operation is invalid");
        }
    }
}
