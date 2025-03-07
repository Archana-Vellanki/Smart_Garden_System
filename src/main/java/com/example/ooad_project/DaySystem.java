package com.example.ooad_project;

import com.example.ooad_project.Events.DayChangeEvent;
import com.example.ooad_project.Plant.Plant;
import com.example.ooad_project.ThreadUtils.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DaySystem {
    private static DaySystem instance = null;
    private final ScheduledExecutorService scheduler;
    private volatile int currentDay;  // Use volatile to ensure visibility between threads
    private final Logger logger = LogManager.getLogger("DayLogger");
    GardenGrid gardenGrid = GardenGrid.getInstance();

    private DaySystem() {
        logger.info("Day System Initialized");
        scheduler = Executors.newScheduledThreadPool(1);
        currentDay = 0;  // Start at Day 0
        scheduler.scheduleAtFixedRate(this::endOfDayActions, 0, 30, TimeUnit.SECONDS);
    }

    public static synchronized DaySystem getInstance() {
        if (instance == null) {
            instance = new DaySystem();
        }
        return instance;
    }

    public synchronized int getCurrentDay() {
        return currentDay;
    }

    private synchronized void incrementDay() {
        currentDay++;
    }

    private void endOfDayActions() {
        try {
            logger.info("End of Day: {}", getCurrentDay());

            EventBus.publish("SprinklerActivationEvent", null);

            for (int i = 0; i < gardenGrid.getNumRows(); i++) {
                for (int j = 0; j < gardenGrid.getNumCols(); j++) {
                    Plant plant = gardenGrid.getPlant(i, j);
                    if (plant != null) {
                        plant.setIsWatered(false);
                        plant.healPlant(6);
                        plant.setCurrentWater(0);
                    }
                }
            }

            incrementDay();
            EventBus.publish("DayChangeEvent", new DayChangeEvent(getCurrentDay()));
            logger.info("Changed day to: {}", getCurrentDay());

        } catch (Exception e) {
            logger.error("Error during end of day processing: ", e);
        }
    }
}
