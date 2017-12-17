package team.itis.ivm.data;

import java.util.ArrayList;

public class Project {

    public String name;
    private ArrayList<Content> ViewItems = new ArrayList<>();

    public void setViewItems(ArrayList<Content> viewItems) {
        ViewItems = viewItems;
    }

    public ArrayList<Content> getViewItems() {
        return ViewItems;
    }


    public Project(String name) {
    }
}
