package com.example.ooad_project.Events;

    public class SprinklerEvent {


    int waterNeeded;

    int row;
    int col;

    public SprinklerEvent(int row, int col, int waterNeeded) {
        this.row = row;
        this.col = col;
        this.waterNeeded = waterNeeded;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getWaterNeeded() {
        return waterNeeded;
    }


}
