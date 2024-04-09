package model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Interlocking {
    String name;
    List<View> views;

    public List<View> getViews() {
        return views;
    }

    public void setViews(List<View> views) {
        this.views = views;
    }

    public Interlocking(String name, List<View> views){
        this.name = name;
        this.views = views;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void add(View view){
        views.add(view);
    }
    public void remove(View view){
        views.remove(view);
    }
}
