package Service;

import model.Interlocking;
import model.View;
import org.sikuli.script.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewService {
    private static ViewService viewService;
    private ScreenService screenService;
    private Map<String,View> views;
    private ViewService(){
        views = new HashMap<>();
        screenService = ScreenService.getInstance();
        initialiseViews();
    }

    private void initialiseViews() {
        int count = screenService.getNumberOfScreens();
        if(count>5){
//            views.put("WESTALL",new View("WESTALL",screenService.getScreens().get(0)));
//            views.put("SPRINGVALE",new View("SPRINGVALE",screenService.getScreens().get(5)));
//            views.put("DANDENONG",new View("DANDENONG",screenService.getScreens().get(2)));
//            views.put("PAKENHAM",new View("PAKENHAM",screenService.getScreens().get(1)));
//            views.put("CRANBOURNE",new View("CRANBOURNE",screenService.getScreens().get(3)));
//            views.put("PEDS", new View("PEDS",screenService.getScreens().get(4)));
        }else{
//            views.put("WESTALL",new View("WESTALL",screenService.getScreens().get(0)));
//            views.put("SPRINGVALE",new View("SPRINGVALE",screenService.getScreens().get(0)));
//            views.put("DANDENONG",new View("DANDENONG",screenService.getScreens().get(0)));
//            views.put("PAKENHAM",new View("PAKENHAM",screenService.getScreens().get(0)));
//            views.put("CRANBOURNE",new View("CRANBOURNE",screenService.getScreens().get(0)));
//            views.put("PEDS", new View("PEDS",screenService.getScreens().get(0)));
        }
    }

    public static ViewService getInstance(){

        if(viewService == null){
            viewService =  new ViewService();
            return viewService;
        }else{
            return viewService;
        }
    }

    public List<View> getViewByNames(String... viewNames){

        ArrayList<View> views = new ArrayList();
        for(String viewName:viewNames){
            views.add(this.views.get(viewName));
        }
        return views;
    }
    public View getViewByName(String viewName){
        return this.views.get(viewName);
    }
    public void changeScreenByView(String name, int screenNumber){
        View view = this.getViewByName(name);
        //System.out.println(view.getName()+" "+view.getScreen().getID()+" "+ + screenNumber);
        view.setScreen(screenService.getScreens().get(screenNumber));
        System.out.println(view.getScreen().getID());
    }
    public Integer getScreenNumberByViewName(String viewName){
        return this.getViewByName(viewName).getScreen().getID();
    }
}
