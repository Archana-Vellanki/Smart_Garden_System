package com.example.ooad_project.Events;

public class CoolTemperatureEvent {

    int tempDiff;
    int row;
    int col;


    public CoolTemperatureEvent(int row, int col, int tempDiff) {

        this.row = row;
        this.col = col;
        this.tempDiff = tempDiff;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getTempDiff() {
        return tempDiff;
    }


}
