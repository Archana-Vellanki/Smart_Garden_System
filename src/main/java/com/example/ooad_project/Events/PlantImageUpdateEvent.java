package com.example.ooad_project.Events;

import com.example.ooad_project.Plant.Plant;

public class PlantImageUpdateEvent {

    private final Plant plant;

    public PlantImageUpdateEvent(Plant plant) {
        this.plant = plant;
    }

    public Plant getPlant() {
        return plant;
    }


}
