package com.example.ooad_project.SubSystems;

import com.example.ooad_project.Events.DayChangeEvent;
import com.example.ooad_project.Events.DisplayParasiteEvent;
import com.example.ooad_project.Events.ParasiteEvent;
import com.example.ooad_project.GardenGrid;
import com.example.ooad_project.Parasite.Parasite;
import com.example.ooad_project.Plant.Plant;
import com.example.ooad_project.Plant.PlantManager;
import com.example.ooad_project.ThreadUtils.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PesticideSystem implements Runnable{
    private int currentDay;
    private final GardenGrid gardenGrid;
    private static final Logger logger = LogManager.getLogger("PesticideSystemLogger");

    public PesticideSystem() {
        this.gardenGrid = GardenGrid.getInstance();
//        System.out.println("Pesticide System Initialized");
        logger.info("Pesticide System Initialized");

        EventBus.subscribe("DayChangeEvent", event -> handleDayChangeEvent((DayChangeEvent) event));
//        Subscribe to the ParasiteEvent that will be published by the GardenSimulationAPI
        EventBus.subscribe("ParasiteEvent", event -> handlePesticideEvent((ParasiteEvent) event));
    }

    private void handleDayChangeEvent(DayChangeEvent event) {
        this.currentDay = event.getDay(); // Update currentDay
    }

    private void handlePesticideEvent(ParasiteEvent event) {
        Parasite parasite = event.getParasite();
//        System.out.println("Parasite attack on plant: " + parasite.getName() + " with damage: " + parasite.getDamage());

        // Loop through all the plants in the garden grid
        for (int i = 0; i < gardenGrid.getNumRows(); i++) {
            for (int j = 0; j < gardenGrid.getNumCols(); j++) {
                Plant plant = gardenGrid.getPlant(i, j);
                if (plant != null && parasite.getAffectedPlants().contains(plant.getName())) {

//                    Publish an event to display the parasite on the plant
//                    This is for the JavaFX GUI
                    EventBus.publish("Day: " + currentDay + " DisplayParasiteEvent", new DisplayParasiteEvent(parasite, i, j));

//                    Apply the parasite to the plant
                    parasite.affectPlant(plant);
                    logger.info("Day: " + currentDay + " Pesticide system applied {} to {} at position ({}, {})", parasite.getName(), plant.getName(), i, j);
//                    Heal the plant by half the damage of the parasite
                    plant.healPlant(parasite.getDamage()/2);
                }
            }
        }
    }


//    When handling parasite attacks we will sleep the thread for 5 seconds
//    so that we can see the effects of the parasite attack on the plants in JavaFX


    public void run() {
        while (true) {
            try {

//          Will just loop saying that plants are safe or sm

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}
