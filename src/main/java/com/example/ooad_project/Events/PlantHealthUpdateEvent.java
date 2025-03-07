package com.example.ooad_project.Events;

public class PlantHealthUpdateEvent {

        int row;
        int col;
        int oldHealth;
        int newHealth;

        public PlantHealthUpdateEvent(int row, int col, int oldHealth, int newHealth) {
            this.row = row;
            this.col = col;
            this.oldHealth = oldHealth;
            this.newHealth = newHealth;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getOldHealth() {
            return oldHealth;
        }

        public int getNewHealth() {
            return newHealth;
        }

}
