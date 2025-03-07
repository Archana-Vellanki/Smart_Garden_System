package com.example.ooad_project.Events;

public class TemperatureEvent {
        private final int amount;
        public TemperatureEvent(int amount) {
            this.amount = amount;
        }
        public int getAmount() {
            return amount;
        }
}