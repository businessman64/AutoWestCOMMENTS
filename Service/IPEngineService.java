package Service;

import config.AppConfig;
import model.Point;
import model.RailwayObject;
import model.Track;
import model.View;
import org.apache.log4j.Logger;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class IPEngineService {

    private static final Logger logger = Logger.getLogger(IPEngineService.class.getName());

    public static ArrayList getMatch(RailwayObject railwayObject, String imagePath, View definedView) throws FindFailed {
        List<View> views;
        if(definedView!=null){
            views = new ArrayList<>();
            views.add(definedView);
        }else{
            views = railwayObject.getInterlocking().getViews();
        }
        Match match;
        ArrayList result = new ArrayList();
        boolean success = false;
        int maxRetries = 1;
        Double similarityScore = Double.parseDouble(AppConfig.getProperty("score.similarityScoreHigh"));
        /*if(railwayObject instanceof Point){
            similarityScore = Double.parseDouble(AppConfig.getProperty("score.similarityScoreMedium"));
        }*/
        for(View view : views){
            int retryCount=0;
            while(!success && retryCount!=maxRetries){
                try{
                    //System.out.print(similarityScore);
                    match = view.getScreen().wait(new Pattern(imagePath).similar(similarityScore),4);
                    success = true;
                    result.add(match);
                    result.add(view);
                    return result;

                }catch (FindFailed ff){
                    retryCount++;
                    similarityScore = Double.parseDouble(AppConfig.getProperty("score.similarityScoreMedium"));
                    if(railwayObject instanceof Point){
                        break;
                    }
                    System.out.println("The track was not found in this view. Trying next one.");
                }
            }
        }
        if(success){
            return result;
        }else{
            throw new FindFailed("The engine failed to locate the object");
        }
    }
    public static ArrayList getMatch(RailwayObject railwayObject, String imagePath, View definedView,int timeout) throws FindFailed {
        List<View> views;
        if(definedView!=null){
            views = new ArrayList<>();
            views.add(definedView);
        }else{
            views = railwayObject.getInterlocking().getViews();
        }
        Match match;
        ArrayList result = new ArrayList();
        int retryCount=1;
        int maxRetries = 1;
        boolean success = false;
        Double similarityScore = Double.parseDouble(AppConfig.getProperty("score.similarityScoreHigh"));
        for(View view : views){
            while(!success && retryCount!=maxRetries){
                try{
                    match = view.getScreen().wait(new Pattern(imagePath).similar(similarityScore),timeout);
                    success = true;
                    result.add(match);
                    result.add(view);
                    return result;

                }catch (FindFailed ff){
                    retryCount++;
                    similarityScore = Double.parseDouble(AppConfig.getProperty("score.similarityScoreMedium"));
                    logger.info("The track was not found in this view. Trying next one.");
                }
            }
        }
        if(success){
            return result;
        }else{
            throw new FindFailed("The engine failed to locate the object");
        }
    }
}
