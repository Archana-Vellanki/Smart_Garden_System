package com.example.ooad_project.Parasite.Children;

import com.example.ooad_project.Events.ParasiteDamageEvent;
import com.example.ooad_project.Parasite.Parasite;
import com.example.ooad_project.Plant.Plant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Random;

// Slugs class with a 25% miss chance
public class Slugs extends Parasite {
    private static final double MISS_CHANCE = 0.25;  // 25% chance to miss
    private Random random = new Random();
    private static final Logger logger = LogManager.getLogger("PesticideSystemLogger");

    public Slugs(String name, int damage, String imageName, ArrayList<String> affectedPlants) {
        super(name, damage, imageName, affectedPlants);
    }

    @Override
    public void affectPlant(Plant plant) {
        if (random.nextDouble() >= MISS_CHANCE) {
            // If not missed, apply the damage
            int oldHealth = plant.getCurrentHealth();
            int newHealth = Math.max(0, plant.getCurrentHealth() - this.getDamage());
            super.publishDamageEvent(new ParasiteDamageEvent(plant.getRow(),plant.getCol(), this.getDamage()));

            plant.setCurrentHealth(newHealth);
            logger.info("Slug has successfully damaged the plant {} at position ({}, {}). Old health: {}. New health: {}",
                    plant.getName(), plant.getRow(), plant.getCol(), oldHealth, newHealth);

        } else {
            // If missed, do nothing
            logger.info("Slug attempted to damage the plant {} at position ({}, {}) but missed.",
                    plant.getName(), plant.getRow(), plant.getCol());
        }
    }
}