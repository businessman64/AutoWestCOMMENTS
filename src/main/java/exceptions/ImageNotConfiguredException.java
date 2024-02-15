package exceptions;

import org.sikuli.script.Image;

public class ImageNotConfiguredException extends Exception{
    public ImageNotConfiguredException(String msg){
        super(msg);
    }
}
