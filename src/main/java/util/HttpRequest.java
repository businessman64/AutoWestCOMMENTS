package util;

import exceptions.DescriptionNotFoundException;
import exceptions.NetworkException;
import model.Track;
import org.apache.log4j.Logger;
import org.opencv.dnn.Net;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
    static Logger logger =Logger.getLogger(HttpRequest.class.getName());
    public static String queryRailwayPresenter(Track track) throws IOException, ParserConfigurationException, SAXException, DescriptionNotFoundException, NetworkException {
        String url = "http://RailwayPresenter.DNG/"+track.getId();
        String response = sendGetRequest(url);
        String output = XMLParser.parseGetRequest(response);
        if(output!=null){
            return output;
        }else{
            throw new DescriptionNotFoundException();
        }
    }

    public static String sendGetRequest(String url) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        logger.info("Response code: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder responseBody = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            responseBody.append(inputLine);
        }

        in.close();
        if(responseCode == 200){
            return responseBody.toString();

        }else{
            throw new IOException("There as a problem communicating with the service");
        }
    }

}
