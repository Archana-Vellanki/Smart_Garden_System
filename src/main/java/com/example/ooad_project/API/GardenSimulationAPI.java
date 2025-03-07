package com.example.ooad_project.API;

import com.example.ooad_project.DaySystem;
import com.example.ooad_project.Events.ParasiteEvent;
import com.example.ooad_project.Events.RainEvent;
import com.example.ooad_project.Events.TemperatureEvent;
import com.example.ooad_project.GardenGrid;
import com.example.ooad_project.Parasite.Parasite;
import com.example.ooad_project.Parasite.ParasiteManager;
import com.example.ooad_project.Plant.Plant;
import com.example.ooad_project.Plant.PlantManager;
import com.example.ooad_project.ThreadUtils.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GardenSimulationAPI implements GardenSimulationAPIInterface {
    private static final Logger logger = LogManager.getLogger("GardenSimulationAPILogger");
    private ParasiteManager parasiteManager = ParasiteManager.getInstance();

    @Override
    public void initializeGarden() {
        logger.info("Initializing Garden");
        GardenGrid gardenGrid = GardenGrid.getInstance();
        PlantManager plantManager = PlantManager.getInstance();

        EventBus.publish("InitializeGarden", null);


//        }
    }


    @Override
    public Map<String, Object> getPlants() {
        try {
            logger.info("API called to get plant information");

            List<String> plantNames = new ArrayList<>();
            List<Integer> waterRequirements = new ArrayList<>();
            List<List<String>> parasiteLists = new ArrayList<>();

            for (Plant plant : GardenGrid.getInstance().getPlants()) {
                plantNames.add(plant.getName());
                waterRequirements.add(plant.getWaterRequirement());
                parasiteLists.add(plant.getVulnerableTo());
            }

            Map<String, Object> response = Map.of(
                    "plants", plantNames,
                    "waterRequirement", waterRequirements,
                    "parasites", parasiteLists
            );

            System.out.println("\n\nResponse: from getPlants\n\n");
            System.out.println(response);

            return response;
        } catch (Exception e) {
            logger.error("Error occurred while retrieving plant information", e);
            return null;
        }
    }

    @Override
    public void rain(int amount) {
        logger.info("API called rain with amount: {}", amount);
        EventBus.publish("RainEvent", new RainEvent(amount));
    }

    @Override
    public void temperature(int amount) {
        logger.info("API called temperature set to: {}", amount);
        EventBus.publish("TemperatureEvent", new TemperatureEvent(amount));
    }

    @Override
    public void parasite(String name) {
        logger.info("API called to handle parasite: {}", name);
        Parasite parasite = parasiteManager.getParasiteByName(name);
        if(parasite == null) {
            logger.info("API - Parasite with name {} not found", name);
            return;
        }
        EventBus.publish("ParasiteEvent", new ParasiteEvent(parasite));

    }

    @Override
    public void getState() {
        logger.info("Day: " + DaySystem.getInstance().getCurrentDay() + "API called to get current state of the garden.");
        StringBuilder stateBuilder = new StringBuilder();
        stateBuilder.append(String.format("Current Garden State as of Day %d:\n", DaySystem.getInstance().getCurrentDay()));

        GardenGrid gardenGrid = GardenGrid.getInstance();
        ArrayList<Plant> plants = gardenGrid.getPlants();

        if (plants.isEmpty()) {
            stateBuilder.append("No plants are currently in the garden.\n");
        } else {
            for (Plant plant : plants) {
                stateBuilder.append(String.format("\nPlant Name: %s (Position: Row %d, Col %d)\n", plant.getName(), plant.getRow(), plant.getCol()));
                stateBuilder.append(String.format("  - Current Health: %d/%d\n", plant.getCurrentHealth(), plant.getHealthFull()));
                stateBuilder.append(String.format("  - Growth Stage: %s\n", plant.getGrowthStageDescription()));
                stateBuilder.append(String.format("  - Water Status: %s (Current Water: %d, Requirement: %d)\n", plant.getIsWatered() ? "Watered" : "Needs Water", plant.getCurrentWater(), plant.getWaterRequirement()));
                stateBuilder.append(String.format("  - Temperature Requirement: %d degrees\n", plant.getTemperatureRequirement()));
                stateBuilder.append(String.format("  - Current Image: %s\n", plant.getCurrentImage()));
                stateBuilder.append(String.format("  - Vulnerable to: %s\n", String.join(", ", plant.getVulnerableTo())));
            }
        }

        logger.info(stateBuilder.toString());
    }



}
