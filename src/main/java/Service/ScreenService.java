package Service;

import controller.TrackController;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.sikuli.script.Mouse;
import org.sikuli.script.Button;
import java.util.logging.Logger;


import java.util.ArrayList;
import java.util.List;

public class ScreenService {
    public static ScreenService screenService;

    private static final Logger logger = Logger.getLogger(ScreenService.class.getName());

    private ArrayList<Screen> screens;
    private ScreenService(){
        initialiseScreens();
    }

    private void initialiseScreens() {
        screens = new ArrayList<Screen>();
        for(int i=0;i< Screen.getNumberScreens();i++){
                screens.add(new Screen(i));
        }
    }

    public static ScreenService getInstance(){

        if(screenService == null){
            screenService = new ScreenService();
            return screenService;
        }else{
            return screenService;
        }
    }
    public List<Screen> getScreens(){
            return screens;
    }
    public int getNumberOfScreens(){
        return Screen.getNumberScreens();
    }

    public void findScreen(Screen screen,Location screenCoordinates) {
          float x = screenCoordinates.getX();
          float y = screenCoordinates.getY();

          logger.info("X: "+x+" Y:"+y);
//        screen.mouseMove(Mouse.at().getX(),Mouse.at().getY());
//        screen.mouseDown(Button.LEFT);
//        screen.mouseMove(screenCoordinates.getX(), screenCoordinates.getY());
//        screen.mouseUp(Button.LEFT);
    }
}
