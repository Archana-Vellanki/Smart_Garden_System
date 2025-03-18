

package com.example.ooad_project.Events;

public class PesticideApplicationEvent {
    private final int row;
    private final int col;
    private final String pesticideType;

    public PesticideApplicationEvent(int row, int col, String pesticideType) {
        this.row = row;
        this.col = col;
        this.pesticideType = pesticideType;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getPesticideType() {
        return pesticideType;
    }
}

