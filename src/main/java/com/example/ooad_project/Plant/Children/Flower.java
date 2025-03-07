package com.example.ooad_project.Plant.Children;


import com.example.ooad_project.Plant.Plant;

import java.util.ArrayList;

/**
 * This class represents a flower in the garden.
 * It is a subclass of the Plant class.
 * It will be extended by all the flowers in the garden.
 */
public class Flower extends Plant {

    public Flower(String name, int waterRequirement, String imageName, int temperatureRequirement, ArrayList<String> vulnerableTo, int healthSmall, int healthMedium, int healthFull, ArrayList<String> allImages){
        super(name, waterRequirement, imageName, temperatureRequirement,vulnerableTo, healthSmall,  healthMedium,  healthFull, allImages);
    }

}
