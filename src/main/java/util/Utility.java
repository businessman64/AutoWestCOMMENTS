package util;

import exceptions.ObjectStateException;
import model.RsmMessage;
import model.SignalRsmMessage;

import java.io.File;
import java.io.FileNotFoundException;

public class Utility {
    public static boolean checkIfFileExists(String image) throws FileNotFoundException {
        File file = new File(image);
        if (file.exists()){
            return true;
        }
        return false;
    }
    public static void checkTrackFailures(RsmMessage rsmMessage) throws ObjectStateException {

        checkFailures(rsmMessage);
        if (rsmMessage.isVitalBlocked()){
            throw new ObjectStateException("The track is already blocked.");
        }else if (rsmMessage.isDisregarded()){
            throw new ObjectStateException("The track is disregarded and cannot be blocked");
        }

    }
    public static void checkFailures(RsmMessage rsmMessage) throws ObjectStateException {

        if(rsmMessage.isCommsFailed()){
            throw new ObjectStateException("The communication with the track object is broken");
        }

    }
}
