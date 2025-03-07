package com.example.ooad_project.Plant;

import com.example.ooad_project.Plant.Children.Flower;
import com.example.ooad_project.Plant.Children.Tree;
import com.example.ooad_project.Plant.Children.Vegetable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


// Plant Manager will get plant data from the JSON file and store it in lists of flowers, trees, and vegetables
public class PlantManager {
    private static PlantManager instance;
    private List<Flower> flowers;
    private List<Tree> trees;
    private List<Vegetable> vegetables;

    private PlantManager() {
        flowers = new ArrayList<>();
        trees = new ArrayList<>();
        vegetables = new ArrayList<>();
        loadPlantsData();
    }

    public static synchronized PlantManager getInstance() {
        if (instance == null) {
            instance = new PlantManager();
        }
        return instance;
    }


    public Plant getPlantByName(String name) {
        // Check in flowers
        for (Flower flower : flowers) {
            if (flower.getName().equals(name)) {
                // Create a new Flower instance with the same properties
                return new Flower(flower.getName(), flower.getWaterRequirement(), flower.getCurrentImage(),
                        flower.getTemperatureRequirement(), new ArrayList<>(flower.getVulnerableTo()),
                        flower.getHealthSmall(), flower.getHealthMedium(), flower.getHealthFull(), flower.getAllImages());
            }
        }
        // Check in trees
        for (Tree tree : trees) {
            if (tree.getName().equals(name)) {
                // Create a new Tree instance with the same properties
                return new Tree(tree.getName(), tree.getWaterRequirement(), tree.getCurrentImage(),
                        tree.getTemperatureRequirement(), new ArrayList<>(tree.getVulnerableTo()),
                        tree.getHealthSmall(), tree.getHealthMedium(), tree.getHealthFull(), tree.getAllImages());
            }
        }
        // Check in vegetables
        for (Vegetable vegetable : vegetables) {
            if (vegetable.getName().equals(name)) {
                // Create a new Vegetable instance with the same properties
                return new Vegetable(vegetable.getName(), vegetable.getWaterRequirement(), vegetable.getCurrentImage(),
                        vegetable.getTemperatureRequirement(), new ArrayList<>(vegetable.getVulnerableTo()),
                        vegetable.getHealthSmall(), vegetable.getHealthMedium(), vegetable.getHealthFull(),vegetable.getAllImages() );
            }
        }
        return null; // Or throw an exception if preferred
    }



    private void loadPlantsData() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("plants.json")));
            JSONObject jsonObject = new JSONObject(content);

            loadFlowers(jsonObject.getJSONArray("flowers"));
            loadTrees(jsonObject.getJSONArray("trees"));
            loadVegetables(jsonObject.getJSONArray("vegetables"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFlowers(JSONArray flowerData) {
        for (int i = 0; i < flowerData.length(); i++) {
            JSONObject flower = flowerData.getJSONObject(i);
            ArrayList<String> vulnerableTo = new ArrayList<>();
            ArrayList<String> allImages = new ArrayList<>();
            JSONArray vulnerabilities = flower.getJSONArray("vulnerableTo");
            JSONArray images = flower.getJSONArray("allImages");

            for (int j = 0; j < vulnerabilities.length(); j++) {
                vulnerableTo.add(vulnerabilities.getString(j));
            }
            for (int j = 0; j < images.length(); j++) {
                allImages.add(images.getString(j));
            }

            flowers.add(new Flower(
                    flower.getString("name"),
                    flower.getInt("waterRequirement"),
                    flower.getString("currentImage"),
                    flower.getInt("temperatureRequirement"),
                    vulnerableTo,
                    flower.getInt("healthSmall"),
                    flower.getInt("healthMedium"),
                    flower.getInt("healthFull"),
                    allImages  // Pass the list of all images
            ));
        }
    }

    private void loadTrees(JSONArray treeData) {
        for (int i = 0; i < treeData.length(); i++) {
            JSONObject tree = treeData.getJSONObject(i);
            ArrayList<String> vulnerableTo = new ArrayList<>();
            ArrayList<String> allImages = new ArrayList<>();
            JSONArray vulnerabilities = tree.getJSONArray("vulnerableTo");
            JSONArray images = tree.getJSONArray("allImages");

            for (int j = 0; j < vulnerabilities.length(); j++) {
                vulnerableTo.add(vulnerabilities.getString(j));
            }
            for (int j = 0; j < images.length(); j++) {
                allImages.add(images.getString(j));
            }

            trees.add(new Tree(
                    tree.getString("name"),
                    tree.getInt("waterRequirement"),
                    tree.getString("currentImage"),
                    tree.getInt("temperatureRequirement"),
                    vulnerableTo,
                    tree.getInt("healthSmall"),
                    tree.getInt("healthMedium"),
                    tree.getInt("healthFull"),
                    allImages  // Pass the list of all images
            ));
        }
    }

    private void loadVegetables(JSONArray vegetableData) {
        for (int i = 0; i < vegetableData.length(); i++) {
            JSONObject vegetable = vegetableData.getJSONObject(i);
            ArrayList<String> vulnerableTo = new ArrayList<>();
            ArrayList<String> allImages = new ArrayList<>();
            JSONArray vulnerabilities = vegetable.getJSONArray("vulnerableTo");
            JSONArray images = vegetable.getJSONArray("allImages");

            for (int j = 0; j < vulnerabilities.length(); j++) {
                vulnerableTo.add(vulnerabilities.getString(j));
            }
            for (int j = 0; j < images.length(); j++) {
                allImages.add(images.getString(j));
            }

            vegetables.add(new Vegetable(
                    vegetable.getString("name"),
                    vegetable.getInt("waterRequirement"),
                    vegetable.getString("currentImage"),
                    vegetable.getInt("temperatureRequirement"),
                    vulnerableTo,
                    vegetable.getInt("healthSmall"),
                    vegetable.getInt("healthMedium"),
                    vegetable.getInt("healthFull"),
                    allImages  // Pass the list of all images
            ));
        }
    }


    public List<Flower> getFlowers() {
        return flowers;
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public List<Vegetable> getVegetables() {
        return vegetables;
    }
}
