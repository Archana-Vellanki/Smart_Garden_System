package com.example.ooad_project.API;

import java.util.Map;

public interface GardenSimulationAPIInterface {
    void initializeGarden();
    Map<String, Object> getPlants();
    void rain(int amount);
    void temperature(int amount);
    void parasite(String name);
    void getState();
}
