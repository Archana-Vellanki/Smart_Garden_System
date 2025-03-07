package com.example.ooad_project.SubSystems;

import com.example.ooad_project.Events.*;
import com.example.ooad_project.GardenGrid;
import com.example.ooad_project.Plant.Plant;
import com.example.ooad_project.ThreadUtils.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TemperatureSystem implements Runnable{
    private int currentDay;
    private final GardenGrid gardenGrid;
    private static final Logger logger = LogManager.getLogger("TemperatureSystemLogger");


    public TemperatureSystem() {
//        Subscribe to the TemperatureEvent
//        Published from GardenSimulationAPI
        this.gardenGrid = GardenGrid.getInstance();
        logger.info("Temperature System Initialized");
        EventBus.subscribe("DayChangeEvent", event -> handleDayChangeEvent((DayChangeEvent) event));
        EventBus.subscribe("TemperatureEvent", event -> handleTemperatureEvent((TemperatureEvent) event));
    }

    private void handleDayChangeEvent(DayChangeEvent event) {
        this.currentDay = event.getDay(); // Update currentDay
    }

    private void handleTemperatureEvent(TemperatureEvent event) {
        int currentTemperature = event.getAmount();
        logger.info("Day: " + currentDay + " API called temperature set to: {}", currentTemperature);

        for (int i = 0; i < gardenGrid.getNumRows(); i++) {
            for (int j = 0; j < gardenGrid.getNumCols(); j++) {
                Plant plant = gardenGrid.getPlant(i, j);
                if (plant != null) {
                    int tempDiff = currentTemperature - plant.getTemperatureRequirement();
                    if (tempDiff > 0) {
                        EventBus.publish("Day: " + currentDay + " TemperatureCoolEvent", new TemperatureCoolEvent(plant.getRow(), plant.getCol(), Math.abs(tempDiff)));
                        logger.info("Day: " + currentDay + " Temperature system cooled {} at position ({}, {}) by {} degrees F.", plant.getName(), i, j, Math.abs(tempDiff));
                        EventBus.publish("SprinklerEvent", new SprinklerEvent(plant.getRow(), plant.getCol(), tempDiff));
                        logger.info("Day: " + currentDay + " Sprinklers started at position ({}, {}) to cool down the plant.", i, j);
                    } else if (tempDiff < 0) {
                        EventBus.publish("TemperatureHeatEvent", new TemperatureHeatEvent(plant.getRow(), plant.getCol(), Math.abs(tempDiff)));
                        logger.info("Day: " + currentDay + " Temperature system heated {} at position ({}, {}) by {} degrees F.", plant.getName(), i, j, Math.abs(tempDiff));
                    } else {
                        logger.info("Day: " + currentDay + " {} at position ({}, {}) is at optimal temperature.", plant.getName(), i, j);
                    }
                }
            }
        }
    }


    public void run() {

        while (true) {
            try {
                logger.info("Day: " + currentDay + " All Levels are optimal");
                Thread.sleep(20000);
//                System.out.println("Temperature System is running");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
