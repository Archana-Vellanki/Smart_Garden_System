package com.example.ooad_project.Parasite;
import com.example.ooad_project.Parasite.Children.Rat;

import com.example.ooad_project.Parasite.Children.Aphids;
import com.example.ooad_project.Parasite.Children.Crow;
import com.example.ooad_project.Parasite.Children.Locust;
import com.example.ooad_project.Parasite.Children.Slugs;


import java.util.ArrayList;

public class ParasiteFactory {

    public static Parasite createParasite(String name, int damage, String imageName, ArrayList<String> affectedPlants) {
        switch (name.toLowerCase()) {
            case "rat":
                return new Rat(name, damage, imageName, affectedPlants);
            case "crow":
                return new Crow(name, damage, imageName, affectedPlants);
            case "locust":
                return new Locust(name, damage, imageName, affectedPlants);
            case "aphids":
                return new Aphids(name, damage, imageName, affectedPlants);
            case "slugs":
                return new Slugs(name, damage, imageName, affectedPlants);
            default:
                throw new IllegalArgumentException("Unknown parasite type: " + name);
        }
    }
}
