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
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.animation.PauseTransition;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.animation.FadeTransition;

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
    private ImageView sunImageView; // Add this as a class field
    private Circle sunOrbitPath; // For visualizing the orbit path (optional)
    private AnimationTimer sunAnimationTimer; // For controlling sun animation
    private Group cloudGroup; // For holding multiple cloud images
    private double sunAngle = 0;


    @FXML
    private Rectangle treePlaceholder;
    @FXML
    private Rectangle treeTrunk;
    @FXML
    private Line rightBranch1;
    @FXML
    private Line rightBranch2;
    @FXML
    private Line leftBranch;
    private final Random random = new Random();
    private GardenGrid gardenGrid;

    //    This is the plant manager that will be used to get the plant data
//    from the JSON file, used to populate the menu buttons
    private PlantManager plantManager = PlantManager.getInstance();
    @FXML
    private HBox menuBar;


    //    Same as above but for the parasites
    private ParasiteManager parasiteManager = ParasiteManager.getInstance();

    public GardenUIController() {
        gardenGrid = GardenGrid.getInstance();
    }

    //    This is the method that will print the grid
    @FXML
    public void printGrid() {
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
        //  setupMenuBar();
        showSunnyWeather();

        showOptimalTemperature();

        showNoParasites();
        // Remove the unwanted Veg label in top left
        Platform.runLater(() -> {
            // This is a drastic approach but should work to remove any top-left labels
            for (Node node : anchorPane.getChildren()) {
                if (node instanceof Label || node instanceof Text) {
                    if (node.getLayoutX() < 50 && node.getLayoutY() < 50) {
                        if (node instanceof Label && "Veg".equals(((Label) node).getText())) {
                            node.setVisible(false);
                            anchorPane.getChildren().remove(node);
                        } else if (node instanceof Text && "Veg".equals(((Text) node).getText())) {
                            node.setVisible(false);
                            anchorPane.getChildren().remove(node);
                        }
                    }
                }
            }
        });
        removeTopLeftVeg();
        removeVegLabelById();
        removeFlowerLabelById();
        removeTreeLabelById();
        setupSimpleTreeMenu();

        // Load plants data
        loadStyledPlantsData();

        // Fix for the flower menu button - use setOnAction instead of setOnMouseClicked
        flowerMenuButton.setOnAction(event -> {
            if (!flowerMenuButton.isShowing()) {
                System.out.println("Opening flower menu from setOnAction...");
                flowerMenuButton.show();
            }
        });

        // Make sure the button is properly configured
        flowerMenuButton.setPickOnBounds(true);
        flowerMenuButton.setFocusTraversable(true);
        flowerMenuButton.setMouseTransparent(false);
        //   flowerMenuButton.setPickOnBounds(true);


//         Load the background image
//         Load the background image
        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/def.png"));


        // Create an ImageView
        ImageView imageView = new ImageView(backgroundImage);
        imageView.setPreserveRatio(false);
        imageView.setOpacity(0.9);

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
        gridPane.getColumnConstraints().clear();
        for (int col = 0; col < gardenGrid.getNumCols(); col++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPrefWidth(80);
            colConst.setHgrow(Priority.SOMETIMES); // Allow some growth
            gridPane.getColumnConstraints().add(colConst);
        }

        // Add RowConstraints with better spacing
        gridPane.getRowConstraints().clear();
        for (int row = 0; row < gardenGrid.getNumRows(); row++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(80);
            rowConst.setVgrow(Priority.SOMETIMES); // Allow some growth
            gridPane.getRowConstraints().add(rowConst);
        }

        createSimpleGradientGrid(gridPane, gardenGrid.getNumRows(), gardenGrid.getNumCols());
        gridPane.setPadding(new Insets(5, 5, 5, 5));

        // Initialize the rain canvas and animation
        rainCanvas = new Canvas(1000, 800);
        anchorPane.getChildren().add(rainCanvas); // Add the canvas to the AnchorPane
        rainDrops = new ArrayList<>();
        addVerticalTextToTrunk();
        //gridPane.setStyle("-fx-grid-lines-visible: true; -fx-border-color: brown; -fx-border-width: 2;");

        // Load plants data from JSON file and populate MenuButtons
        //loadPlantsData();
//        loadParasitesData();

        log4jLogger.info("GardenUIController initialized");


        EventBus.subscribe("RainEvent", event -> changeRainUI((RainEvent) event));
        EventBus.subscribe("DisplayParasiteEvent", event -> handleDisplayParasiteEvent((DisplayParasiteEvent) event));
        EventBus.subscribe("PlantImageUpdateEvent", event -> handlePlantImageUpdateEvent((PlantImageUpdateEvent) event));
        EventBus.subscribe("DayChangeEvent", event -> handleDayChangeEvent((DayChangeEvent) event));
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


//    public void createColoredGrid(GridPane gridPane, int numRows, int numCols) {
//        double cellWidth = 80;  // Width of each cell
//        double cellHeight = 80; // Height of each cell
//
//        // Loop through rows and columns to create cells
//        for (int row = 0; row < numRows; row++) {
//            for (int col = 0; col < numCols; col++) {
//                // Create a StackPane for each cell
//                StackPane cell = new StackPane();
//
//                // Set preferred size of the cell
//                cell.setPrefSize(cellWidth, cellHeight);
//
//                // Set a unique border color for each cell
//                Color borderColor = Color.BROWN; // Function to generate random colors
//                cell.setBorder(new Border(new BorderStroke(
//                        borderColor,
//                        BorderStrokeStyle.SOLID,
//                        CornerRadii.EMPTY,
//                        new BorderWidths(2) // Border thickness
//                )));
//
//                // Add the cell to the GridPane
//                gridPane.add(cell, col, row);
//            }
//        }
//    }


    private void handlePlantDeathUIChangeEvent(Plant plant) {

    }

    private void handlePlantHealthUpdateEvent(PlantHealthUpdateEvent event) {
        logger.info("Day: " + logDay + " Plant health updated at row " + event.getRow() + " and column " + event.getCol() + " from " + event.getOldHealth() + " to " + event.getNewHealth());
//        System.out.println("Plant health updated at row " + event.getRow() + " and column " + event.getCol() + " from " + event.getOldHealth() + " to " + event.getNewHealth());
    }

    private void handleInitializeGarden() {
        Object[][] gardenLayout = {
                {"Oak", 0, 1}, {"Maple", 0, 5}, {"Pine", 0, 6},
                {"Tomato", 1, 6}, {"Carrot", 2, 2}, {"Lettuce", 1, 0},
                {"Sunflower", 3, 1}, {"Rose", 4, 4}, {"Jasmine", 4, 6},
                {"Oak", 4, 6}, {"Tomato", 3, 0}, {"Sunflower", 4, 3}  // Adjusted invalid rows
        };

        Platform.runLater(() -> {
            for (Object[] plantInfo : gardenLayout) {
                String plantType = (String) plantInfo[0];
                int row = (Integer) plantInfo[1];
                int col = (Integer) plantInfo[2];

                // Prevent out-of-bounds errors
                if (row >= gardenGrid.getNumRows() || col >= gardenGrid.getNumCols()) {
                    logger.error("Invalid plant position: " + plantType + " at (" + row + ", " + col + ")");
                    continue; // Skip adding this plant
                }

                Plant plant = plantManager.getPlantByName(plantType);
                if (plant != null) {
                    plant.setRow(row);
                    plant.setCol(col);
                    try {
                        gardenGrid.addPlant(plant, row, col);
                        addPlantToGridUI(plant, row, col);
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
        imageView.setFitHeight(45); // Slightly larger
        imageView.setFitWidth(45);

        // Add drop shadow for better visibility
        javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(1);
        dropShadow.setOffsetY(1);
        imageView.setEffect(dropShadow);

        StackPane pane = new StackPane(imageView);
        pane.setStyle("-fx-alignment: center;");

        // Add a scale transition when adding plants
        ScaleTransition growTransition = new ScaleTransition(Duration.millis(600), imageView);
        growTransition.setFromX(0.2);
        growTransition.setFromY(0.2);
        growTransition.setToX(1.0);
        growTransition.setToY(1.0);

        gridPane.add(pane, col, row);
        GridPane.setHalignment(pane, HPos.CENTER);
        GridPane.setValignment(pane, VPos.CENTER);

        // Play the animation
        growTransition.play();
    }

    // Alternative direct approach to remove the Veg label by CSS ID
    private void removeVegLabelById() {
        Platform.runLater(() -> {
            // Look for any element with the ID 'vegLabel' or similar
            for (Node node : anchorPane.getChildrenUnmodifiable()) {
                if (node.getId() != null &&
                        (node.getId().contains("veg") || node.getId().contains("Veg"))) {
                    node.setVisible(false); // Hide it
                    ((Parent)node.getParent()).getChildrenUnmodifiable().remove(node); // Try to remove it
                }
            }

            // Another approach is to overlay a white rectangle on top of it
            Rectangle coverRect = new Rectangle(0, 0, 50, 50);
            coverRect.setFill(javafx.scene.paint.Color.WHITE);
            coverRect.setOpacity(1.0);
            anchorPane.getChildren().add(coverRect);
        });
    }
    private void removeFlowerLabelById() {
        Platform.runLater(() -> {
            // Look for any element with the ID 'vegLabel' or similar
            for (Node node : anchorPane.getChildrenUnmodifiable()) {
                if (node.getId() != null &&
                        (node.getId().contains("flower") || node.getId().contains("Flower"))) {
                    node.setVisible(false); // Hide it
                    ((Parent)node.getParent()).getChildrenUnmodifiable().remove(node); // Try to remove it
                }
            }

            // Another approach is to overlay a white rectangle on top of it
            Rectangle coverRect = new Rectangle(0, 0, 50, 50);
            coverRect.setFill(javafx.scene.paint.Color.WHITE);
            coverRect.setOpacity(1.0);
            anchorPane.getChildren().add(coverRect);
        });
    }
    private void removeTreeLabelById() {
        Platform.runLater(() -> {
            // Look for any element with the ID 'vegLabel' or similar
            for (Node node : anchorPane.getChildrenUnmodifiable()) {
                if (node.getId() != null &&
                        (node.getId().contains("tree") || node.getId().contains("Tree"))) {
                    node.setVisible(false); // Hide it
                    ((Parent)node.getParent()).getChildrenUnmodifiable().remove(node); // Try to remove it
                }
            }

            // Another approach is to overlay a white rectangle on top of it
            Rectangle coverRect = new Rectangle(0, 0, 50, 50);
            coverRect.setFill(javafx.scene.paint.Color.WHITE);
            coverRect.setOpacity(1.0);
            anchorPane.getChildren().add(coverRect);
        });
    }


    //    Function that is called when the parasite damage event is published
    private void handleParasiteDamageEvent(ParasiteDamageEvent event) {
        logger.info("Day: " + logDay + " Displayed plant damaged at row " + event.getRow() +
                " and column " + event.getCol() + " by " + event.getDamage());

        Platform.runLater(() -> {
            int row = event.getRow();
            int col = event.getCol();
            int damage = event.getDamage();

            // Create a pane to hold the damage display
            StackPane damagePane = new StackPane();

            // Create a background shape for better visibility
            Rectangle background = new Rectangle(30, 25);
            background.setFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.85)); // More opaque white background
            background.setArcWidth(10);
            background.setArcHeight(10);
            background.setStroke(javafx.scene.paint.Color.RED);
            background.setStrokeWidth(2);

            // Add drop shadow to the background
            javafx.scene.effect.DropShadow bgShadow = new javafx.scene.effect.DropShadow();
            bgShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.5));
            bgShadow.setRadius(5);
            bgShadow.setOffsetY(2);
            background.setEffect(bgShadow);

            // Create a label with larger, bolder text
            Label damageLabel = new Label("-" + damage);
            damageLabel.setTextFill(javafx.scene.paint.Color.RED);
            damageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            // Ensure damage label is on top of all other children in the stack pane
            StackPane.setAlignment(damageLabel, Pos.CENTER);
          //  StackPane.setZOrder(damageLabel, Integer.MAX_VALUE);

            // Add drop shadow for better contrast
            javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
            dropShadow.setColor(javafx.scene.paint.Color.BLACK);
            dropShadow.setRadius(3);
            dropShadow.setSpread(0.2);
            damageLabel.setEffect(dropShadow);

            // Add background and label to the pane
            damagePane.getChildren().addAll(background, damageLabel);

            // Set the pane's position in the grid
            GridPane.setRowIndex(damagePane, row);
            GridPane.setColumnIndex(damagePane, col);
            GridPane.setHalignment(damagePane, HPos.CENTER);  // Center horizontally
            GridPane.setValignment(damagePane, VPos.TOP);     // Position at the top of the cell

            // Add margin to push the damage number upward, away from plants
            GridPane.setMargin(damagePane, new Insets(2, 0, 0, 0));
            gridPane.getChildren().add(damagePane);

            // Set an initial elevation so damage numbers appear above plants
            damagePane.setViewOrder(-1.0); // Lower values appear on top in JavaFX

            // Add entrance animation with scale and slight bounce
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), damagePane);
            scaleIn.setFromX(0.1);
            scaleIn.setFromY(0.1);
            scaleIn.setToX(1.2); // Slightly overshoot
            scaleIn.setToY(1.2);

            // Add a second transition to create bounce effect
            ScaleTransition scaleBounce = new ScaleTransition(Duration.millis(100), damagePane);
            scaleBounce.setFromX(1.2);
            scaleBounce.setFromY(1.2);
            scaleBounce.setToX(1.0);
            scaleBounce.setToY(1.0);

            // Sequence the animations
            scaleIn.setOnFinished(event1 -> scaleBounce.play());
            scaleIn.play();

            // Remove the damage display after delay
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> {
                // Add exit animation
                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), damagePane);
                scaleOut.setFromX(1.0);
                scaleOut.setFromY(1.0);
                scaleOut.setToX(0.1);
                scaleOut.setToY(0.1);
                scaleOut.setOnFinished(event2 -> gridPane.getChildren().remove(damagePane));
                scaleOut.play();
            });
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
                Integer nodeRow = GridPane.getRowIndex(node);
                Integer nodeCol = GridPane.getColumnIndex(node);
                return nodeRow != null && nodeCol != null && nodeRow == row && nodeCol == col;
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

    private void pestControl(String imagePestControlName, int row, int col) {
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
    public void showPestOnGrid() {
    }


//    private void changeRainUI(RainEvent event) {
//        // Start rain animation
//        startRainAnimation();
//
//        logger.info("Day: " + logDay + " Displayed rain event with amount: " + event.getAmount() + "mm");
//
//        Platform.runLater(() -> {
//            // Stop sun animation if running
//            stopSunAnimation();
//
//            // Hide sun if visible
//            if (sunImageView != null) {
//                sunImageView.setVisible(false);
//            }
//
//            // Create or update cloud group
//            if (cloudGroup == null) {
//                cloudGroup = new Group();
//                anchorPane.getChildren().add(cloudGroup);
//            } else {
//                cloudGroup.getChildren().clear();
//            }
//
//            // Add multiple clouds with different positions
//            addMultipleClouds(cloudGroup, event.getAmount());
//
//            // Set the text with the rain amount in small blue font
//            rainStatusLabel.setGraphic(null); // Remove any existing graphics
//            rainStatusLabel.setText(event.getAmount() + "mm");
//            rainStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1E90FF; -fx-font-weight: bold;");
//
//            // Create a pause transition
//            PauseTransition pause = new PauseTransition(Duration.seconds(5));
//            pause.setOnFinished(e -> {
//                // Update UI to reflect no rain after the event ends
//                showSunnyWeather();
//            });
//            pause.play();
//        });
//    }
//
//    // New method to add multiple clouds
//    private void addMultipleClouds(Group cloudGroup, int rainAmount) {
//        // Load cloud image
//        Image cloudImage = new Image(getClass().getResourceAsStream("/images/rain.png"));
//
//        // Add 8 clouds with different positions and sizes
//        Random random = new Random();
//        for (int i = 0; i < 4; i++) {
//            ImageView cloudView = new ImageView(cloudImage);
//
//            // Vary cloud size
//            double sizeVariation = 0.6 + (random.nextDouble() * 0.8); // 0.6 to 1.4 size factor
//            cloudView.setFitHeight(70 * sizeVariation);
//            cloudView.setFitWidth(70 * sizeVariation);
//
//            // Position clouds across the top area
//            double xPos = 50 + (i * 100) + (random.nextDouble() * 40 - 20);
//            double yPos = 20 + (random.nextDouble() * 60);
//
//            cloudView.setLayoutX(xPos);
//            cloudView.setLayoutY(yPos);
//
//            // Add a subtle cloud drift animation
//            TranslateTransition drift = new TranslateTransition(Duration.seconds(10 + random.nextDouble() * 5), cloudView);
//            drift.setByX(random.nextDouble() * 40 - 20); // Drift slightly left or right
//            drift.setAutoReverse(true);
//            drift.setCycleCount(TranslateTransition.INDEFINITE);
//            drift.play();
//
//            // Add drop shadow for depth
//            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
//            shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
//            shadow.setRadius(5);
//            shadow.setOffsetY(3);
//            cloudView.setEffect(shadow);
//
//            // Add cloud to group
//            cloudGroup.getChildren().add(cloudView);
//
//            // Add cloud entrance animation
//            FadeTransition fadeIn = new FadeTransition(Duration.millis(500 + i * 100), cloudView);
//            fadeIn.setFromValue(0);
//            fadeIn.setToValue(1);
//            fadeIn.play();
//        }
//    }
//
//    // Update stopRainAfterFiveSeconds method
//    private void stopRainAfterFiveSeconds() {
//        PauseTransition pauseRain = new PauseTransition(Duration.seconds(1));
//        pauseRain.setOnFinished(event -> {
//            // Clear the canvas and stop the animation
//            if (rainAnimation != null) {
//                rainAnimation.stop();
//            }
//            if (rainCanvas != null && rainCanvas.getGraphicsContext2D() != null) {
//                rainCanvas.getGraphicsContext2D().clearRect(0, 0, 1000, 800);
//            }
//
//            // Fade out clouds if present
//            if (cloudGroup != null) {
//                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), cloudGroup);
//                fadeOut.setFromValue(1.0);
//                fadeOut.setToValue(0.0);
//                fadeOut.setOnFinished(e -> {
//                    anchorPane.getChildren().remove(cloudGroup);
//                    cloudGroup = null;
//                });
//                fadeOut.play();
//            }
//        });
//        pauseRain.play();
//    }

    private void addVerticalTextToTrunk() {
        Platform.runLater(() -> {
            try {
                // Create a Group to hold our vertical text
                Group textGroup = new Group();

                // Text will display "ADD ME" vertically
                Text addMeText = new Text("ADD ME");

                // Set the text styling
                addMeText.setFill(javafx.scene.paint.Color.WHITE);
                addMeText.setStroke(javafx.scene.paint.Color.rgb(70, 40, 20, 0.9)); // Brown stroke
                addMeText.setStrokeWidth(1.5);
                addMeText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));

                // Create a drop shadow effect for better visibility
                javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
                shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
                shadow.setRadius(3);
                shadow.setOffsetX(2);
                shadow.setOffsetY(2);
                addMeText.setEffect(shadow);

                // Rotate the text 90 degrees to make it vertical (down the trunk)
                addMeText.getTransforms().add(new javafx.scene.transform.Rotate(90, 0, 0));

                // Add the text to the group
                textGroup.getChildren().add(addMeText);

                // Position the text on the trunk
                // Note: Adjust these coordinates to match your trunk's position
                double trunkX = anchorPane.getWidth() - 80; // Position on the right side like in your screenshot
                double trunkY = 300; // Middle of the trunk height

                textGroup.setLayoutX(trunkX);
                textGroup.setLayoutY(trunkY);

                // Add the text group to the scene
                anchorPane.getChildren().add(textGroup);

                // Add a hovering animation to draw attention
                javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(
                        Duration.millis(1500), textGroup);
                scaleTransition.setFromX(1.0);
                scaleTransition.setFromY(1.0);
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.setCycleCount(javafx.animation.Animation.INDEFINITE);
                scaleTransition.setAutoReverse(true);
                scaleTransition.play();

                // Make the text clickable - when clicked it can show the menu
                textGroup.setOnMouseClicked(event -> {
                    // You can add code here to show your plant selection menu
                    // For example, if you have a method to show your menu:
                    // showPlantSelectionMenu();
                    logger.info("ADD ME text clicked, could show plant selection menu here");
                });

                // Add hover effect
                textGroup.setOnMouseEntered(event -> {
                    textGroup.setCursor(javafx.scene.Cursor.HAND);
                    addMeText.setFill(javafx.scene.paint.Color.YELLOW);
                });

                textGroup.setOnMouseExited(event -> {
                    textGroup.setCursor(javafx.scene.Cursor.DEFAULT);
                    addMeText.setFill(javafx.scene.paint.Color.WHITE);
                });

            } catch (Exception e) {
                logger.error("Error adding vertical text to trunk: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    private void showSunnyWeather() {
        // Stop rain if it's active
        if (flag == 1) {
            stopRainAfterFiveSeconds();
        }
        flag = 1;

        logger.info("Day: " + logDay + " Displayed sunny weather");

        Platform.runLater(() -> {
            // Clear any existing cloud group
            if (cloudGroup != null) {
                anchorPane.getChildren().remove(cloudGroup);
            }

            // Create a smaller sun image
            if (sunImageView == null) {
                Image sunImage = new Image(getClass().getResourceAsStream("/images/sun.png"));
                sunImageView = new ImageView(sunImage);
                sunImageView.setFitHeight(100); // Reduced from 200 to 100
                sunImageView.setFitWidth(100);  // Reduced from 200 to 100

                // Add drop shadow for a glowing effect
                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                glow.setColor(javafx.scene.paint.Color.YELLOW);
                glow.setRadius(15);
                glow.setSpread(0.8);
                sunImageView.setEffect(glow);

                // Add to the scene if not already there
                anchorPane.getChildren().add(sunImageView);
            } else {
                // Make sure the sun is visible
                sunImageView.setVisible(true);
            }

            // Set initial position
            sunImageView.setLayoutX(100);
            sunImageView.setLayoutY(100);

            // Always restart sun animation (fixes issue where animation doesn't restart after rain)
            startSunAnimation();

            // Set the label text with smaller yellow font
            rainStatusLabel.setGraphic(null); // Remove any existing graphics
            rainStatusLabel.setText("Sunny");
            rainStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");
        });
    }

    // New method to start sun animation
    private void startSunAnimation() {
        // Define sun orbit radius and center position
        final double orbitRadius = 30;
        final double centerX = 130; // Center of orbit
        final double centerY = 110; // Center of orbit

        // Visualize the orbit path (optional - comment out if you don't want to see it)
    /*
    if (sunOrbitPath == null) {
        sunOrbitPath = new Circle(centerX, centerY, orbitRadius);
        sunOrbitPath.setFill(null);
        sunOrbitPath.setStroke(javafx.scene.paint.Color.LIGHTYELLOW);
        sunOrbitPath.setStrokeWidth(1);
        sunOrbitPath.setOpacity(0.3);
        anchorPane.getChildren().add(sunOrbitPath);
    }
    */

        // Create animation timer for sun movement
        sunAnimationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Slowly update the angle
                sunAngle += 0.005; // Controls rotation speed

                // Calculate new position
                double newX = centerX + orbitRadius * Math.cos(sunAngle);
                double newY = centerY + orbitRadius * Math.sin(sunAngle);

                // Update sun position
                sunImageView.setLayoutX(newX - (sunImageView.getFitWidth() / 2));
                sunImageView.setLayoutY(newY - (sunImageView.getFitHeight() / 2));
            }
        };
        sunAnimationTimer.start();
    }

    // Method to stop sun animation
    private void stopSunAnimation() {
        if (sunAnimationTimer != null) {
            sunAnimationTimer.stop();
            sunAnimationTimer = null; // Reset to null so we can create a new animation later
        }
    }

    // Update changeRainUI method
    private void changeRainUI(RainEvent event) {
        // Start rain animation
        startRainAnimation();

        logger.info("Day: " + logDay + " Displayed rain event with amount: " + event.getAmount() + "mm");

        Platform.runLater(() -> {
            // Stop sun animation if running
            stopSunAnimation();

            // Hide sun if visible
            if (sunImageView != null) {
                sunImageView.setVisible(false);
            }

            // Create or update cloud group
            if (cloudGroup == null) {
                cloudGroup = new Group();
                anchorPane.getChildren().add(cloudGroup);
            } else {
                cloudGroup.getChildren().clear();
            }

            // Add multiple clouds with different positions
            addMultipleClouds(cloudGroup, event.getAmount());

            // Set the text with the rain amount in small blue font
            rainStatusLabel.setGraphic(null); // Remove any existing graphics
            rainStatusLabel.setText(event.getAmount() + "mm");
            rainStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1E90FF; -fx-font-weight: bold;");

            // Create a pause transition
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                // Update UI to reflect no rain after the event ends
                showSunnyWeather();
            });
            pause.play();
        });
    }

    // New method to add multiple clouds
    private void addMultipleClouds(Group cloudGroup, int rainAmount) {
        // Load cloud image
        Image cloudImage = new Image(getClass().getResourceAsStream("/images/rain.png"));

        // Add 4 clouds (reduced from 8) with different positions and sizes
        Random random = new Random();

        // Get window width to better position clouds
        double windowWidth = anchorPane.getWidth();
        if (windowWidth <= 0) windowWidth = 800; // Fallback if width not available

        // Calculate spacing for 4 clouds
        double spacing = windowWidth / 5; // Divide by 5 to get 4 spaces between 5 points

        for (int i = 0; i < 4; i++) {
            ImageView cloudView = new ImageView(cloudImage);

            // Vary cloud size
            double sizeVariation = 0.7 + (random.nextDouble() * 0.6); // 0.7 to 1.3 size factor
            cloudView.setFitHeight(80 * sizeVariation);
            cloudView.setFitWidth(80 * sizeVariation);

            // Position clouds evenly across the top area
            // Use spacing to position clouds evenly
            double xPos = spacing * (i + 1) - (cloudView.getFitWidth() / 2);

            // Position at very top of window with minimal variation
            double yPos = 0 + (random.nextDouble() * 15); // 0-15px from top

            cloudView.setLayoutX(xPos);
            cloudView.setLayoutY(yPos);

            // Add a subtle cloud drift animation
            TranslateTransition drift = new TranslateTransition(Duration.seconds(15 + random.nextDouble() * 5), cloudView);
            drift.setByX(random.nextDouble() * 30 - 15); // Drift slightly left or right
            drift.setAutoReverse(true);
            drift.setCycleCount(TranslateTransition.INDEFINITE);
            drift.play();

            // Add blue outline stroke to the cloud
            javafx.scene.effect.DropShadow outline = new javafx.scene.effect.DropShadow();
            outline.setColor(javafx.scene.paint.Color.rgb(30, 144, 255, 0.7)); // Blue outline
            outline.setRadius(3);
            outline.setSpread(0.4);

            // Add drop shadow for depth beneath the blue outline
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
            shadow.setRadius(5);
            shadow.setOffsetY(3);
            outline.setInput(shadow); // Combine effects

            cloudView.setEffect(outline);

            // Add cloud to group
            cloudGroup.getChildren().add(cloudView);

            // Add cloud entrance animation with slight delay between clouds
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400 + i * 150), cloudView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }

    // Update stopRainAfterFiveSeconds method
    private void stopRainAfterFiveSeconds() {
        PauseTransition pauseRain = new PauseTransition(Duration.seconds(1));
        pauseRain.setOnFinished(event -> {
            // Clear the canvas and stop the animation
            if (rainAnimation != null) {
                rainAnimation.stop();
            }
            if (rainCanvas != null && rainCanvas.getGraphicsContext2D() != null) {
                rainCanvas.getGraphicsContext2D().clearRect(0, 0, 1000, 800);
            }

            // Fade out clouds if present
            if (cloudGroup != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), cloudGroup);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    anchorPane.getChildren().remove(cloudGroup);
                    cloudGroup = null;
                });
                fadeOut.play();
            }
        });
        pauseRain.play();
    }


    private void changeTemperatureUI(TemperatureEvent event) {

        logger.info("Day: " + logDay + " Temperature changed to: " + event.getAmount() + "°F");

        Platform.runLater(() -> {
            // Update UI to reflect the temperature change

            // Create an ImageView for the temperature icon
            String image = "normalTemperature.png";
            int fitHeight = 150;
            int fitWidth = 50;
            if (event.getAmount() <= 50) {
                image = "coldTemperature.png";
            } else if (event.getAmount() >= 60) {
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

        logger.info("Day: " + logDay + " Displayed optimal temperature");

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
//    private void setupTreeMenu() {
//        // Add tooltips to menu buttons
//        Tooltip.install(treeMenuButton, new Tooltip("Add Trees to your garden"));
//        Tooltip.install(flowerMenuButton, new Tooltip("Add Flowers to your garden"));
//        Tooltip.install(vegetableMenuButton, new Tooltip("Add Vegetables to your garden"));
//
//        // Set the popup direction for each menu button
//        treeMenuButton.setPopupSide(Side.RIGHT);
//        flowerMenuButton.setPopupSide(Side.LEFT);
//        vegetableMenuButton.setPopupSide(Side.BOTTOM);
//
//        // Apply additional styling
//        String baseMenuButtonStyle = "-fx-background-radius: 30px; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0;";
//        treeMenuButton.setStyle(baseMenuButtonStyle + "-fx-background-color: #2E8B57;");
//        flowerMenuButton.setStyle(baseMenuButtonStyle + "-fx-background-color: #FF69B4;");
//        vegetableMenuButton.setStyle(baseMenuButtonStyle + "-fx-background-color: #FF8C00;");
//
//        // Make arrows smaller or hidden
//        treeMenuButton.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
//        flowerMenuButton.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
//        vegetableMenuButton.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
//    }

    private void setupMenuEventHandlers() {
        // Add hover animations for menu buttons
        setupMenuButtonAnimation(treeMenuButton);
        setupMenuButtonAnimation(flowerMenuButton);
        setupMenuButtonAnimation(vegetableMenuButton);

        // Add hover effect for trunk and branches
        setupTreePartHoverEffect(treeTrunk);
        setupTreePartHoverEffect(rightBranch1);
        setupTreePartHoverEffect(rightBranch2);
        setupTreePartHoverEffect(leftBranch);
    }

    private void setupMenuButtonAnimation(MenuButton button) {
        // Scale animation on hover
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);

        button.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);
            scaleTransition.playFromStart();
        });

        button.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.playFromStart();
        });

        // Style context menu when showing
        button.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Apply custom styles to dropdown menu
                button.getContextMenu().setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                "-fx-background-radius: 15px; " +
                                "-fx-padding: 8px; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);"
                );
            }
        });
    }

    private void setupTreePartHoverEffect(javafx.scene.shape.Shape shape) {
        shape.setOnMouseEntered(e -> {
            shape.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        });

        shape.setOnMouseExited(e -> {
            shape.setStyle("");
        });
    }

//    private void loadPlantsData() {
//        // Clear existing items
//        treeMenuButton.getItems().clear();
//        flowerMenuButton.getItems().clear();
//        vegetableMenuButton.getItems().clear();
//
//        // Load trees with styled menu items
//        for (Tree tree : plantManager.getTrees()) {
//            MenuItem menuItem = createStyledMenuItem(tree.getName(), tree.getCurrentImage());
//            menuItem.setOnAction(e -> addPlantToGrid(tree.getName(), tree.getCurrentImage()));
//            treeMenuButton.getItems().add(menuItem);
//        }
//
//        // Load flowers with styled menu items
//        for (Flower flower : plantManager.getFlowers()) {
//            MenuItem menuItem = createStyledMenuItem(flower.getName(), flower.getCurrentImage());
//            menuItem.setOnAction(e -> addPlantToGrid(flower.getName(), flower.getCurrentImage()));
//            flowerMenuButton.getItems().add(menuItem);
//        }
//
//        // Load vegetables with styled menu items
//        for (Vegetable vegetable : plantManager.getVegetables()) {
//            MenuItem menuItem = createStyledMenuItem(vegetable.getName(), vegetable.getCurrentImage());
//            menuItem.setOnAction(e -> addPlantToGrid(vegetable.getName(), vegetable.getCurrentImage()));
//            vegetableMenuButton.getItems().add(menuItem);
//        }
//    }

//    private MenuItem createStyledMenuItem(String name, String imagePath) {
//        MenuItem menuItem = new MenuItem(name);
//
//        try {
//            // Try to load an image with a scaled-down size
//            Image image = new Image(getClass().getResourceAsStream("/images/" + imagePath), 24, 24, true, true);
//            ImageView imageView = new ImageView(image);
//            menuItem.setGraphic(imageView);
//        } catch (Exception e) {
//            // If image loading fails, just use text
//            log4jLogger.error("Failed to load image for menu item: " + name);
//        }
//
//        // Add padding and styling
//        menuItem.setStyle("-fx-padding: 8px 12px; -fx-font-size: 14px;");
//
//        return menuItem;
//    }

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
//    private void loadPlantsData() {
//        for (Tree tree : plantManager.getTrees()) {
//            MenuItem menuItem = new MenuItem(tree.getName());
//            treeMenuButton.getItems().add(menuItem);
//        }
//        for (Flower flower : plantManager.getFlowers()) {
//            MenuItem menuItem = new MenuItem(flower.getName());
//            flowerMenuButton.getItems().add(menuItem);
//        }
//        for (Vegetable vegetable : plantManager.getVegetables()) {
//            MenuItem menuItem = new MenuItem(vegetable.getName());
//            vegetableMenuButton.getItems().add(menuItem);
//        }
//    }

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

    private void createTreeImage() {
        try {
            // Create a tree trunk image
            ImageView treeImageView = new ImageView();

            // You can either:
            // 1. Use an existing image if you have one:
            // Image treeImage = new Image(getClass().getResourceAsStream("/images/tree_trunk.png"));

            // 2. Or create one programmatically (recommended for more control):
            Image treeImage = createTreeTrunkImage();

            treeImageView.setImage(treeImage);
            treeImageView.setFitWidth(80);
            treeImageView.setFitHeight(200);

            // Position the tree trunk
            treeImageView.setLayoutX(treePlaceholder.getLayoutX() - 40);
            treeImageView.setLayoutY(treePlaceholder.getLayoutY() - 60);

            // Add to the scene
            anchorPane.getChildren().add(treeImageView);

            // Make sure the tree is behind the menu buttons
            treeImageView.toBack();

            // Draw branches
            drawTreeBranches();

        } catch (Exception e) {
            log4jLogger.error("Failed to create tree image: " + e.getMessage());
        }
    }

    private Image createTreeTrunkImage() {
        // Create a simple tree trunk with branches using JavaFX canvas
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(100, 250);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw trunk
        gc.setFill(javafx.scene.paint.Color.web("#8B4513"));
        gc.fillRect(40, 50, 30, 180);

        // Draw some texture/detail on trunk
        gc.setStroke(javafx.scene.paint.Color.web("#704214"));
        gc.setLineWidth(2);
        gc.strokeLine(45, 60, 45, 220);
        gc.strokeLine(55, 70, 55, 210);
        gc.strokeLine(65, 80, 65, 200);

        // Convert to Image
        return canvas.snapshot(null, null);
    }

    private void drawTreeBranches() {
        // Create branch to the right-up for tree button
        javafx.scene.shape.Line rightUpBranch = new javafx.scene.shape.Line();
        rightUpBranch.setStartX(treePlaceholder.getLayoutX() + 5);
        rightUpBranch.setStartY(treePlaceholder.getLayoutY() + 30);
        rightUpBranch.setEndX(treeMenuButton.getLayoutX() + 30);
        rightUpBranch.setEndY(treeMenuButton.getLayoutY() + 30);
        rightUpBranch.setStrokeWidth(8);
        rightUpBranch.setStroke(javafx.scene.paint.Color.web("#8B4513"));

        // Create branch to the left for flower button
        javafx.scene.shape.Line leftBranch = new javafx.scene.shape.Line();
        leftBranch.setStartX(treePlaceholder.getLayoutX() + 5);
        leftBranch.setStartY(treePlaceholder.getLayoutY() + 70);
        leftBranch.setEndX(flowerMenuButton.getLayoutX() + 30);
        leftBranch.setEndY(flowerMenuButton.getLayoutY() + 30);
        leftBranch.setStrokeWidth(8);
        leftBranch.setStroke(javafx.scene.paint.Color.web("#8B4513"));

        // Create branch to the right-down for vegetable button
        javafx.scene.shape.Line rightDownBranch = new javafx.scene.shape.Line();
        rightDownBranch.setStartX(treePlaceholder.getLayoutX() + 5);
        rightDownBranch.setStartY(treePlaceholder.getLayoutY() + 110);
        rightDownBranch.setEndX(vegetableMenuButton.getLayoutX() + 30);
        rightDownBranch.setEndY(vegetableMenuButton.getLayoutY() + 30);
        rightDownBranch.setStrokeWidth(8);
        rightDownBranch.setStroke(javafx.scene.paint.Color.web("#8B4513"));

        // Add branches to the scene
        anchorPane.getChildren().addAll(rightUpBranch, leftBranch, rightDownBranch);

        // Make sure branches are behind buttons
        rightUpBranch.toBack();
        leftBranch.toBack();
        rightDownBranch.toBack();
    }

    private void hideMenuButtonArrows(MenuButton button) {
        // Use a safer approach that doesn't rely on lookup()
        button.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);

        // Apply CSS to hide arrow instead of trying to access it directly
        button.setStyle(button.getStyle() + " -fx-mark-color: transparent; -fx-padding: 0;");
    }

    // Here's a simpler setupCircularMenuButtons method that avoids the error
    private void setupCircularMenuButtons() {
        // Add tooltips to menu buttons
        Tooltip.install(treeMenuButton, new Tooltip("Add Trees to your garden"));
        Tooltip.install(flowerMenuButton, new Tooltip("Add Flowers to your garden"));
        Tooltip.install(vegetableMenuButton, new Tooltip("Add Vegetables to your garden"));

        // Set the popup direction for each menu button
        treeMenuButton.setPopupSide(Side.RIGHT);
        flowerMenuButton.setPopupSide(Side.BOTTOM);
        vegetableMenuButton.setPopupSide(Side.BOTTOM);

        // Apply styling with hidden arrows to make buttons perfectly circular
        String baseStyle = "-fx-background-radius: 30px; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-mark-color: transparent; -fx-padding: 5px;";

        treeMenuButton.setStyle(baseStyle + "-fx-background-color: #2E8B57;");
        flowerMenuButton.setStyle(baseStyle + "-fx-background-color: #FF69B4;");
        flowerMenuButton.setStyle("-fx-padding: 15px; -fx-background-radius: 50%;");

        vegetableMenuButton.setStyle(baseStyle + "-fx-background-color: #FF8C00;");

        // Add hover animations
        addHoverAnimation(treeMenuButton);
        addHoverAnimation(flowerMenuButton);
        addHoverAnimation(vegetableMenuButton);

        // Add styling for context menus
        styleContextMenus();
    }

    // A simpler alternative for the entire tree menu setup
    // Replace your existing setupSimpleTreeMenu method with this fixed version
    // Replace this method in your code to fix the flower menu
    // Replace this method in your code to fix the flower menu using direct popup
    // Fix the variable initialization in the setupSimpleTreeMenu method
    private void setupSimpleTreeMenu() {
        try {
            // Get anchor dimensions - FIXED VARIABLE INITIALIZATION ERROR
            double anchorWidth = anchorPane.getWidth() > 0 ? anchorPane.getWidth() : 1187.0;
            double anchorHeight = anchorPane.getHeight() > 0 ? anchorPane.getHeight() : 641.0;

            // Position the tree on the right side of the screen
            double trunkX = anchorWidth - 80; // Moved a bit more to the right
            double trunkTop = 120; // Start higher
            double trunkHeight = anchorHeight - 170; // Extend trunk more

            // Rest of the method remains the same...

            // Remove any existing tree elements to prevent duplicates
            anchorPane.getChildren().removeIf(node ->
                    node instanceof javafx.scene.shape.Arc ||
                            node instanceof javafx.scene.shape.Polygon ||
                            node instanceof javafx.scene.shape.Rectangle ||
                            node instanceof Circle && ((Circle) node).getRadius() > 40);

            // Create a thinner trunk on the right
            javafx.scene.shape.Rectangle trunk = new javafx.scene.shape.Rectangle(
                    trunkX, trunkTop,
                    60, trunkHeight // Reduced width from 80 to 60
            );

            // Create gradient for trunk
            javafx.scene.paint.LinearGradient trunkGradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#8B4513")),
                    new javafx.scene.paint.Stop(0.5, javafx.scene.paint.Color.web("#A0522D")),
                    new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#8B4513"))
            );
            trunk.setFill(trunkGradient);

            // Add a border to the trunk
            trunk.setStroke(javafx.scene.paint.Color.web("#5D3112"));
            trunk.setStrokeWidth(3);
            trunk.setArcWidth(20);
            trunk.setArcHeight(20);

            // Add shadow effect to trunk
            trunk.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.5)));

            // Create bigger colorful circles for each button - vertically stacked
            double circleX = trunkX - 150;
            double spacing = 160; // Increased spacing between buttons (was 140)

            Circle treeCircle = createEnhancedCircleButton("#4CAF50");
            Circle flowerCircle = createEnhancedCircleButton("#FF69B4");
            Circle vegCircle = createEnhancedCircleButton("#FF8C00");

            // Texts for button labels with larger font
            Label treeLabel = createEnhancedButtonLabel("Tree");
            Label flowerLabel = createEnhancedButtonLabel("Flower");
            Label vegLabel = createEnhancedButtonLabel("Veg");

            // Create stackpanes to hold circles and labels
            StackPane treeButton = new StackPane(treeCircle, treeLabel);
            StackPane flowerButton = new StackPane(flowerCircle, flowerLabel);
            StackPane vegButton = new StackPane(vegCircle, vegLabel);

            // Position the circle buttons vertically with more space between them
            treeButton.setLayoutX(circleX);
            treeButton.setLayoutY(trunkTop + 40);

            flowerButton.setLayoutX(circleX);
            flowerButton.setLayoutY(trunkTop + 40 + spacing);

            vegButton.setLayoutX(circleX);
            vegButton.setLayoutY(trunkTop + 40 + spacing * 2);

            // Create context menus
            ContextMenu treeMenu = createMenuForPlants(plantManager.getTrees());
            ContextMenu flowerMenu = createMenuForPlants(plantManager.getFlowers());
            ContextMenu vegMenu = createMenuForPlants(plantManager.getVegetables());

            // Style the context menus
            styleContextMenu(treeMenu);
            styleContextMenu(flowerMenu);
            styleContextMenu(vegMenu);

            // Add click handlers to the circle buttons with proper positioning of menus
            treeButton.setOnMouseClicked(e -> {
                // Position the menu to the left of the button
                treeMenu.show(treeButton, e.getScreenX() - 250, e.getScreenY());
            });

            flowerButton.setOnMouseClicked(e -> {
                // Position the menu to the left of the button
                flowerMenu.show(flowerButton, e.getScreenX() - 250, e.getScreenY());
            });

            vegButton.setOnMouseClicked(e -> {
                // Position the menu to the left of the button
                vegMenu.show(vegButton, e.getScreenX() - 250, e.getScreenY());
            });

            // Add visual feedback when hovering
            addEnhancedHoverEffect(treeButton, treeCircle);
            addEnhancedHoverEffect(flowerButton, flowerCircle);
            addEnhancedHoverEffect(vegButton, vegCircle);

            // Create horizontal branches connecting the circles to the trunk - adjusted positions
            javafx.scene.shape.Line topBranch = createBranch(circleX + 50, trunkTop + 40, trunkX, trunkTop + 40);
            javafx.scene.shape.Line middleBranch = createBranch(circleX + 50, trunkTop + 40 + spacing, trunkX, trunkTop + 40 + spacing);
            javafx.scene.shape.Line bottomBranch = createBranch(circleX + 50, trunkTop + 40 + spacing * 2, trunkX, trunkTop + 40 + spacing * 2);

            // Create extended ground hill that reaches the bottom of screen
            javafx.scene.shape.Arc ground = new javafx.scene.shape.Arc(
                    trunkX + 30, anchorHeight, // Position at the bottom of the screen
                    250, 100, // Increased height from 80 to 100
                    0, 180
            );
            ground.setType(javafx.scene.shape.ArcType.ROUND);

            // Gradient for ground
            javafx.scene.paint.LinearGradient groundGradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#7CFC00")),
                    new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#2E8B57"))
            );
            ground.setFill(groundGradient);
            ground.setStroke(javafx.scene.paint.Color.web("#228B22"));
            ground.setStrokeWidth(2);

            // Add shadow to ground
            ground.setEffect(new javafx.scene.effect.DropShadow(5, javafx.scene.paint.Color.rgb(0, 0, 0, 0.3)));

            // Add all elements to the scene
            anchorPane.getChildren().addAll(ground, trunk, topBranch, middleBranch, bottomBranch);
            anchorPane.getChildren().addAll(treeButton, flowerButton, vegButton);

            // Add decorative dots - adjusted to be on the hill
            addEnhancedDecorativeDots(trunkX + 30, anchorHeight - 10);

            log4jLogger.info("Adjusted vertical tree menu setup completed successfully");

        } catch (Exception e) {
            log4jLogger.error("Failed to set up vertical tree menu: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Circle createEnhancedCircleButton(String baseColor) {
        // Create a larger circle
        Circle circle = new Circle(60); // Increased size from 50 to 60

        // Create a radial gradient for a 3D effect
        javafx.scene.paint.RadialGradient gradient = new javafx.scene.paint.RadialGradient(
                0, 0, 0.3, 0.3, 0.7, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web(baseColor).brighter().brighter()),
                new javafx.scene.paint.Stop(0.8, javafx.scene.paint.Color.web(baseColor)),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web(baseColor).darker())
        );

        circle.setFill(gradient);

        // Add a thicker border
        circle.setStroke(javafx.scene.paint.Color.WHITE);
        circle.setStrokeWidth(4); // Increased from 3 to 4

        // Add a glow and drop shadow for depth
        javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.5));
        dropShadow.setRadius(12); // Increased from 10 to 12
        dropShadow.setOffsetX(4); // Increased from 3 to 4
        dropShadow.setOffsetY(4); // Increased from 3 to 4

        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.4); // Increased from 0.3 to 0.4
        glow.setInput(dropShadow);

        circle.setEffect(glow);

        return circle;
    }

    // Updated method to create enhanced button labels with larger font
    private Label createEnhancedButtonLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(javafx.scene.paint.Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 3, 0, 0, 0);");
        return label;
    }

    // Modified to create horizontal branches with better styling
    private javafx.scene.shape.Line createBranch(double startX, double startY, double endX, double endY) {
        javafx.scene.shape.Line branch = new javafx.scene.shape.Line(startX, startY, endX, endY);
        branch.setStrokeWidth(15); // Reduced from 18 to 15

        // Linear gradient for the branch
        javafx.scene.paint.LinearGradient branchGradient = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#A0522D")),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#8B4513"))
        );
        branch.setStroke(branchGradient);

        // Add a slight shadow
        branch.setEffect(new javafx.scene.effect.DropShadow(6, javafx.scene.paint.Color.rgb(0, 0, 0, 0.5)));

        // Set stroke line cap to round for better appearance
        branch.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        return branch;
    }

    // Modified to add more decorative elements on the hill
    private void addEnhancedDecorativeDots(double centerX, double baseY) {
        // Create decorative flowers/plants on the hill
        Random random = new Random();

        // Add more elements
        for (int i = 0; i < 12; i++) {
            double offsetX = random.nextDouble() * 500 - 250; // -250 to +250 (wider spread)
            double offsetY = random.nextDouble() * 50 - 10;  // -10 to +40 (covering hill area)

            // Create flower base/stem
            javafx.scene.shape.Line stem = new javafx.scene.shape.Line(
                    centerX + offsetX,
                    baseY + offsetY,
                    centerX + offsetX,
                    baseY + offsetY - random.nextInt(15) - 5 // Taller stems
            );
            stem.setStroke(javafx.scene.paint.Color.web("#228B22"));
            stem.setStrokeWidth(2);

            // Randomize flower colors
            String[] flowerColors = {
                    "#FF69B4", "#FF1493", "#FFFF00", "#FFA500", "#9370DB", "#FF6347", "#00FF7F", "#1E90FF"
            };
            String flowerColor = flowerColors[random.nextInt(flowerColors.length)];

            // Create flower/plant top - some slightly larger
            Circle flowerTop = new Circle(2 + random.nextInt(6)); // Size range 2-7
            flowerTop.setFill(javafx.scene.paint.Color.web(flowerColor));
            flowerTop.setCenterX(centerX + offsetX);
            flowerTop.setCenterY(baseY + offsetY - random.nextInt(15) - 8);

            // Add a subtle glow
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.3);
            flowerTop.setEffect(glow);

            anchorPane.getChildren().addAll(stem, flowerTop);
        }

        // Add a few grass blades
        for (int i = 0; i < 8; i++) {
            double offsetX = random.nextDouble() * 400 - 200; // -200 to +200

            javafx.scene.shape.Line grassBlade = new javafx.scene.shape.Line(
                    centerX + offsetX,
                    baseY,
                    centerX + offsetX + random.nextInt(11) - 5, // Slight angle
                    baseY - random.nextInt(15) - 5 // Height 5-20
            );

            grassBlade.setStroke(javafx.scene.paint.Color.web("#32CD32")); // Lime green
            grassBlade.setStrokeWidth(1.5);
            anchorPane.getChildren().add(grassBlade);
        }
    }

    private void styleContextMenu(ContextMenu menu) {
        menu.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.98); " + // More opaque
                        "-fx-background-radius: 20px; " + // Larger radius
                        "-fx-border-color: #CCCCCC; " +
                        "-fx-border-width: 2px; " + // Thicker border
                        "-fx-border-radius: 20px; " + // Matching radius
                        "-fx-padding: 15px; " + // More padding
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 5);" // Enhanced shadow
        );
    }

    // Create a styled context menu for plants
    private <T extends Plant> ContextMenu createMenuForPlants(List<T> plants) {
        ContextMenu menu = new ContextMenu();

        for (T plant : plants) {
            MenuItem item = new MenuItem(plant.getName());

            try {
                // Add image if available with larger size
                Image image = new Image(getClass().getResourceAsStream("/images/" + plant.getCurrentImage()), 40, 40, true, true);
                ImageView imageView = new ImageView(image);

                // Add a border around the image
                StackPane imageContainer = new StackPane();
                Rectangle border = new Rectangle(44, 44);
                border.setArcWidth(8);
                border.setArcHeight(8);
                border.setFill(javafx.scene.paint.Color.TRANSPARENT);
                border.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
                border.setStrokeWidth(2);

                imageContainer.getChildren().addAll(border, imageView);

                item.setGraphic(imageContainer);
            } catch (Exception e) {
                log4jLogger.warn("Could not load image for " + plant.getName());
            }

            // Style the menu item with larger font and better padding
            item.setStyle(
                    "-fx-padding: 10px 20px; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: normal; " +
                            "-fx-cursor: hand;"
            );

            // Add hover effect to menu item
            final String plantName = plant.getName();
            final String imagePath = plant.getCurrentImage();

            item.setOnAction(e -> {
                log4jLogger.info("Selected plant: " + plantName);
                addPlantToGrid(plantName, imagePath);
            });

            // Add to menu
            menu.getItems().add(item);
        }

        return menu;
    }
    // Helper method to create branches

    private void addEnhancedHoverEffect(StackPane button, Circle circle) {
        // Store original effect
        javafx.scene.effect.Effect originalEffect = circle.getEffect();

        // Create scale transitions
        ScaleTransition growTransition = new ScaleTransition(Duration.millis(200), button);
        growTransition.setToX(1.15);
        growTransition.setToY(1.15);

        ScaleTransition shrinkTransition = new ScaleTransition(Duration.millis(200), button);
        shrinkTransition.setToX(1.0);
        shrinkTransition.setToY(1.0);

        button.setOnMouseEntered(e -> {
            // Enhanced glow effect on hover
            javafx.scene.effect.Glow enhancedGlow = new javafx.scene.effect.Glow(0.8);
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.WHITE);
            shadow.setRadius(25);
            enhancedGlow.setInput(shadow);
            circle.setEffect(enhancedGlow);

            // Scale up
            growTransition.playFromStart();

            // Change cursor
            button.setCursor(javafx.scene.Cursor.HAND);
        });

        button.setOnMouseExited(e -> {
            // Restore original effect
            circle.setEffect(originalEffect);

            // Scale back
            shrinkTransition.playFromStart();

            // Restore cursor
            button.setCursor(javafx.scene.Cursor.DEFAULT);
        });
    }

    // Create a colored circle button
    private Circle createCircleButton(String colorCode) {
        Circle circle = new Circle(45);
        circle.setFill(javafx.scene.paint.Color.web(colorCode));

        // Add a shine effectrecent:///dc5095c21374a420b952eda067ccc8cf
        circle.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.WHITE));

        return circle;
    }

    // Add scale effect to a node
    private void addScaleEffect(Node node) {
        ScaleTransition growTransition = new ScaleTransition(Duration.millis(200), node);
        growTransition.setToX(1.1);
        growTransition.setToY(1.1);

        ScaleTransition shrinkTransition = new ScaleTransition(Duration.millis(200), node);
        shrinkTransition.setToX(1.0);
        shrinkTransition.setToY(1.0);

        node.setOnMouseEntered(e -> growTransition.playFromStart());
        node.setOnMouseExited(e -> shrinkTransition.playFromStart());
    }

    // Create a context menu for plants


    // Helper method to style the tree buttons
    private void styleTreeButton(Button button, String baseColor) {
        // Set size
        button.setPrefHeight(90.0);
        button.setPrefWidth(90.0);

        // Apply styling
        button.setStyle(
                "-fx-background-radius: 45px; " +
                        "-fx-background-color: linear-gradient(to bottom right, #FFFFFF30, " + baseColor + ", " + baseColor + "AA); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 0;"
        );

        // Add shadow effect
        button.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(255, 255, 255, 0.6)));

        // Add hover animation
        ScaleTransition growTransition = new ScaleTransition(Duration.millis(200), button);
        growTransition.setToX(1.1);
        growTransition.setToY(1.1);

        ScaleTransition shrinkTransition = new ScaleTransition(Duration.millis(200), button);
        shrinkTransition.setToX(1.0);
        shrinkTransition.setToY(1.0);

        button.setOnMouseEntered(e -> growTransition.playFromStart());
        button.setOnMouseExited(e -> shrinkTransition.playFromStart());
    }

    // Create a context menu for plant types
    private <T extends Plant> ContextMenu createPlantContextMenu(List<T> plants) {
        ContextMenu menu = new ContextMenu();

        for (T plant : plants) {
            MenuItem item = new MenuItem(plant.getName());

            try {
                // Add image if available
                Image image = new Image(getClass().getResourceAsStream("/images/" + plant.getCurrentImage()), 24, 24, true, true);
                ImageView imageView = new ImageView(image);
                item.setGraphic(imageView);
            } catch (Exception e) {
                log4jLogger.warn("Could not load image for " + plant.getName());
            }

            // Set action handler - use the existing addPlantToGrid method
            item.setOnAction(e -> addPlantToGrid(plant.getName(), plant.getCurrentImage()));

            // Add to menu
            menu.getItems().add(item);
        }

        // Style the context menu
        menu.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 5px;"
        );

        return menu;
    }

    // Helper method to create fresh menu buttons with consistent styling
    private MenuButton createFreshMenuButton(String text, String baseColor) {
        MenuButton button = new MenuButton(text);

        // Set size
        button.setPrefHeight(90.0);
        button.setPrefWidth(90.0);

        // Apply basic styling
        button.setStyle(
                "-fx-background-radius: 45px; " +
                        "-fx-background-color: linear-gradient(to bottom right, #FFFFFF30, " + baseColor + ", " + baseColor + "AA); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 0;"
        );

        // Add shadow effect
        button.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(255, 255, 255, 0.6)));

        // Set critical properties for proper functionality
        button.setContentDisplay(ContentDisplay.CENTER);
        button.setPickOnBounds(true);
        button.setMouseTransparent(false);
        button.setFocusTraversable(true);

        // Add hover animation
        ScaleTransition growTransition = new ScaleTransition(Duration.millis(200), button);
        growTransition.setToX(1.1);
        growTransition.setToY(1.1);

        ScaleTransition shrinkTransition = new ScaleTransition(Duration.millis(200), button);
        shrinkTransition.setToX(1.0);
        shrinkTransition.setToY(1.0);

        button.setOnMouseEntered(e -> growTransition.playFromStart());
        button.setOnMouseExited(e -> shrinkTransition.playFromStart());

        // Set popup direction based on position
        if (text.equals("Tree")) {
            button.setPopupSide(Side.RIGHT);
        } else if (text.equals("Flower")) {
            button.setPopupSide(Side.LEFT);
        } else if (text.equals("Veg")) {
            button.setPopupSide(Side.RIGHT);
        }

        return button;
    }

    // Helper method to populate menu buttons with plant items
    private <T extends Plant> void populateMenuWithPlants(MenuButton button, List<T> plants) {
        for (T plant : plants) {
            MenuItem menuItem = new MenuItem(plant.getName());

            try {
                // Add image if available
                Image image = new Image(getClass().getResourceAsStream("/images/" + plant.getCurrentImage()), 24, 24, true, true);
                ImageView imageView = new ImageView(image);
                menuItem.setGraphic(imageView);
            } catch (Exception e) {
                log4jLogger.warn("Could not load image for " + plant.getName());
            }

            // Set action handler
            menuItem.setOnAction(e -> addPlantToGrid(plant.getName(), plant.getCurrentImage()));

            // Add to menu
            button.getItems().add(menuItem);
        }
    }

// Updated setupSimpleTreeMenu with addi

    private void addDecorativeDots(double centerX, double baseY) {
        // Create a few small green dots on the ground
        int numDots = 5;
        Random random = new Random();

        for (int i = 0; i < numDots; i++) {
            double offsetX = random.nextDouble() * 200 - 100; // -100 to +100
            double offsetY = random.nextDouble() * 30 + 20;   // +20 to +50 (lower on hill)

            Circle dot = new Circle(4);
            dot.setFill(javafx.scene.paint.Color.web("#ffecf2")); // Darker green
            dot.setCenterX(centerX + offsetX);
            dot.setCenterY(baseY + offsetY);

            anchorPane.getChildren().add(dot);
        }
    }

// Keep your existing addHoverAnimation method

    // Update the hover animation to be smoother for the cartoon style
    private void addHoverAnimation(MenuButton button) {
        ScaleTransition growTransition = new ScaleTransition(Duration.millis(200), button);
        growTransition.setToX(1.1);
        growTransition.setToY(1.1);

        ScaleTransition shrinkTransition = new ScaleTransition(Duration.millis(200), button);
        shrinkTransition.setToX(1.0);
        shrinkTransition.setToY(1.0);

        // Add a slight bounce effect
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 15, 0.7, -5, -5);");
            growTransition.playFromStart();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 15, 0.7, -5, -5);",
                    "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.6), 10, 0.5, -5, -5);"));
            shrinkTransition.playFromStart();
        });
    }

    // Update the setupCircularMenuButtons method to ensure text is visible
//    private void setupCircularMenuButtons() {
//        // Add tooltips to menu buttons
//        Tooltip.install(treeMenuButton, new Tooltip("Add Trees to your garden"));
//        Tooltip.install(flowerMenuButton, new Tooltip("Add Flowers to your garden"));
//        Tooltip.install(vegetableMenuButton, new Tooltip("Add Vegetables to your garden"));
//
//        // Set the popup direction for each menu button
//        treeMenuButton.setPopupSide(Side.RIGHT);
//        flowerMenuButton.setPopupSide(Side.LEFT);
//        vegetableMenuButton.setPopupSide(Side.BOTTOM);
//
//        // Ensure text is centered and visible
//        treeMenuButton.setContentDisplay(ContentDisplay.CENTER);
//        flowerMenuButton.setContentDisplay(ContentDisplay.CENTER);
//        vegetableMenuButton.setContentDisplay(ContentDisplay.CENTER);
//
//        // Add hover animations
//        addHoverAnimation(treeMenuButton);
//        addHoverAnimation(flowerMenuButton);
//        addHoverAnimation(vegetableMenuButton);
//
//        // Add styling for context menus
//        styleContextMenus();
//    }

//    private void addHoverAnimation(MenuButton button) {
//        ScaleTransition growTransition = new ScaleTransition(Duration.millis(150), button);
//        growTransition.setToX(1.1);
//        growTransition.setToY(1.1);
//
//        ScaleTransition shrinkTransition = new ScaleTransition(Duration.millis(150), button);
//        shrinkTransition.setToX(1.0);
//        shrinkTransition.setToY(1.0);
//
//        button.setOnMouseEntered(e -> growTransition.playFromStart());
//        button.setOnMouseExited(e -> shrinkTransition.playFromStart());
//    }

    private void styleContextMenus() {
        // Apply styling to dropdown menus when they appear
        setupContextMenuStyle(treeMenuButton);
        setupContextMenuStyle(flowerMenuButton);
        setupContextMenuStyle(vegetableMenuButton);
    }

    private void setupContextMenuStyle(MenuButton button) {
        button.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.getContextMenu().setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                "-fx-background-radius: 15px; " +
                                "-fx-padding: 8px; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);"
                );
            }
        });
    }

    private void loadStyledPlantsData() {
        // Clear existing items
        treeMenuButton.getItems().clear();
        flowerMenuButton.getItems().clear();
        vegetableMenuButton.getItems().clear();

        // Load trees with styled menu items
        for (Tree tree : plantManager.getTrees()) {
            MenuItem menuItem = createStyledMenuItem(tree.getName(), tree.getCurrentImage());
            menuItem.setOnAction(e -> addPlantToGrid(tree.getName(), tree.getCurrentImage()));
            treeMenuButton.getItems().add(menuItem);
        }

        // Load flowers with styled menu items
        for (Flower flower : plantManager.getFlowers()) {
            System.out.println("Adding flower: " + flower.getName());
            MenuItem menuItem = createStyledMenuItem(flower.getName(), flower.getCurrentImage());
            menuItem.setOnAction(e -> addPlantToGrid(flower.getName(), flower.getCurrentImage()));
            flowerMenuButton.getItems().add(menuItem);
        }
        System.out.println("Flower menu items count: " + flowerMenuButton.getItems().size());
        for (MenuItem item : flowerMenuButton.getItems()) {
            System.out.println("Flower menu item: " + item.getText());
        }
        // Load vegetables with styled menu items
        for (Vegetable vegetable : plantManager.getVegetables()) {
            MenuItem menuItem = createStyledMenuItem(vegetable.getName(), vegetable.getCurrentImage());
            menuItem.setOnAction(e -> addPlantToGrid(vegetable.getName(), vegetable.getCurrentImage()));
            vegetableMenuButton.getItems().add(menuItem);
        }
    }

    private MenuItem createStyledMenuItem(String name, String imagePath) {
        MenuItem menuItem = new MenuItem(name);

        try {
            // Try to load an image
            Image image = new Image(getClass().getResourceAsStream("/images/" + imagePath), 24, 24, true, true);
            ImageView imageView = new ImageView(image);
            menuItem.setGraphic(imageView);
        } catch (Exception e) {
            // If image loading fails, just use text
            log4jLogger.error("Failed to load image for menu item: " + name);
        }

        // Style the menu item
        menuItem.setStyle("-fx-padding: 8px 12px; -fx-font-size: 14px;");

        return menuItem;
    }

//    private void createColoredGrid(GridPane gridPane, int numRows, int numCols) {
//        double cellWidth = 80;  // Width of each cell
//        double cellHeight = 80; // Height of each cell
//
//        // Loop through rows and columns to create cells
//        for (int row = 0; row < numRows; row++) {
//            for (int col = 0; col < numCols; col++) {
//                // Create a StackPane for each cell
//                StackPane cell = new StackPane();
//
//                // Set preferred size of the cell
//                cell.setPrefSize(cellWidth, cellHeight);
//
//                // Set a softer, semi-transparent fill for the grid cells
//                cell.setStyle(
//                        "-fx-background-color: rgba(220, 255, 220, 0.7); " + // Light green with 70% opacity
//                                "-fx-background-radius: 5px; " + // Slightly rounded corners
//                                "-fx-border-color: rgba(139, 69, 19, 0.5); " + // Brown border with 50% opacity
//                                "-fx-border-width: 1.5px; " + // Thinner border
//                                "-fx-border-radius: 5px;" // Matching rounded corners for border
//                );
//
//                // Add the cell to the GridPane
//                gridPane.add(cell, col, row);
//            }
//        }
//
//        // Add spacing between cells
//        gridPane.setHgap(3); // Horizontal gap
//        gridPane.setVgap(3); // Vertical gap
//    }
private void createSimpleGradientGrid(GridPane gridPane, int numRows, int numCols) {
    // Colors without any quotes issues
    javafx.scene.paint.Color[] colors = {
            javafx.scene.paint.Color.web("#e8f5e9"),  // Very light green
            javafx.scene.paint.Color.web("#c8e6c9"),  // Light green
            javafx.scene.paint.Color.web("#a5d6a7"),  // Medium light green
            javafx.scene.paint.Color.web("#81c784")   // Medium green
    };

    for (int row = 0; row < numRows; row++) {
        for (int col = 0; col < numCols; col++) {
            int colorIndex = (row + col) % colors.length;

            StackPane cell = new StackPane();
            cell.setPrefSize(80, 80);

            // Create a background color with rounded corners
            javafx.scene.layout.BackgroundFill backgroundFill = new javafx.scene.layout.BackgroundFill(
                    colors[colorIndex],
                    new CornerRadii(6),
                    Insets.EMPTY
            );

            // Apply the background
            cell.setBackground(new Background(backgroundFill));

            // Add a border
            cell.setBorder(new Border(new BorderStroke(
                    javafx.scene.paint.Color.rgb(76, 175, 80, 0.3),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(6),
                    new BorderWidths(1)
            )));

            // Add a drop shadow effect
            javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
            dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.1));
            dropShadow.setRadius(2);
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(1);
            cell.setEffect(dropShadow);

            gridPane.add(cell, col, row);
        }
    }

    gridPane.setHgap(3);
    gridPane.setVgap(3);
    gridPane.setPadding(new Insets(5, 5, 5, 5));

    // Apply styling to the grid without using CSS strings (avoids errors)
    gridPane.setBackground(new Background(new BackgroundFill(
            javafx.scene.paint.Color.rgb(255, 255, 255, 0.4),
            new CornerRadii(10),
            Insets.EMPTY
    )));

    gridPane.setBorder(new Border(new BorderStroke(
            javafx.scene.paint.Color.rgb(76, 175, 80, 0.5),
            BorderStrokeStyle.SOLID,
            new CornerRadii(10),
            new BorderWidths(2)
    )));

    javafx.scene.effect.DropShadow gridShadow = new javafx.scene.effect.DropShadow();
    gridShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2));
    gridShadow.setRadius(8);
    gridShadow.setOffsetX(0);
    gridShadow.setOffsetY(2);
    gridPane.setEffect(gridShadow);
}
//    private void createEnhancedGrid(GridPane gridPane, int numRows, int numCols) {
//        // Colors array without any CSS issues
//        String[] baseColors = {
//                "#e8f5e9", // Very light green
//                "#c8e6c9", // Light green
//                "#a5d6a7", // Medium light green
//                "#81c784"  // Medium green
//        };
//
//        for (int row = 0; row < numRows; row++) {
//            for (int col = 0; col < numCols; col++) {
//                int colorIndex = (row + col) % baseColors.length;
//
//                StackPane cell = new StackPane();
//                cell.setPrefSize(80, 80);
//
//                // Fixed style string - no quotes around color values within the style
//                cell.setStyle(
//                        "-fx-background-color: " + baseColors[colorIndex] + "; " +
//                                "-fx-background-radius: 6px; " +
//                                "-fx-border-color: rgba(76, 175, 80, 0.3); " +
//                                "-fx-border-width: 1px; " +
//                                "-fx-border-radius: 6px; " +
//                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);"
//                );
//
//                gridPane.add(cell, col, row);
//            }
//        }
//
//        // Rest of the method...
//    }
    // Add this method to remove the orange Veg label from top left
    private void removeTopLeftVeg() {
        Platform.runLater(() -> {
            // Find all labels or HBoxes in the top-left corner
            for (Node node : anchorPane.getChildren()) {
                // Check for direct labels
                if (node instanceof Label) {
                    Label label = (Label) node;
                    // Check if it contains "Veg" text and is in the top-left corner
                    if ("Veg".equals(label.getText()) &&
                            node.getLayoutX() < 50 && node.getLayoutY() < 50) {
                        anchorPane.getChildren().remove(node);
                        log4jLogger.info("Found and removed top-left Veg label");
                        break;
                    }
                }

                // Check for VBox that might contain the Veg label
                if (node instanceof VBox && node.getLayoutY() < 100) {
                    VBox vbox = (VBox) node;
                    for (Node child : vbox.getChildren()) {
                        if (child instanceof Label && "Veg".equals(((Label) child).getText())) {
                            vbox.getChildren().remove(child);
                            log4jLogger.info("Removed Veg label from VBox");
                            break;
                        }
                        if (child instanceof Text && "Veg".equals(((Text) child).getText())) {
                            vbox.getChildren().remove(child);
                            log4jLogger.info("Removed Veg text from VBox");
                            break;
                        }
                    }
                }

                // Check for HBox that might contain the Veg label
                if (node instanceof HBox && node.getLayoutY() < 100) {
                    HBox hbox = (HBox) node;
                    for (Node child : hbox.getChildren()) {
                        if (child instanceof Label && "Veg".equals(((Label) child).getText())) {
                            hbox.getChildren().remove(child);
                            log4jLogger.info("Removed Veg label from HBox");
                            break;
                        }
                    }
                }
            }

            // Also check for directly added Veg text (as seen in your FXML)
            List<Node> toRemove = new ArrayList<>();
            for (Node node : anchorPane.getChildren()) {
                if (node instanceof Text) {
                    Text text = (Text) node;
                    if ("Veg".equals(text.getText()) || "WEATHER".equals(text.getText())) {
                        if (node.getLayoutX() < 50 && node.getLayoutY() < 50) {
                            toRemove.add(node);
                        }
                    }
                }
            }

            anchorPane.getChildren().removeAll(toRemove);
        });
    }
}