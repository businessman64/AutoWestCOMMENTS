package Service;

import jnr.ffi.annotations.In;
import model.Interlocking;
import model.View;
import org.sikuli.script.Screen;

import java.util.*;

public class InterlockingService {
    public static InterlockingService interlockingService;

    public Map<String,Interlocking> interlockingMap;

    private ViewService viewService;
    private InterlockingService(){
        viewService = ViewService.getInstance();
        interlockingMap = new HashMap<>();
        initialiseInterlockings();
    }

    private void initialiseInterlockings() {

        interlockingMap.put("OAK",new Interlocking("OAK", this.viewService.getViewByNames("WESTALL")));
        interlockingMap.put("HUN",new Interlocking("HUN", this.viewService.getViewByNames("WESTALL")));
        interlockingMap.put("CLA",new Interlocking("CLA", this.viewService.getViewByNames("WESTALL")));
        interlockingMap.put("WTL",new Interlocking("WTL", this.viewService.getViewByNames("SPRINGVALE","WESTALL")));
        interlockingMap.put("YMN",new Interlocking("YMN",  this.viewService.getViewByNames("SPRINGVALE")));
        interlockingMap.put("DNG",new Interlocking("DNG", this.viewService.getViewByNames("DANDENONG","PAKENHAM","SPRINGVALE")));
        interlockingMap.put("BEW",new Interlocking("BEW", this.viewService.getViewByNames("CRANBOURNE","DANDENONG")));
        interlockingMap.put("OFC",new Interlocking("OFC", this.viewService.getViewByNames("DANDENONG")));
        interlockingMap.put("PKM",new Interlocking("PKM", this.viewService.getViewByNames("PAKENHAM","CRANBOURNE","DANDENONG")));
        interlockingMap.put("HLM",new Interlocking("HLM",  this.viewService.getViewByNames("PAKENHAM","CRANBOURNE")));
        interlockingMap.put("LBK",new Interlocking("LBK", this.viewService.getViewByNames("PAKENHAM","CRANBOURNE")));
        interlockingMap.put("CBE",new Interlocking("CBE", this.viewService.getViewByNames("PAKENHAM","CRANBOURNE")));
        interlockingMap.put("MPK",new Interlocking("MPK",  this.viewService.getViewByNames("CRANBOURNE")));
        interlockingMap.put("PKE",new Interlocking("PKE", this.viewService.getViewByNames("CRANBOURNE","PEDS")));
        interlockingMap.put("BYP",new Interlocking("BYP",  this.viewService.getViewByNames("CRANBOURNE","PEDS")));
        interlockingMap.put("LWY",new Interlocking("LWY", this.viewService.getViewByNames("PEDS")));
    }


    public static InterlockingService getInstance(){
        if(interlockingService == null){
            interlockingService = new InterlockingService();
            return interlockingService;
        }else{
            return interlockingService;
        }
    }
    public Interlocking getInterlockingByName(String name){
        return this.interlockingMap.get(name);
    }
}
