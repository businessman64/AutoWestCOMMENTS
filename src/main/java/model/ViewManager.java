package model;

import org.sikuli.script.Screen;

public class ViewManager {
    // Static instance
    private static ViewManager instance;

    // Current View
    private View currentView;

    // Private constructor
    private ViewManager() {}

    // Static method to get the singleton instance
    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    // Method to set the current view
    public void setView(String viewName, Screen screen, int coordinateX, int coordinateY) {
        currentView = new View(viewName, screen, coordinateX, coordinateY);
    }

    // Method to get the current view
    public View getCurrentView() {
        return currentView;
    }
}

