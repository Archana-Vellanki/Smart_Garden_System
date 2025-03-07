package com.example.ooad_project.Events;

public class ParasiteDamageEvent {

    int row;
    int col;
    int damage;

    public ParasiteDamageEvent(int row, int col, int damage) {
        this.row = row;
        this.col = col;
        this.damage = damage;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getDamage() {
        return damage;
    }


}
