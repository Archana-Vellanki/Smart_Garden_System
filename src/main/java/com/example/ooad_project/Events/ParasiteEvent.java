package com.example.ooad_project.Events;

import com.example.ooad_project.Parasite.Parasite;

public class ParasiteEvent {

    private final Parasite parasite;

    public ParasiteEvent(Parasite parasite) {
        this.parasite = parasite;
    }

    public Parasite getParasite() {
        return parasite;
    }


}
