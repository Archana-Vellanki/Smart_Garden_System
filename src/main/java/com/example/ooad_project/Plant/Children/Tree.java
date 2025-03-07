package com.example.ooad_project.Plant.Children;

import com.example.ooad_project.Plant.Plant;

import java.util.ArrayList;

public class Tree extends Plant {

    public Tree(String name, int waterRequirement, String imageName, int temperatureRequirement, ArrayList<String> vulnerableTo, int healthSmall, int healthMedium, int healthFull, ArrayList<String> allImages){
        super(name, waterRequirement, imageName, temperatureRequirement,vulnerableTo, healthSmall,  healthMedium,  healthFull, allImages);
    }

}
