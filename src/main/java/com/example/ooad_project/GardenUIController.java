package com.example.ooad_project;

import com.example.ooad_project.API.GardenSimulationAPI;
import com.example.ooad_project.Events.*;
import com.example.ooad_project.Parasite.Parasite;
import com.example.ooad_project.Parasite.ParasiteManager;
import com.example.ooad_project.Plant.Children.Flower;
import com.example.ooad_project.Plant.Plant;
import com.example.ooad_project.Plant.Children.Tree;
import com.example.ooad_project.Plant.Children.Vegetable;
import com.example.ooad_project.Plant.PlantManager;
import com.example.ooad_project.ThreadUtils.EventBus;
import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.animation.PauseTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GardenUIController {

    @FXML
    private Button sidButton;

    @FXML
    private Label currentDay;

//    @FXML
//    private MenuButton parasiteMenuButton;

//    @FXML
//    private Button pestTestButton;

    @FXML
    private Label getPLantButton;

    @FXML
    private Label rainStatusLabel;
    @FXML
    private Label temperatureStatusLabel;
    @FXML
    private Label parasiteStatusLabel;

    @FXML
    private GridPane gridPane;
    @FXML
    private MenuButton vegetableMenuButton;

    @FXML
    private MenuButton flowerMenuButton;
    @FXML
    private MenuButton treeMenuButton;

    @FXML
    private AnchorPane anchorPane;

    int flag = 0;
    int logDay = 0;
    DayChangeEvent dayChangeEvent;
    private static class RainDrop {
        double x, y, speed;

        public RainDrop(double x, double y, double speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }

    // Create Canvas for the rain animation
    private Canvas rainCanvas;
    private List<RainDrop> rainDrops;
    private AnimationTimer rainAnimation;



    private final Random random = new Random();
    private GardenGrid gardenGrid;

//    This is the plant manager that will be used to get the plant data
//    from the JSON file, used to populate the menu buttons
    private PlantManager plantManager = PlantManager.getInstance();

//    Same as above but for the parasites
    private ParasiteManager parasiteManager = ParasiteManager.getInstance();

    public GardenUIController() {
        gardenGrid = GardenGrid.getInstance();
    }

//    This is the method that will print the grid
    @FXML
    public void printGrid(){
        gardenGrid.printGrid();
    }

    @FXML
    public void sidButtonPressed() {
        System.out.println("SID Button Pressed");
        plantManager.getVegetables().forEach(flower -> System.out.println(flower.getCurrentImage()));
    }

//    @FXML
//    private TextArea logTextArea;

    private static final Logger logger = LogManager.getLogger("GardenUIControllerLogger");


    @FXML
    public void getPLantButtonPressed() {
        GardenSimulationAPI api = new GardenSimulationAPI();
//        api.getPlants();
        api.getState();
    }


//    This is the UI Logger for the GardenUIController
//    This is used to log events that happen in the UI
    private Logger log4jLogger = LogManager.getLogger("GardenUIControllerLogger");

    @FXML
    public void initialize() {

        initializeLogger();

        showSunnyWeather();

        showOptimalTemperature();

        showNoParasites();

//
//         Load the background image
//         Load the background image
        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/backgroundImage.jpg"));



        // Create an ImageView
        ImageView imageView = new ImageView(backgroundImage);
        imageView.setPreserveRatio(false);

        // Add the ImageView as the first child of the AnchorPane
        anchorPane.getChildren().add(0, imageView);

        // Bind ImageView's size to the AnchorPane's size
        anchorPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            imageView.setFitWidth(newVal.doubleValue());
        });
        anchorPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            imageView.setFitHeight(newVal.doubleValue());
        });

        // Add ColumnConstraints
        for (int col = 0; col < gardenGrid.getNumCols(); col++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPrefWidth(80); // Adjust the width as needed
            gridPane.getColumnConstraints().add(colConst);
        }

        // Add RowConstraints
        for (int row = 0; row < gardenGrid.getNumRows(); row++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(80); // Adjust the height as needed
            gridPane.getRowConstraints().add(rowConst);
        }

        createColoredGrid(gridPane, gardenGrid.getNumRows(),gardenGrid.getNumCols());

        // Initialize the rain canvas and animation
        rainCanvas = new Canvas(1000, 800);
        anchorPane.getChildren().add(rainCanvas); // Add the canvas to the AnchorPane
        rainDrops = new ArrayList<>();

        //gridPane.setStyle("-fx-grid-lines-visible: true; -fx-border-color: brown; -fx-border-width: 2;");

        // Load plants data from JSON file and populate MenuButtons
        loadPlantsData();
//        loadParasitesData();

        log4jLogger.info("GardenUIController initialized");



        EventBus.subscribe("RainEvent", event -> changeRainUI((RainEvent) event));
        EventBus.subscribe("DisplayParasiteEvent", event -> handleDisplayParasiteEvent((DisplayParasiteEvent) event));
        EventBus.subscribe("PlantImageUpdateEvent", event -> handlePlantImageUpdateEvent((PlantImageUpdateEvent) event));
        EventBus.subscribe("DayChangeEvent",event -> handleDayChangeEvent((DayChangeEvent) event));
        EventBus.subscribe("TemperatureEvent", event -> changeTemperatureUI((TemperatureEvent) event));
        EventBus.subscribe("ParasiteEvent", event -> changeParasiteUI((ParasiteEvent) event));

//      Gives you row, col and waterneeded
        EventBus.subscribe("SprinklerEvent", event -> handleSprinklerEvent((SprinklerEvent) event));


//        When plant is cooled by x
        EventBus.subscribe("TemperatureCoolEvent", event -> handleTemperatureCoolEvent((TemperatureCoolEvent) event));


//      When plant is heated by x
        EventBus.subscribe("TemperatureHeatEvent", event -> handleTemperatureHeatEvent((TemperatureHeatEvent) event));


//        When plant is damaged by x
//        Includes -> row, col, damage
        EventBus.subscribe("ParasiteDamageEvent", event -> handleParasiteDamageEvent((ParasiteDamageEvent) event));

        EventBus.subscribe("InitializeGarden", event -> handleInitializeGarden());

//        Event whenever there is change to plants health
        EventBus.subscribe("PlantHealthUpdateEvent", event -> handlePlantHealthUpdateEvent((PlantHealthUpdateEvent) event));

        EventBus.subscribe("PlantDeathUIChangeEvent", event -> handlePlantDeathUIChangeEvent((Plant) event));

    }

    // Start rain animation
    private void startRainAnimation() {


        //logger.info("Srivarsha");
        GraphicsContext gc = rainCanvas.getGraphicsContext2D();

        // Create initial raindrops
        for (int i = 0; i < 100; i++) {
            rainDrops.add(new RainDrop(random.nextDouble() * anchorPane.getWidth(), random.nextDouble() * anchorPane.getHeight(), 2 + random.nextDouble() * 4));
        }

        // Animation timer to update and draw raindrops
        rainAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateRainDrops();
                drawRain(gc);
            }
        };

        rainAnimation.start();

    }

    // Update raindrop positions
    private void updateRainDrops() {
        for (RainDrop drop : rainDrops) {
            drop.y += drop.speed;
            if (drop.y > anchorPane.getHeight()) {
                drop.y = 0;
                drop.x = random.nextDouble() * anchorPane.getWidth();
            }
        }
    }

    // Draw raindrops on the canvas
    private void drawRain(GraphicsContext gc) {
        gc.clearRect(0, 0, anchorPane.getWidth(), anchorPane.getHeight());
        gc.setFill(Color.CYAN);

        for (RainDrop drop : rainDrops) {
            gc.fillOval(drop.x, drop.y, 3, 15); // Raindrop shape (x, y, width, height)
        }
    }

    // Stop rain animation after 5 seconds
    private void stopRainAfterFiveSeconds() {
        PauseTransition pauseRain = new PauseTransition(Duration.seconds(1));
        pauseRain.setOnFinished(event -> {
            // Clear the canvas and stop the animation
            rainAnimation.stop();
            rainCanvas.getGraphicsContext2D().clearRect(0, 0, 1000, 800);
        });
        pauseRain.play();
    }



    public void createColoredGrid(GridPane gridPane, int numRows, int numCols) {
        double cellWidth = 80;  // Width of each cell
        double cellHeight = 80; // Height of each cell

        // Loop through rows and columns to create cells
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                // Create a StackPane for each cell
                StackPane cell = new StackPane();

                // Set preferred size of the cell
                cell.setPrefSize(cellWidth, cellHeight);

                // Set a unique border color for each cell
                Color borderColor = Color.BROWN; // Function to generate random colors
                cell.setBorder(new Border(new BorderStroke(
                        borderColor,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(2) // Border thickness
                )));

                // Add the cell to the GridPane
                gridPane.add(cell, col, row);
            }
        }
    }


    private void handlePlantDeathUIChangeEvent(Plant plant){

    }

    private void handlePlantHealthUpdateEvent(PlantHealthUpdateEvent event){
        logger.info("Day: " + logDay + " Plant health updated at row " + event.getRow() + " and column " + event.getCol() + " from " + event.getOldHealth() + " to " + event.getNewHealth());
//        System.out.println("Plant health updated at row " + event.getRow() + " and column " + event.getCol() + " from " + event.getOldHealth() + " to " + event.getNewHealth());
    }

    private void handleInitializeGarden() {
        // Hard-coded positions for plants as specified in the layout
        Object[][] gardenLayout = {
                {"Oak", 0, 1}, {"Maple", 0, 5}, {"Pine", 0, 6},
                {"Tomato", 1, 6}, {"Carrot", 2, 2}, {"Lettuce", 1, 0},
                {"Sunflower", 3, 1}, {"Rose", 4, 4}, {"Jasmine", 4, 6},
                {"Oak", 5, 6}, {"Tomato", 3, 0}, {"Sunflower", 5, 3}
        };

        Platform.runLater(() -> {
            for (Object[] plantInfo : gardenLayout) {
                String plantType = (String) plantInfo[0];
                int row = (Integer) plantInfo[1];
                int col = (Integer) plantInfo[2];

                Plant plant = plantManager.getPlantByName(plantType);
                if (plant != null) {
                    plant.setRow(row);
                    plant.setCol(col);
                    try {
                        gardenGrid.addPlant(plant, row, col);  // Add plant to the logical grid
                        addPlantToGridUI(plant, row, col);    // Add plant to the UI
                    } catch (Exception e) {
                        logger.error("Failed to place plant: " + plant.getName() + " at (" + row + ", " + col + "): " + e.getMessage());
                    }
                }
            }
        });
    }

    private void addPlantToGridUI(Plant plant, int row, int col) {

        logger.info("Day: " + logDay + " Adding plant to grid: " + plant.getName() + " at row " + row + " and column " + col);

        String imageFile = plant.getCurrentImage();
        Image image = new Image(getClass().getResourceAsStream("/images/" + imageFile));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(40); // Fit the cell size
        imageView.setFitWidth(40);

        StackPane pane = new StackPane(imageView);
        pane.setStyle("-fx-alignment: center;"); // Center the image in the pane
        gridPane.add(pane, col, row); // Add the pane to the grid
        GridPane.setHalignment(pane, HPos.CENTER); // Center align in grid cell
        GridPane.setValignment(pane, VPos.CENTER);
    }

//    Function that is called when the parasite damage event is published
private void handleParasiteDamageEvent(ParasiteDamageEvent event) {

        logger.info("Day: " + logDay + " Displayed plant damaged at row " + event.getRow() + " and column " + event.getCol() + " by " + event.getDamage());

    Platform.runLater(() -> {
        int row = event.getRow();
        int col = event.getCol();
        int damage = event.getDamage();

        // Create a label with the damage value prefixed by a minus sign
        Label damageLabel = new Label("-" + String.valueOf(damage));
        damageLabel.setTextFill(javafx.scene.paint.Color.RED);
        damageLabel.setStyle("-fx-font-weight: bold;");

        // Set the label's position in the grid
        GridPane.setRowIndex(damageLabel, row);
        GridPane.setColumnIndex(damageLabel, col);
        GridPane.setHalignment(damageLabel, HPos.RIGHT);  // Align to right
        GridPane.setValignment(damageLabel, VPos.TOP);    // Align to top
        gridPane.getChildren().add(damageLabel);

        // Remove the label after a pause
        PauseTransition pause = new PauseTransition(Duration.seconds(5)); // Set duration to 5 seconds
        pause.setOnFinished(_ -> gridPane.getChildren().remove(damageLabel));
        pause.play();
    });
}


    private void handleTemperatureHeatEvent(TemperatureHeatEvent event) {

        logger.info("Day: " + logDay + " Displayed plant heated at row " + event.getRow() + " and column " + event.getCol() + " by " + event.getTempDiff());

        Platform.runLater(() -> {
            int row = event.getRow();
            int col = event.getCol();

            String imageName = "heat.png"; // Update this to your heat image name
            Image heatImage = new Image(getClass().getResourceAsStream("/images/" + imageName));
            ImageView heatImageView = new ImageView(heatImage);
            heatImageView.setFitHeight(20);  // Match the cell size in the grid
            heatImageView.setFitWidth(20);

            GridPane.setRowIndex(heatImageView, row);
            GridPane.setColumnIndex(heatImageView, col);
            GridPane.setHalignment(heatImageView, HPos.LEFT);  // Align to left
            GridPane.setValignment(heatImageView, VPos.TOP); // Align to top
            gridPane.getChildren().add(heatImageView);

            PauseTransition pause = new PauseTransition(Duration.seconds(5)); // Set duration to 10 seconds
            pause.setOnFinished(_ -> gridPane.getChildren().remove(heatImageView));
            pause.play();
        });
    }


//    Function that is called when the temperature cool event is published

    private void handleTemperatureCoolEvent(TemperatureCoolEvent event) {


        logger.info("Day: " + currentDay + " Displayed plant cooled at row " + event.getRow() + " and column " + event.getCol() + " by " + event.getTempDiff());

        Platform.runLater(() -> {
            int row = event.getRow();
            int col = event.getCol();

            String imageName = "cool.png"; // Update this to your cool image name
            Image coolImage = new Image(getClass().getResourceAsStream("/images/" + imageName));
            ImageView coolImageView = new ImageView(coolImage);
            coolImageView.setFitHeight(20);  // Match the cell size in the grid
            coolImageView.setFitWidth(20);

            GridPane.setRowIndex(coolImageView, row);
            GridPane.setColumnIndex(coolImageView, col);
            GridPane.setHalignment(coolImageView, HPos.LEFT);  // Align to left
            GridPane.setValignment(coolImageView, VPos.TOP); // Align to top
            gridPane.getChildren().add(coolImageView);

            PauseTransition pause = new PauseTransition(Duration.seconds(5)); // Set duration to 10 seconds
            pause.setOnFinished(_ -> gridPane.getChildren().remove(coolImageView));
            pause.play();
        });
    }
//  Function that is called when the sprinkler event is published
private void handleSprinklerEvent(SprinklerEvent event) {

    logger.info("Day: " + currentDay + " Displayed Sprinkler activated at row " + event.getRow() + " and column " + event.getCol() + " with water amount " + event.getWaterNeeded());

    Platform.runLater(() -> {
        int row = event.getRow();
        int col = event.getCol();

        // Create a group to hold animated droplets
        Group sprinklerAnimationGroup = new Group();

        // Add multiple lines or droplets to simulate water spray
        int numDroplets = 10; // Number of water droplets
        double tileWidth = 40; // Width of the grid cell
        double tileHeight = 40; // Height of the grid cell

        for (int i = 0; i < numDroplets; i++) {
            // Calculate evenly spaced positions within the tile
            double positionX = (i % Math.sqrt(numDroplets)) * (tileWidth / Math.sqrt(numDroplets));
            double positionY = (i / Math.sqrt(numDroplets)) * (tileHeight / Math.sqrt(numDroplets));

            Circle droplet = new Circle();
            droplet.setRadius(3); // Radius of the droplet
            droplet.setFill(Color.BLUE); // Color of the droplet

            // Set starting position for the droplet
            droplet.setCenterX(positionX);
            droplet.setCenterY(positionY);

            // Create a transition for each droplet
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(droplet);
            transition.setDuration(Duration.seconds(0.9)); // Droplet animation duration
            transition.setByX(Math.random() * 20 - 2.5); // Small random spread on X-axis
            transition.setByY(Math.random() * 20);      // Small downward spread on Y-axis
            transition.setCycleCount(1);
            // Add to group and start animation
            sprinklerAnimationGroup.getChildren().add(droplet);
            transition.play();
        }

        // Add animation group to the grid cell
        GridPane.setRowIndex(sprinklerAnimationGroup, row);
        GridPane.setColumnIndex(sprinklerAnimationGroup, col);
        gridPane.getChildren().add(sprinklerAnimationGroup);

        // Remove animation after it completes
        PauseTransition pause = new PauseTransition(Duration.seconds(3)); // Total duration for animation to persist
        pause.setOnFinished(_ -> gridPane.getChildren().remove(sprinklerAnimationGroup));
        pause.play();
    });
}

    private void initializeLogger() {
//        LoggerAppender.setController(this);
    }

//    public void appendLogText(String text) {
//        Platform.runLater(() -> logTextArea.appendText(text + "\n"));
//    }

    public void handleDayChangeEvent(DayChangeEvent event) {

        logger.info("Day: " + logDay + " Day changed to: " + event.getDay());
        dayChangeEvent = event;
        Platform.runLater(() -> {
            logDay = event.getDay();
            currentDay.setText("Day: " + event.getDay());
        });
    }

    private void handlePlantImageUpdateEvent(PlantImageUpdateEvent event) {

        logger.info("Day: " + logDay + " Plant image updated at row " + event.getPlant().getRow() + " and column " + event.getPlant().getCol() + " to " + event.getPlant().getCurrentImage());

//        Be sure to wrap the code in Platform.runLater() to update the UI
//        This is because the event is being handled in a different thread
//        and we need to update the UI in the JavaFX Application Thread
        Platform.runLater(() -> {

            Plant plant = event.getPlant();

            // Calculate the grid position
            int row = plant.getRow();
            int col = plant.getCol();

            // Find the ImageView for the plant in the grid and remove it
            gridPane.getChildren().removeIf(node -> {
                if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                    return GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col && node instanceof ImageView;
                }
                return false;
            });

            // Load the new image for the plant
            String imageName = plant.getCurrentImage();
            Image newImage = new Image(getClass().getResourceAsStream("/images/" + imageName));
            ImageView newImageView = new ImageView(newImage);
            newImageView.setFitHeight(40);  // Match the cell size in the grid
            newImageView.setFitWidth(40);

            // Create a pane to center the image
            StackPane pane = new StackPane();
            pane.getChildren().add(newImageView);
            gridPane.add(pane, col, row);
        });
    }


    private void handleDisplayParasiteEvent(DisplayParasiteEvent event) {

        logger.info("Day: " + logDay + " Parasite displayed at row " + event.getRow() + " and column " + event.getColumn() + " with name " + event.getParasite().getName());

        // Load the image for the rat
        String imageName = "/images/" + event.getParasite().getImageName();
        Image ratImage = new Image(getClass().getResourceAsStream(imageName));
        ImageView parasiteImageView = new ImageView(ratImage);

//
        parasiteImageView.setFitHeight(50);  // Match the cell size in the grid
        parasiteImageView.setFitWidth(50);

        // Use the row and column from the event
        int row = event.getRow();
        int col = event.getColumn();

        // Place the rat image on the grid
//        gridPane.add(parasiteImageView, col, row);
//        System.out.println("Rat placed at row " + row + " and column " + col);


        // Place the parasite image on the grid in the same cell but with offset
        GridPane.setRowIndex(parasiteImageView, row);
        GridPane.setColumnIndex(parasiteImageView, col);
        GridPane.setHalignment(parasiteImageView, HPos.RIGHT);  // Align to right
        GridPane.setValignment(parasiteImageView, VPos.BOTTOM); // Align to bottom
        gridPane.getChildren().add(parasiteImageView);




        // Create a pause transition of 5 seconds before removing the rat image
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        String imagePestControlName = "/images/pControl.png";


        pause.setOnFinished(_ -> {
            pestControl(imagePestControlName, row, col);
            gridPane.getChildren().remove(parasiteImageView);  // Remove the rat image from the grid
//            System.out.println("Rat removed from row " + row + " and column " + col);
            //gridPane.getChildren().remove(pestControlImageView);
        });

        pause.play();
    }

    private void pestControl(String imagePestControlName, int row, int col){
        Image pestControlImage = new Image(getClass().getResourceAsStream(imagePestControlName));
        ImageView pestControlImageView = new ImageView(pestControlImage);

//
        pestControlImageView.setFitHeight(70);  // Match the cell size in the grid
        pestControlImageView.setFitWidth(70);

        GridPane.setRowIndex(pestControlImageView, row);
        GridPane.setColumnIndex(pestControlImageView, col);
        GridPane.setHalignment(pestControlImageView, HPos.RIGHT);  // Align to right
        GridPane.setValignment(pestControlImageView, VPos.BOTTOM); // Align to bottom
        gridPane.getChildren().add(pestControlImageView);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        pause.setOnFinished(_ -> {
            gridPane.getChildren().remove(pestControlImageView);  // Remove the rat image from the grid
//            System.out.println("Rat removed from row " + row + " and column " + col);
            //gridPane.getChildren().remove(pestControlImageView);
        });

        pause.play();
    }


//    private void loadParasitesData() {
//        for (Parasite parasite : parasiteManager.getParasites()) {
//            MenuItem menuItem = new MenuItem(parasite.getName());
//            menuItem.setOnAction(e -> handleParasiteSelection(parasite));
//            parasiteMenuButton.getItems().add(menuItem);
//        }
//    }

    private void handleParasiteSelection(Parasite parasite) {
        // Implement what happens when a parasite is selected
        // For example, display details or apply effects to the garden
//        System.out.println("Selected parasite: " + parasite.getName() + " with damage: " + parasite.getDamage());
    }

//
    @FXML
    public void showPestOnGrid() {}


    private void changeRainUI(RainEvent event) {

        startRainAnimation();

        // Stop rain after 5 seconds
        //stopRainAfterFiveSeconds();

        logger.info("Day: " + logDay + " Displayed rain event with amount: " + event.getAmount() + "mm");

        Platform.runLater(() -> {
            // Update UI to reflect it's raining
//            System.out.println("Changing UI to reflect rain event");

            // Create an ImageView for the rain icon
            Image rainImage = new Image(getClass().getResourceAsStream("/images/rain.png"));
            ImageView rainImageView = new ImageView(rainImage);
            rainImageView.setFitHeight(200);
            rainImageView.setFitWidth(200);

            // Set the text with the rain amount
            rainStatusLabel.setGraphic(rainImageView);
            rainStatusLabel.setText(event.getAmount() + "mm");

            // Create a pause transition of 5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                // Update UI to reflect no rain after the event ends
                showSunnyWeather();
            });
            pause.play();
        });
    }

    private void showSunnyWeather() {

        if(flag == 1)
            stopRainAfterFiveSeconds();
        flag = 1;
        //rainCanvas.getGraphicsContext2D().clearRect(0, 0, anchorPane.getWidth(), anchorPane.getHeight());

        logger.info("Day: " + logDay + " Displayed sunny weather");

        Platform.runLater(() -> {
            // Create an ImageView for the sun icon
            Image sunImage = new Image(getClass().getResourceAsStream("/images/sun.png"));
            ImageView sunImageView = new ImageView(sunImage);
            sunImageView.setFitHeight(200);
            sunImageView.setFitWidth(200);

            // Set the text with the sun status
            rainStatusLabel.setGraphic(sunImageView);
            rainStatusLabel.setText("Sunny");
        });
    }


    private void changeTemperatureUI(TemperatureEvent event) {

        logger.info("Day: " + logDay + " Temperature changed to: " + event.getAmount() + "°F");

        Platform.runLater(() -> {
            // Update UI to reflect the temperature change

            // Create an ImageView for the temperature icon
            String image = "normalTemperature.png";
            int fitHeight = 150;
            int fitWidth = 50;
            if (event.getAmount() <= 50 ) {
                image = "coldTemperature.png";
            } else if(event.getAmount() >= 60){
                image = "hotTemperature.png";
            }
            Image tempImage = new Image(getClass().getResourceAsStream("/images/Temperature/" + image));
            ImageView tempImageView = new ImageView(tempImage);
            tempImageView.setFitHeight(fitHeight);
            tempImageView.setFitWidth(fitWidth);
            tempImageView.setLayoutX(300.0);
            // Set the text with the temperature amount
            temperatureStatusLabel.setGraphic(tempImageView);
            temperatureStatusLabel.setText(event.getAmount() + "°F");

            // Create a pause transition of 5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                // Update UI to reflect optimal temperature after the event ends
                showOptimalTemperature();
            });
            pause.play();
        });
    }

    private void showOptimalTemperature() {

        logger.info("Day: " + logDay +" Displayed optimal temperature");

        Platform.runLater(() -> {
            // Create an ImageView for the optimal temperature icon
            Image optimalImage = new Image(getClass().getResourceAsStream("/images/Temperature/normalTemperature.png"));
            ImageView optimalImageView = new ImageView(optimalImage);
            optimalImageView.setFitHeight(150);
            optimalImageView.setFitWidth(50);
            optimalImageView.setLayoutX(100);
            // Set the text with the optimal status
            temperatureStatusLabel.setGraphic(optimalImageView);
            temperatureStatusLabel.setText("Optimal");
        });
    }

    private void changeParasiteUI(ParasiteEvent event) {

        logger.info("Day: " + logDay + " Parasite event triggered: " + event.getParasite().getName());

        Platform.runLater(() -> {
            // Update UI to reflect parasite event
//            System.out.println("Changing UI to reflect parasite event");

            // Create an ImageView for the sad icon
            Image parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));

            if (Objects.equals(event.getParasite().getName(), "Slugs")) {
                parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/slugDetected.png"));
            } else if (Objects.equals(event.getParasite().getName(), "Crow")) {
                parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/crowDetected.png"));
            } else if (Objects.equals(event.getParasite().getName(), "Locust")) {
                parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/locustDetected.png"));
            } else if (Objects.equals(event.getParasite().getName(), "Aphids")) {
                parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/aphidsDetected.png"));
            } else if (Objects.equals(event.getParasite().getName(), "Rat")) {
                parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/ratDetected.png"));
            } else if (Objects.equals(event.getParasite().getName(), "Parasite")) {
                parasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/parasiteDetected.png"));
            }

            ImageView sadImageView = new ImageView(parasiteImage);
            sadImageView.setFitHeight(60);
            sadImageView.setFitWidth(60);
            // Set the text with the parasite name
            parasiteStatusLabel.setGraphic(sadImageView);
            parasiteStatusLabel.setText(event.getParasite().getName() + " detected");

            // Create a pause transition of 5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                // Update UI to reflect no parasites after the event ends
                showNoParasites();
//                System.out.println("Parasite event ended, updating UI to show no parasites.");
            });
            pause.play();
        });
    }

    private void showNoParasites() {

        logger.info("Day: " + logDay + " Displayed no parasites status");

        Platform.runLater(() -> {
            // Create an ImageView for the happy icon
            Image happyImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
            ImageView happyImageView = new ImageView(happyImage);
            happyImageView.setFitHeight(60);
            happyImageView.setFitWidth(60);

            // Set the text with the no parasites status
            parasiteStatusLabel.setGraphic(happyImageView);
            parasiteStatusLabel.setText("No Parasites");
        });
    }

    //    This is the method that will populate the menu buttons with the plant data
    private void loadPlantsData() {

        logger.info("Day: " + currentDay + " Loading plant data from JSON file");

//        for (Flower flower : plantManager.getFlowers()) {
//            MenuItem menuItem = new MenuItem(flower.getName());
//            menuItem.setOnAction(e -> addPlantToGrid(flower.getName(), flower.getCurrentImage()));
//            flowerMenuButton.getItems().add(menuItem);
//        }

        for (Flower flower : plantManager.getFlowers()) {
            CustomMenuItem menuItem = createImageMenuItem(flower.getName(), flower.getCurrentImage());
            menuItem.setOnAction(e -> addPlantToGrid(flower.getName(), flower.getCurrentImage()));
            flowerMenuButton.getItems().add(menuItem);
        }

        logger.info("Day: " + currentDay + " Loading Tree");

//        for (Tree tree : plantManager.getTrees()) {
//            MenuItem menuItem = new MenuItem(tree.getName());
//            menuItem.setOnAction(e -> addPlantToGrid(tree.getName(), tree.getCurrentImage()));
//            treeMenuButton.getItems().add(menuItem);
//        }

        for (Tree tree : plantManager.getTrees()) {
            CustomMenuItem menuItem = createImageMenuItem(tree.getName(), tree.getCurrentImage());
            menuItem.setOnAction(e -> addPlantToGrid(tree.getName(), tree.getCurrentImage()));
            treeMenuButton.getItems().add(menuItem);
        }

//        for (Vegetable vegetable : plantManager.getVegetables()) {
//            MenuItem menuItem = new MenuItem(vegetable.getName());
//            menuItem.setOnAction(e -> addPlantToGrid(vegetable.getName(), vegetable.getCurrentImage()));
//            vegetableMenuButton.getItems().add(menuItem);
//        }

        logger.info("Day: " + currentDay + " Loading Vegetable");

        for (Vegetable vegetable : plantManager.getVegetables()) {
            logger.info("1");
            CustomMenuItem menuItem = createImageMenuItem(vegetable.getName(), vegetable.getCurrentImage());
            logger.info("2");
            menuItem.setOnAction(e -> addPlantToGrid(vegetable.getName(), vegetable.getCurrentImage()));
            vegetableMenuButton.getItems().add(menuItem);
        }


    }

    private CustomMenuItem createImageMenuItem(String name, String imagePath) {
        logger.info("3");
        // Create an HBox to hold the image and text
        HBox hBox = new HBox(20); // 10px spacing
        logger.info("4");
        hBox.setAlignment(Pos.CENTER_LEFT);
        logger.info("5");

        // Load the image
        logger.info(name);
        logger.info(imagePath);
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + imagePath)));
        logger.info("6");
        imageView.setFitWidth(120); // Set width
        imageView.setFitHeight(120); // Set height

        // Create a label for the text
        Label label = new Label(name);
        label.setStyle("-fx-font-size: 28px;");

        // Add the image and text to the HBox
        hBox.getChildren().addAll(imageView, label);

        // Wrap the HBox in a CustomMenuItem
        CustomMenuItem customMenuItem = new CustomMenuItem(hBox);
        customMenuItem.setHideOnClick(true); // Automatically hide the dropdown when clicked

        return customMenuItem;
    }

    private void addPlantToGrid(String name, String imageFile) {

        Canvas canvas = new Canvas(800, 600);
        startRainAnimation(canvas);

        Group root = new Group();
        root.getChildren().add(canvas);


        logger.info("Day: " + logDay + " Adding plant to grid: " + name + " with image: " + imageFile);

        Plant plant = plantManager.getPlantByName(name); // Assume this method retrieves the correct plant
        if (plant != null) {
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 100) { // Limit attempts to avoid potential infinite loop
                int row = random.nextInt(gardenGrid.getNumRows());
                int col = random.nextInt(gardenGrid.getNumCols());
                if (!gardenGrid.isSpotOccupied(row, col)) {

                    ImageView farmerView = new ImageView(new Image(getClass().getResourceAsStream("/images/farmer.png")));
                    farmerView.setFitHeight(60);
                    farmerView.setFitWidth(60);

                    // Create a pane to center the image
                    StackPane farmerPane = new StackPane();
                    farmerPane.getChildren().add(farmerView);
                    gridPane.add(farmerPane, col, row);

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));

                    pause.setOnFinished(_ -> {
                        gridPane.getChildren().remove(farmerPane);  // Remove the rat image from the grid
//            System.out.println("Rat removed from row " + row + " and column " + col);
                        //gridPane.getChildren().remove(pestControlImageView);
                    });
                    pause.play();

                    PauseTransition farmerPause = new PauseTransition(Duration.seconds(3));

                    farmerPause.setOnFinished(event -> {
                        // Code to execute after the 5-second pause
//                    Need row and col for logging
//                        System.out.println("Placing " + name + " at row " + row + " col " + col);
                        plant.setRow(row);
                        plant.setCol(col);
                        gardenGrid.addPlant(plant, row, col);
                        ImageView plantView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + imageFile)));
                        plantView.setFitHeight(40);
                        plantView.setFitWidth(40);

                        // Create a pane to center the image
                        StackPane pane = new StackPane();
                        pane.getChildren().add(plantView);
                        gridPane.add(pane, col, row);

                        // Optionally update UI here
                        Platform.runLater(() -> {
                            // Update your UI components if necessary
                        });
                    });

// Start the pause
                    farmerPause.play();
                    placed = true;

                }
                attempts++;
            }
            if (!placed) {
                System.err.println("Failed to place the plant after 100 attempts, grid might be full.");
            }
        } else {
            System.err.println("Plant not found: " + name);
        }
    }

    public void startRainAnimation(Canvas canvas) {
        // Raindrop class (local definition inside the function)
        class Raindrop {
            double x, y;
            double speed;

            public Raindrop(double x, double y, double speed) {
                this.x = x;
                this.y = y;
                this.speed = speed;
            }
        }

        List<Raindrop> raindrops = new ArrayList<>();
        Random random = new Random();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Generate initial raindrops
        for (int i = 0; i < 100; i++) {
            raindrops.add(new Raindrop(random.nextDouble() * canvas.getWidth(),
                    random.nextDouble() * -canvas.getHeight(),
                    2 + random.nextDouble() * 4));
        }

        // Animation timer for the rain effect
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Update raindrops
                for (Raindrop drop : raindrops) {
                    drop.y += drop.speed;
                    if (drop.y > canvas.getHeight()) {
                        drop.y = random.nextDouble() * -100;
                        drop.x = random.nextDouble() * canvas.getWidth();
                    }
                }

                // Draw raindrops
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.setFill(Color.CYAN);
                for (Raindrop drop : raindrops) {
                    gc.fillOval(drop.x, drop.y, 2, 10);
                }
            }
        }.start();
    }
}