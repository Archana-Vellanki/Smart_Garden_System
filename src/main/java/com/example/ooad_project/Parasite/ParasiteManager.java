package com.example.ooad_project.Parasite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParasiteManager {
    private static ParasiteManager instance;
    private List<Parasite> parasites;

    private ParasiteManager() {
        parasites = new ArrayList<>();
        loadParasitesData();
    }

    public static synchronized ParasiteManager getInstance() {
        if (instance == null) {
            instance = new ParasiteManager();
        }
        return instance;
    }

    public Parasite getParasiteByName(String name) {
        for (Parasite parasite : parasites) {
            if (parasite.getName().equalsIgnoreCase(name)) {
                return parasite;
            }
        }
        return null; // Or throw an exception if preferred
    }

    private void loadParasitesData() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("parasites.json")));
            JSONObject jsonObject = new JSONObject(content);
            JSONArray parasitesArray = jsonObject.getJSONArray("parasites");

            for (int i = 0; i < parasitesArray.length(); i++) {
                JSONObject parasiteJson = parasitesArray.getJSONObject(i);
                JSONArray targetPlantsJsonArray = parasiteJson.getJSONArray("targetPlants");
                ArrayList<String> targetPlants = new ArrayList<>();
                for (int j = 0; j < targetPlantsJsonArray.length(); j++) {
                    targetPlants.add(targetPlantsJsonArray.getString(j));
                }

                // Use the factory to create the parasite instance
                Parasite parasite = ParasiteFactory.createParasite(
                        parasiteJson.getString("name"),
                        parasiteJson.getInt("damage"),
                        parasiteJson.getString("imageName"),
                        targetPlants
                );
                parasites.add(parasite);
            }
        } catch (IOException e) {
//            Should Log the error
//            Parasite Creation Failed
//            Implement Logger later
            e.printStackTrace();
        }
    }



    public List<Parasite> getParasites() {
        return parasites;
    }
}
