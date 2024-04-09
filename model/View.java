package model;

import org.sikuli.script.Screen;

public class View {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;
    Screen screen;

    int coordinateX;
    int coordinateY;



    public int getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }


    //This class can have interlocking objects to be able to
    //find interlocking directly from the view.
    //ATM, I cannot think of any use case so commenting it.
    //Interlocking interlocking[];
    public View(String name, Screen s,int coordinateX, int coordinateY ){
        this.name = name;
        this.screen = s;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;

    }
    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }



}
