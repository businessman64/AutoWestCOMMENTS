package Service;

import exceptions.DescriptionNotFoundException;
import exceptions.NetworkException;
import model.*;
import org.xml.sax.SAXException;
import util.XMLParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class RailwayStateManagerService {

    private static RailwayStateManagerService railwayStateManagerService;
    private static final String URL = "http://RailwayStateManager.DNG:8080/xml/";
    private RailwayStateManagerService(){
    }
    public static RailwayStateManagerService getInstance(){
        if(railwayStateManagerService == null){
            railwayStateManagerService = new RailwayStateManagerService();
        }
        return railwayStateManagerService;
    }
    public RsmMessage getState(Track track) throws ParserConfigurationException, SAXException, NetworkException {
        //String response = util.CmdLine.executeCurlics(this.URL + "track/" + track.getId());
        try{
            String response = util.HttpRequest.sendGetRequest(this.URL + "track/" + track.getId());
            return XMLParser.parseRailwayStateManagerMessage(response);
            // Below I have caught the IOException because we need to try querying the RSM again with P letter.
        }catch (IOException ne){
            try{
                String trackName[] = track.getId().split("/T");
                String newTrackName = "P"+trackName[1];
                String response = util.HttpRequest.sendGetRequest(this.URL + "track/" + trackName[0]+"/"+newTrackName);
                return XMLParser.parseRailwayStateManagerMessage(response);
                // Below I have caught the IOException because we have to give the actuall reason of the failure to user.
            }catch (IOException nee){
                throw new NetworkException("Could not connect to the Railway state manager"+nee.getMessage());
            }
        }
    }
    public RsmMessage getPointState(Point point) throws IOException, ParserConfigurationException, SAXException, NetworkException {
        //String response = util.CmdLine.executeCurlics(this.URL + "track/" + track.getId());
        String response = util.HttpRequest.sendGetRequest(this.URL + "track/" + point.getId());
        return XMLParser.parseRailwayStateManagerMessage(response);
    }
    public SignalRsmMessage getSignalState(Signal signal) throws ParserConfigurationException, SAXException, NetworkException {
        //String response = util.CmdLine.executeCurlics(this.URL + "track/" + track.getId());
        try{
            String response = util.HttpRequest.sendGetRequest(this.URL + "signal/" + signal.getId());
            return XMLParser.parseRailwayStateManagerMessageForSignal(response);
        }catch (IOException nee){
                throw new NetworkException("Could not connect to the Railway state manager"+nee.getMessage());
            }
        }
    }
