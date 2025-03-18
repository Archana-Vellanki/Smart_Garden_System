package com.example.ooad_project;

import com.example.ooad_project.API.SmartGardenAPI;
import com.example.ooad_project.Events.*;
import com.example.ooad_project.Parasite.Parasite;
import com.example.ooad_project.Parasite.ParasiteManager;
import com.example.ooad_project.Plant.Children.Flower;
import com.example.ooad_project.Plant.Plant;
import com.example.ooad_project.Plant.Children.Tree;
import com.example.ooad_project.Plant.Children.Vegetable;
import com.example.ooad_project.Plant.PlantManager;
import com.example.ooad_project.ThreadUtils.EventBus;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GardenUIController {


    @FXML
    private Label currentDay;

//    @FXML
//    private MenuButton parasiteMenuButton;

//    @FXML
//    private Button pestTestButton;

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
    DayUpdateEvent dayChangeEvent;


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
    private HBox parasiteStatusContainer;
    private TranslateTransition parasiteStatusAnimation;

    private PathTransition parasitePathTransition;
    private Label pesticideStatusLabel;
    private ProgressBar pesticideLevelBar;

    private Timeline pesticideStatusAnimation;


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


//    @FXML
//    private TextArea logTextArea;

    private static final Logger logger = LogManager.getLogger("GardenUIControllerLogger");


    @FXML
    public void getPLantButtonPressed() {
        SmartGardenAPI api = new SmartGardenAPI();
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
        // Place this after showNoParasites()
        //  Platform.runLater(this::animateParasiteStatus);
        //  setupParasiteAnimation();
        // Remove the unwanted Veg label in top left
        PauseTransition delay1 = new PauseTransition(Duration.millis(500));
        delay1.setOnFinished(e -> {
            // Initialize any delayed elements
            //  initializeParasiteStatusWithGround();

            // Add our simple pesticide system box
            createEnhancedVerticalPesticideBox();
        });
        delay1.play();

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
        //   flowerMenuButton.setPickOnBounds(true);/home/hp/Downloads/back2.png


//         Load the background image
//         Load the background image
        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/rrr.png"));


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
        EventBus.subscribe("ParasiteDisplayEvent", event -> handleDisplayParasiteEvent((ParasiteDisplayEvent) event));
        EventBus.subscribe("PlantImageUpdateEvent",
                event -> handlePlantImageUpdateEvent((PlantImageUpdateEvent) event));
        EventBus.subscribe("DayUpdateEvent", event -> handleDayChangeEvent((DayUpdateEvent) event));
        EventBus.subscribe("TemperatureEvent", event -> changeTemperatureUI((TemperatureEvent) event));
        EventBus.subscribe("ParasiteEvent", event -> changeParasiteUI((ParasiteEvent) event));

//      Gives you row, col and waterneeded
        EventBus.subscribe("SprinklerEvent", event -> handleSprinklerEvent((SprinklerEvent) event));


//        When plant is cooled by x
        EventBus.subscribe("CoolTemperatureEvent", event -> handleTemperatureCoolEvent((CoolTemperatureEvent) event));

//      When plant is heated by x
        EventBus.subscribe("HeatTemperatureEvent", event -> handleTemperatureHeatEvent((HeatTemperatureEvent) event));

//        When plant is damaged by x
//        Includes -> row, col, damage
        EventBus.subscribe("ParasiteDamageEvent", event -> handleParasiteDamageEvent((ParasiteDamageEvent) event));

        EventBus.subscribe("InitializeGarden", event -> handleInitializeGarden());

//        Event whenever there is change to plants health
        EventBus.subscribe("PlantHealthUpdateEvent", event -> handlePlantHealthUpdateEvent((PlantHealthUpdateEvent) event));

        EventBus.subscribe("PlantDeathUIChangeEvent", event -> handlePlantDeathUIChangeEvent((Plant) event));
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> initializeParasiteStatusWithGround());
        delay.play();
        EventBus.subscribe("PesticideApplicationEvent", event -> {
            if (event instanceof PesticideApplicationEvent) {
                PesticideApplicationEvent pestEvent = (PesticideApplicationEvent) event;
                showSimplePesticideSpray(pestEvent.getRow(), pestEvent.getCol());

                // Decrease pesticide level slightly with each use
                if (pesticideLevelBar != null) {
                    double currentLevel = pesticideLevelBar.getProgress();
                    updatePesticideLevel(currentLevel - 0.05);
                }
            }
        });

    }

    // Start rain animation
    private void startRainAnimation() {

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
        Platform.runLater(() -> {
            int row = plant.getRow();
            int col = plant.getCol();
            System.out.println("🪦The dead plant was removed from the garden (" + row + "," + col + ")");

            boolean removed = gridPane.getChildren().removeIf(node -> {
                Integer nodeRow = GridPane.getRowIndex(node);
                Integer nodeCol = GridPane.getColumnIndex(node);
                boolean match = nodeRow != null && nodeCol != null && nodeRow == row && nodeCol == col;
                //if (match) System.out.println("✅ Removed UI node at (" + row + "," + col + ")");
                return match;
            });

            if (!removed) {
                System.out.println("❌ No UI node found at (" + row + "," + col + ")");
            }
        });

    }

    private void handlePlantHealthUpdateEvent(PlantHealthUpdateEvent event) {
        logger.info("Day: " + logDay + " Plant health updated at row " + event.getRow() + " and column " + event.getCol() + " from " + event.getOldHealth() + " to " + event.getNewHealth());
//        System.out.println("Plant health updated at row " + event.getRow() + " and column " + event.getCol() + " from " + event.getOldHealth() + " to " + event.getNewHealth());
    }

    private void handleInitializeGarden() {
        // Hard-coded positions for plants as specified in the layout
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
    // Replace your existing label removal methods with these fixed versions

    private void removeVegLabelById() {
        Platform.runLater(() -> {
            // Look for any element with the ID 'vegLabel' or similar
            List<Node> nodesToRemove = new ArrayList<>();

            for (Node node : anchorPane.getChildren()) {
                if (node.getId() != null &&
                        (node.getId().contains("veg") || node.getId().contains("Veg"))) {
                    node.setVisible(false); // Hide it
                    nodesToRemove.add(node); // Mark for removal
                }
            }

            // Remove all marked nodes
            anchorPane.getChildren().removeAll(nodesToRemove);

            // Another approach is to overlay a white rectangle on top of it
            Rectangle coverRect = new Rectangle(0, 0, 50, 50);
            coverRect.setFill(javafx.scene.paint.Color.WHITE);
            coverRect.setOpacity(0);  // Setting to 0 to make it invisible
            anchorPane.getChildren().add(coverRect);
        });
    }

    private void removeFlowerLabelById() {
        Platform.runLater(() -> {
            // Look for any element with the ID 'flowerLabel' or similar
            List<Node> nodesToRemove = new ArrayList<>();

            for (Node node : anchorPane.getChildren()) {
                if (node.getId() != null &&
                        (node.getId().contains("flower") || node.getId().contains("Flower"))) {
                    node.setVisible(false); // Hide it
                    nodesToRemove.add(node); // Mark for removal
                }
            }

            // Remove all marked nodes
            anchorPane.getChildren().removeAll(nodesToRemove);

            // Another approach is to overlay a white rectangle on top of it
            Rectangle coverRect = new Rectangle(0, 0, 50, 50);
            coverRect.setFill(javafx.scene.paint.Color.WHITE);
            coverRect.setOpacity(0);  // Setting to 0 to make it invisible
            anchorPane.getChildren().add(coverRect);
        });
    }

    private void removeTreeLabelById() {
        Platform.runLater(() -> {
            // Look for any element with the ID 'treeLabel' or similar
            List<Node> nodesToRemove = new ArrayList<>();

            for (Node node : anchorPane.getChildren()) {
                if (node.getId() != null &&
                        (node.getId().contains("tree") || node.getId().contains("Tree"))) {
                    node.setVisible(false); // Hide it
                    nodesToRemove.add(node); // Mark for removal
                }
            }

            // Remove all marked nodes
            anchorPane.getChildren().removeAll(nodesToRemove);

            // Another approach is to overlay a white rectangle on top of it
            Rectangle coverRect = new Rectangle(0, 0, 50, 50);
            coverRect.setFill(javafx.scene.paint.Color.WHITE);
            coverRect.setOpacity(0);  // Setting to 0 to make it invisible
            anchorPane.getChildren().add(coverRect);
        });
    }

    // Also, fix the removeTopLeftVeg method
    private void removeTopLeftVeg() {
        Platform.runLater(() -> {
            // Find all labels or HBoxes in the top-left corner
            List<Node> nodesToRemove = new ArrayList<>();

            for (Node node : anchorPane.getChildren()) {
                // Check for direct labels
                if (node instanceof Label) {
                    Label label = (Label) node;
                    // Check if it contains "Veg" text and is in the top-left corner
                    if ("Veg".equals(label.getText()) &&
                            node.getLayoutX() < 50 && node.getLayoutY() < 50) {
                        node.setVisible(false);
                        nodesToRemove.add(node);
                        log4jLogger.info("Found and removed top-left Veg label");
                    }
                }

                // Check for nodes in VBox
                if (node instanceof VBox && node.getLayoutY() < 100) {
                    VBox vbox = (VBox) node;
                    List<Node> vboxNodesToRemove = new ArrayList<>();

                    for (Node child : vbox.getChildren()) {
                        if (child instanceof Label && "Veg".equals(((Label) child).getText())) {
                            child.setVisible(false);
                            vboxNodesToRemove.add(child);
                            log4jLogger.info("Removed Veg label from VBox");
                        }
                        if (child instanceof Text && "Veg".equals(((Text) child).getText())) {
                            child.setVisible(false);
                            vboxNodesToRemove.add(child);
                            log4jLogger.info("Removed Veg text from VBox");
                        }
                    }

                    vbox.getChildren().removeAll(vboxNodesToRemove);
                }

                // Check for nodes in HBox
                if (node instanceof HBox && node.getLayoutY() < 100) {
                    HBox hbox = (HBox) node;
                    List<Node> hboxNodesToRemove = new ArrayList<>();

                    for (Node child : hbox.getChildren()) {
                        if (child instanceof Label && "Veg".equals(((Label) child).getText())) {
                            child.setVisible(false);
                            hboxNodesToRemove.add(child);
                            log4jLogger.info("Removed Veg label from HBox");
                        }
                    }

                    hbox.getChildren().removeAll(hboxNodesToRemove);
                }
            }

            // Also check for directly added Veg text
            for (Node node : anchorPane.getChildren()) {
                if (node instanceof Text) {
                    Text text = (Text) node;
                    if ("Veg".equals(text.getText()) || "WEATHER".equals(text.getText())) {
                        if (node.getLayoutX() < 50 && node.getLayoutY() < 50) {
                            nodesToRemove.add(node);
                        }
                    }
                }
            }

            // Remove all nodes marked for removal
            anchorPane.getChildren().removeAll(nodesToRemove);
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

    private void handleTemperatureHeatEvent(HeatTemperatureEvent event) {

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

    private void handleTemperatureCoolEvent(CoolTemperatureEvent event) {

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

    public void handleDayChangeEvent(DayUpdateEvent event) {

        logger.info("Day: " + logDay + " Day changed to: " + event.getDay());
        dayChangeEvent = event;
        Platform.runLater(() -> {
            logDay = event.getDay();
            currentDay.setText("Day: " + event.getDay());
        });
    }

    private void handlePlantImageUpdateEvent(PlantImageUpdateEvent event) {

        logger.info("Day: " + logDay + " Plant image updated at row " + event.getPlant().getRow() + " and column "
                + event.getPlant().getCol() + " to " + event.getPlant().getCurrentImage());

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
                // Create a VBox to stack the letters vertically
                VBox textVBox = new VBox(5); // 5 pixels spacing between letters
                textVBox.setAlignment(Pos.CENTER);

                // Create individual letters
                String[] letters = {"A", "d", "d"};

                for (String letter : letters) {
                    Text letterText = new Text(letter);

                    // Style each letter
                    letterText.setFill(javafx.scene.paint.Color.WHITE);
                    letterText.setStroke(javafx.scene.paint.Color.rgb(70, 40, 20, 0.9)); // Brown stroke
                    letterText.setStrokeWidth(1.5);
                    letterText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));

                    // Add shadow for better visibility
                    javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
                    shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
                    shadow.setRadius(3);
                    shadow.setOffsetX(2);
                    shadow.setOffsetY(2);
                    letterText.setEffect(shadow);

                    // Add letter to the VBox
                    textVBox.getChildren().add(letterText);
                }

                // Position the text on the trunk
                // Note: Adjust these coordinates to match your trunk's position
                double trunkX = anchorPane.getWidth() - 63; // Position on the right side like in your screenshot
                double trunkY = 250; // Upper part of the trunk

                textVBox.setLayoutX(trunkX);
                textVBox.setLayoutY(trunkY);

                // Add the VBox to the 8
                anchorPane.getChildren().add(textVBox);

                // Add a subtle pulsing animation to draw attention
                javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(
                        Duration.millis(1500), textVBox);
                scaleTransition.setFromX(1.0);
                scaleTransition.setFromY(1.0);
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.setCycleCount(javafx.animation.Animation.INDEFINITE);
                scaleTransition.setAutoReverse(true);
                scaleTransition.play();

                // Make the text clickable
                textVBox.setOnMouseClicked(event -> {
                    // You can add code here to show your plant selection menu
                    logger.info("ADD ME text clicked, could show plant selection menu here");
                });

                // Add hover effect to change letter colors on hover
                textVBox.setOnMouseEntered(event -> {
                    textVBox.setCursor(javafx.scene.Cursor.HAND);

                    // Change all letters to yellow on hover
                    for (Node node : textVBox.getChildren()) {
                        if (node instanceof Text) {
                            ((Text) node).setFill(javafx.scene.paint.Color.YELLOW);
                        }
                    }
                });

                textVBox.setOnMouseExited(event -> {
                    textVBox.setCursor(javafx.scene.Cursor.DEFAULT);

                    // Change back to white when not hovering
                    for (Node node : textVBox.getChildren()) {
                        if (node instanceof Text) {
                            ((Text) node).setFill(javafx.scene.paint.Color.WHITE);
                        }
                    }
                });

            } catch (Exception e) {
                logger.error("Error adding vertical stacked text to trunk: " + e.getMessage());
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
            rainStatusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #a81c07; -fx-font-weight: bold;");
        });
    }

    // New method to start sun animation
    private void startSunAnimation() {
        // Define sun orbit radius and center position
        final double orbitRadius = 50;
        final double centerX = 130; // Center of orbit
        final double centerY = 150; // Center of orbit

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
            rainStatusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #4B9CD3; -fx-font-weight: bold;");

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
            // Create a VBox to hold the temperature elements
            VBox tempBox = new VBox(10);
            tempBox.setAlignment(Pos.CENTER);

            int temp = event.getAmount();
            javafx.scene.paint.Color bgColor, borderColor, textColor;
            String imageName;

            // Set colors and image based on temperature
            if (temp <= 50) {
                // Cold temperature
                bgColor = javafx.scene.paint.Color.rgb(210, 230, 255, 0.85);  // Light blue
                borderColor = javafx.scene.paint.Color.rgb(70, 130, 180, 0.7);  // Steel blue
                textColor = javafx.scene.paint.Color.rgb(0, 0, 139);  // Dark blue
                imageName = "coldTemperature.png";
            } else if (temp >= 60) {
                // Hot temperature
                bgColor = javafx.scene.paint.Color.rgb(255, 222, 222, 0.85);  // Light red
                borderColor = javafx.scene.paint.Color.rgb(220, 20, 60, 0.7);  // Crimson
                textColor = javafx.scene.paint.Color.rgb(139, 0, 0);  // Dark red
                imageName = "hotTemperature.png";
            } else {
                // Optimal temperature
                bgColor = javafx.scene.paint.Color.rgb(220, 255, 220, 0.85);  // Light green
                borderColor = javafx.scene.paint.Color.rgb(50, 205, 50, 0.7);  // Lime green
                textColor = javafx.scene.paint.Color.rgb(0, 100, 0);  // Dark green
                imageName = "normalTemperature.png";
            }

            // Add a background with rounded corners
            tempBox.setBackground(new Background(new BackgroundFill(
                    bgColor,
                    new CornerRadii(15),
                    Insets.EMPTY
            )));

            // Add a border
            tempBox.setBorder(new Border(new BorderStroke(
                    borderColor,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(15),
                    new BorderWidths(2)
            )));

            // Add padding
            tempBox.setPadding(new Insets(10));

            // Create an ImageView for the temperature icon
            Image tempImage = new Image(getClass().getResourceAsStream("/images/Temperature/" + imageName));
            ImageView tempImageView = new ImageView(tempImage);
            tempImageView.setFitHeight(80);
            tempImageView.setFitWidth(20);

            // Create a label for temperature text
            Label tempLabel = new Label(temp + "°F");
            tempLabel.setFont(new Font("System Bold", 18));
            tempLabel.setTextFill(textColor);

            // Add the components to the VBox
            tempBox.getChildren().addAll(tempImageView, tempLabel);

            // Add a drop shadow to the whole temperature display
            javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
            dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
            dropShadow.setRadius(10);
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(5);
            tempBox.setEffect(dropShadow);

            // Set the VBox as the graphic
            temperatureStatusLabel.setGraphic(tempBox);
            temperatureStatusLabel.setText("");  // Clear the text since we're using the graphic

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
            // Create a VBox to hold the temperature elements
            VBox tempBox = new VBox(10);
            tempBox.setAlignment(Pos.CENTER);

            // Add a background with rounded corners - green for optimal
            tempBox.setBackground(new Background(new BackgroundFill(
                    javafx.scene.paint.Color.rgb(220, 255, 220, 0.85),  // Light green with transparency
                    new CornerRadii(15),
                    Insets.EMPTY
            )));

            // Add a border
            tempBox.setBorder(new Border(new BorderStroke(
                    javafx.scene.paint.Color.rgb(50, 205, 50, 0.7),  // Lime green border
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(15),
                    new BorderWidths(2)
            )));

            // Add padding
            tempBox.setPadding(new Insets(10));

            // Create an ImageView for the optimal temperature icon
            Image optimalImage = new Image(getClass().getResourceAsStream("/images/Temperature/normalTemperature.png"));
            ImageView optimalImageView = new ImageView(optimalImage);
            optimalImageView.setFitHeight(100);
            optimalImageView.setFitWidth(30);

            // Create a label for "Optimal" text
            Label optimalLabel = new Label("Optimal");
            optimalLabel.setFont(new Font("System Bold", 22));
            optimalLabel.setTextFill(javafx.scene.paint.Color.rgb(0, 100, 0));  // Dark green text

            // Add the components to the VBox
            tempBox.getChildren().addAll(optimalImageView, optimalLabel);

            // Add a drop shadow to the whole temperature display
            javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
            dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
            dropShadow.setRadius(10);
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(5);
            tempBox.setEffect(dropShadow);

            // Set the VBox as the graphic
            temperatureStatusLabel.setGraphic(tempBox);
            temperatureStatusLabel.setText("");  // Clear the text since we're using the graphic
        });
    }

    private void changeParasiteUI(ParasiteEvent event) {
        logger.info("Day: " + logDay + " Parasite event triggered: " + event.getParasite().getName());

        Platform.runLater(() -> {
            try {
                // Determine which image to use
                String parasiteImagePath = "/images/Parasites/noParasite.png";

                if (Objects.equals(event.getParasite().getName(), "Slugs")) {
                    parasiteImagePath = "/images/Parasites/slugDetected.png";
                } else if (Objects.equals(event.getParasite().getName(), "Crow")) {
                    parasiteImagePath = "/images/Parasites/crowDetected.png";
                } else if (Objects.equals(event.getParasite().getName(), "Locust")) {
                    parasiteImagePath = "/images/Parasites/locustDetected.png";
                } else if (Objects.equals(event.getParasite().getName(), "Aphids")) {
                    parasiteImagePath = "/images/Parasites/aphidsDetected.png";
                } else if (Objects.equals(event.getParasite().getName(), "Rat")) {
                    parasiteImagePath = "/images/Parasites/ratDetected.png";
                } else if (Objects.equals(event.getParasite().getName(), "Parasite")) {
                    parasiteImagePath = "/images/Parasites/parasiteDetected.png";
                }

                // Find or create the status label
                if (parasiteStatusLabel == null) {
                    createParasiteStatusWithPathAnimation();
                }

                if (parasiteStatusLabel != null) {
                    // Load the parasite image
                    Image parasiteImage = new Image(getClass().getResourceAsStream(parasiteImagePath));
                    ImageView iconView = new ImageView(parasiteImage);
                    iconView.setFitHeight(24);
                    iconView.setFitWidth(24);

                    // Update the label
                    parasiteStatusLabel.setText(event.getParasite().getName() + " detected");
                    parasiteStatusLabel.setGraphic(iconView);
                    parasiteStatusLabel.setTextFill(Color.rgb(180, 0, 0));

                    // Update style for alert state
                    parasiteStatusLabel.setBackground(new Background(new BackgroundFill(
                            Color.rgb(255, 240, 240, 0.9),
                            new CornerRadii(20),
                            Insets.EMPTY
                    )));

                    parasiteStatusLabel.setBorder(new Border(new BorderStroke(
                            Color.rgb(230, 0, 0, 0.7),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(20),
                            new BorderWidths(2)
                    )));

                    // Add pulsing animation
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(300), parasiteStatusLabel);
                    pulse.setFromX(1.0);
                    pulse.setFromY(1.0);
                    pulse.setToX(1.1);
                    pulse.setToY(1.1);
                    pulse.setCycleCount(6);
                    pulse.setAutoReverse(true);
                    pulse.play();

                    logger.info("Successfully updated parasite status to '" + event.getParasite().getName() + " detected'");
                } else {
                    logger.error("Failed to find or create parasite status label");
                }

                // Schedule reset to "No Parasites" after 5 seconds
                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.setOnFinished(e -> showNoParasites());
                pause.play();

            } catch (Exception e) {
                logger.error("Error updating parasite status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


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

    // Update method for showing no parasites
    // Update the existing showNoParasites method to use path transition
    private void showNoParasites() {
        logger.info("Day: " + logDay + " Displayed no parasites status");

        Platform.runLater(() -> {
            try {
                // Find existing parasite status label
                if (parasiteStatusLabel != null) {
                    // Create an ImageView for the happy icon
                    Image happyImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
                    ImageView happyImageView = new ImageView(happyImage);
                    happyImageView.setFitHeight(24);
                    happyImageView.setFitWidth(24);

                    // Update the Label directly
                    parasiteStatusLabel.setText("No Parasites");
                    parasiteStatusLabel.setGraphic(happyImageView);
                    parasiteStatusLabel.setTextFill(Color.rgb(0, 120, 0));

                    // Reset style
                    parasiteStatusLabel.setBackground(new Background(new BackgroundFill(
                            Color.rgb(255, 255, 255, 0.85),
                            new CornerRadii(20),
                            Insets.EMPTY
                    )));

                    parasiteStatusLabel.setBorder(new Border(new BorderStroke(
                            Color.rgb(0, 150, 0, 0.7),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(20),
                            new BorderWidths(2)
                    )));

                    logger.info("Successfully updated existing parasite status to 'No Parasites'");

                    // Make sure animation is running
                    if (parasitePathTransition != null &&
                            parasitePathTransition.getStatus() != Animation.Status.RUNNING) {
                        parasitePathTransition.play();
                        logger.info("Restarted parasite status animation");
                    }
                } else {
                    logger.warn("Could not find parasite status label in the scene, creating new one");
                    // Create a new one
                    createParasiteStatusWithPathAnimation();
                }
            } catch (Exception e) {
                logger.error("Error updating parasite status to 'No Parasites': " + e.getMessage());
                e.printStackTrace();

                // Create a new label as fallback
                createParasiteStatusWithPathAnimation();
            }
        });
    }

    private void createSimpleMovingParasiteStatus() {
        Platform.runLater(() -> {
            try {
                logger.info("Creating simple moving parasite status");

                // Get window dimensions
                double anchorWidth = anchorPane.getWidth();
                double anchorHeight = anchorPane.getHeight();

                // Use default values if dimensions aren't available yet
                if (anchorWidth <= 0) anchorWidth = 1000;
                if (anchorHeight <= 0) anchorHeight = 700;

                // Create a simple status label
                Label statusLabel = new Label("No Parasites");

                // Add icon
                Image noParasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
                ImageView parasiteImageView = new ImageView(noParasiteImage);
                parasiteImageView.setFitHeight(24);
                parasiteImageView.setFitWidth(24);
                statusLabel.setGraphic(parasiteImageView);

                // Style the label
                statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                statusLabel.setTextFill(Color.rgb(0, 120, 0));
                statusLabel.setContentDisplay(ContentDisplay.LEFT);
                statusLabel.setPadding(new Insets(5, 15, 5, 15));

                // Add background with rounded corners
                statusLabel.setBackground(new Background(new BackgroundFill(
                        Color.rgb(255, 255, 255, 0.85),
                        new CornerRadii(20),
                        Insets.EMPTY
                )));

                // Add border
                statusLabel.setBorder(new Border(new BorderStroke(
                        Color.rgb(0, 150, 0, 0.7),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(20),
                        new BorderWidths(2)
                )));

                // Add shadow for visual appeal
                statusLabel.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.5)));

                // Position at the bottom of the screen, just above the ground
                double groundHeight = 80; // Approximate height of ground
                statusLabel.setLayoutY(anchorHeight - groundHeight - 30);

                // Add to the scene
                anchorPane.getChildren().add(statusLabel);

                // Store reference to this label
                parasiteStatusLabel = statusLabel;

                // Create a simple horizontal animation
                TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(15), statusLabel);
                translateTransition.setFromX(anchorWidth); // Start from right edge
                translateTransition.setToX(-statusLabel.prefWidth(-1) - 50); // Move to just off the left edge
                translateTransition.setCycleCount(Animation.INDEFINITE); // Repeat indefinitely
                translateTransition.setInterpolator(Interpolator.LINEAR); // Constant speed

                // Store reference to animation
                parasiteStatusAnimation = translateTransition;

                // Start the animation
                translateTransition.play();

                logger.info("Simple moving parasite status created successfully");
            } catch (Exception e) {
                logger.error("Error creating simple moving parasite status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void fixParasiteStatus() {
        Platform.runLater(() -> {
            try {
                logger.info("Attempting to fix parasite status display");

                // Get window dimensions
                double anchorWidth = anchorPane.getWidth();
                double anchorHeight = anchorPane.getHeight();

                // Use default values if dimensions aren't available
                if (anchorWidth <= 0) anchorWidth = 1000;
                if (anchorHeight <= 0) anchorHeight = 700;

                // Create a simple status label
                Label statusValueLabel = new Label("No Parasites");

                // Add initial image
                Image noParasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
                ImageView parasiteImageView = new ImageView(noParasiteImage);
                parasiteImageView.setFitHeight(24);
                parasiteImageView.setFitWidth(24);
                statusValueLabel.setGraphic(parasiteImageView);

                // Style the label
                statusValueLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                statusValueLabel.setTextFill(Color.rgb(0, 120, 0));
                statusValueLabel.setContentDisplay(ContentDisplay.LEFT);
                statusValueLabel.setPadding(new Insets(5, 15, 5, 15));

                // Add background with rounded corners
                statusValueLabel.setBackground(new Background(new BackgroundFill(
                        Color.rgb(255, 255, 255, 0.85),
                        new CornerRadii(20),
                        Insets.EMPTY
                )));

                // Add border
                statusValueLabel.setBorder(new Border(new BorderStroke(
                        Color.rgb(0, 150, 0, 0.7),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(20),
                        new BorderWidths(2)
                )));

                // Add shadow for visual appeal
                statusValueLabel.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.5)));

                // Position initially at the right side, just above ground
                statusValueLabel.setLayoutX(anchorWidth - 200);
                statusValueLabel.setLayoutY(anchorHeight - 120);

                // Add to the scene
                anchorPane.getChildren().add(statusValueLabel);

                // Store direct reference to this label
                parasiteStatusLabel = statusValueLabel;

                // Create a simple horizontal animation
                Timeline timeline = new Timeline();

                // Start from right edge
                KeyFrame start = new KeyFrame(Duration.ZERO,
                        new KeyValue(statusValueLabel.layoutXProperty(), anchorWidth));

                // Move to left edge
                KeyFrame end = new KeyFrame(Duration.seconds(20),
                        new KeyValue(statusValueLabel.layoutXProperty(), -statusValueLabel.prefWidth(-1)));

                // Add keyframes to timeline
                timeline.getKeyFrames().addAll(start, end);

                // Set up the timeline to automatically reverse and repeat
                timeline.setAutoReverse(true);
                timeline.setCycleCount(Timeline.INDEFINITE);

                // Start the animation
                timeline.play();

                logger.info("Simple parasite status display created successfully");
            } catch (Exception e) {
                logger.error("Error creating simple parasite status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Add this method call at the end of your initialize() method
    private void callFixAtStartup() {
        // Add a delay to ensure other components are initialized first
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> fixParasiteStatus());
        delay.play();
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


    private Image createTreeTrunkImage() {
        // Create a simple tree trunk with branches using JavaFX canvas
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(100, 250);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw trunk
        gc.setFill(javafx.scene.paint.Color.web("#48240a"));
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
            double anchorHeight = anchorPane.getHeight() > 0 ? anchorPane.getHeight() : 780.0;

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
            LinearGradient trunkGradient = new LinearGradient(
                    0, 0,  // startX, startY (top)
                    0, 1,  // endX, endY   (bottom)
                    true,  // proportional to shape size
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#5F3813")), // Light purple
                    new Stop(0.5, Color.web("#D2B48C")), // Light brown
                    new Stop(1.0, Color.web("#5F3813")) // Dark brown

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

            Circle treeCircle = createEnhancedCircleButton("#265628");
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
                    new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#C68E17")),     // Golden brown at top
                    new javafx.scene.paint.Stop(0.4, javafx.scene.paint.Color.web("#A0522D")),   // Sienna (medium brown)
                    new javafx.scene.paint.Stop(0.7, javafx.scene.paint.Color.web("#8B4513")),   // Saddle brown (darker)
                    new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#654321"))   // Deep brownish-orange at bottom
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
        // Enhanced color palette for better visibility against the background
        javafx.scene.paint.Color[] colors = {
                javafx.scene.paint.Color.web("#e8f5e9", 0.75),  // Very light green with transparency
                javafx.scene.paint.Color.web("#c8e6c9", 0.75),  // Light green with transparency
                javafx.scene.paint.Color.web("#a5d6a7", 0.75),  // Medium light green with transparency
                javafx.scene.paint.Color.web("#81c784", 0.75)   // Medium green with transparency
        };

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int colorIndex = (row + col) % colors.length;

                StackPane cell = new StackPane();
                cell.setPrefSize(80, 80);

                // Create a more vibrant background color with rounded corners
                javafx.scene.layout.BackgroundFill backgroundFill = new javafx.scene.layout.BackgroundFill(
                        colors[colorIndex],
                        new CornerRadii(10),  // More rounded corners
                        Insets.EMPTY
                );

                // Apply the background
                cell.setBackground(new Background(backgroundFill));

                // Add a more prominent border
                cell.setBorder(new Border(new BorderStroke(
                        javafx.scene.paint.Color.rgb(56, 142, 60, 0.5),  // Darker green border
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(10),
                        new BorderWidths(2)  // Thicker border
                )));

                // Add a more prominent drop shadow effect
                javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
                dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.25));  // More visible shadow
                dropShadow.setRadius(4);  // Larger radius
                dropShadow.setOffsetX(0);
                dropShadow.setOffsetY(2);
                cell.setEffect(dropShadow);

                gridPane.add(cell, col, row);
            }
        }

        gridPane.setHgap(4);  // Slightly larger gap
        gridPane.setVgap(4);
        gridPane.setPadding(new Insets(8, 8, 8, 8));  // More padding

        // Apply enhanced styling to the grid
        gridPane.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(240, 255, 240, 0.6),  // Light green tint with transparency
                new CornerRadii(15),  // More rounded corners
                Insets.EMPTY
        )));

        gridPane.setBorder(new Border(new BorderStroke(
                javafx.scene.paint.Color.rgb(56, 142, 60, 0.7),  // Darker, more visible border
                BorderStrokeStyle.SOLID,
                new CornerRadii(15),
                new BorderWidths(3)  // Thicker border
        )));

        // More prominent outer glow/shadow for the entire grid
        javafx.scene.effect.DropShadow gridShadow = new javafx.scene.effect.DropShadow();
        gridShadow.setColor(javafx.scene.paint.Color.rgb(0, 100, 0, 0.3));  // Green-tinted shadow
        gridShadow.setRadius(12);
        gridShadow.setOffsetX(0);
        gridShadow.setOffsetY(3);
        gridPane.setEffect(gridShadow);
    }

    // Add this method to remove the orange Veg label from top left
    private void animateParasiteStatus() {
        // Hide original static parasite display if it exists
        Platform.runLater(() -> {
            try {
                // Find and hide the original parasite VBox
                for (Node node : anchorPane.getChildren()) {
                    if (node instanceof VBox) {
                        VBox vbox = (VBox) node;
                        for (Node child : vbox.getChildren()) {
                            if (child instanceof Text && "PARASITE".equals(((Text) child).getText())) {
                                vbox.setVisible(false);
                                break;
                            }
                        }
                    }
                }

                // Get window dimensions
                double anchorWidth = anchorPane.getWidth();
                double anchorHeight = anchorPane.getHeight();

                // Use default values if dimensions aren't available yet
                if (anchorWidth <= 0) anchorWidth = 1000;
                if (anchorHeight <= 0) anchorHeight = 700;

                // Create a taller ground/hill area
                double groundHeight = 80; // Increased height

                // Create the main ground as a path to allow for hill shape
                Path groundPath = new Path();

                // Starting point - bottom left
                MoveTo start = new MoveTo(0, anchorHeight);
                groundPath.getElements().add(start);

                // Bottom edge - straight line to right side
                LineTo bottomRight = new LineTo(anchorWidth, anchorHeight);
                groundPath.getElements().add(bottomRight);

                // Right edge - straight line up
                LineTo rightEdge = new LineTo(anchorWidth, anchorHeight - groundHeight);
                groundPath.getElements().add(rightEdge);

                // Create hill contour with quadratic curves
                // First hill peak (small)
                QuadCurveTo hill1 = new QuadCurveTo();
                hill1.setControlX(anchorWidth * 0.8);
                hill1.setControlY(anchorHeight - groundHeight - 15);
                hill1.setX(anchorWidth * 0.7);
                hill1.setY(anchorHeight - groundHeight);
                groundPath.getElements().add(hill1);

                // Valley between hills
                QuadCurveTo valley = new QuadCurveTo();
                valley.setControlX(anchorWidth * 0.6);
                valley.setControlY(anchorHeight - groundHeight + 5);
                valley.setX(anchorWidth * 0.5);
                valley.setY(anchorHeight - groundHeight);
                groundPath.getElements().add(valley);

                // Second hill peak (medium)
                QuadCurveTo hill2 = new QuadCurveTo();
                hill2.setControlX(anchorWidth * 0.4);
                hill2.setControlY(anchorHeight - groundHeight - 25);
                hill2.setX(anchorWidth * 0.3);
                hill2.setY(anchorHeight - groundHeight);
                groundPath.getElements().add(hill2);

                // Left edge - complete the path
                LineTo leftEdge = new LineTo(0, anchorHeight - groundHeight);
                groundPath.getElements().add(leftEdge);

                // Close the path
                LineTo closePath = new LineTo(0, anchorHeight);
                groundPath.getElements().add(closePath);

                // Create a gradient for the ground (green to brown)
                LinearGradient groundGradient = new LinearGradient(
                        0, 0,           // startX, startY (top)
                        0, 1,           // endX, endY   (bottom)
                        true,           // proportional to shape size
                        CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.web("#C18F32")),   // Greenish-teal top
                        new Stop(0.5, Color.web("#9c8b6a")),   // Lime-green middle
                        new Stop(1.0, Color.web("#efe3cb"))    // Purple bottom
                );

                groundPath.setFill(groundGradient);

                // Add a thin border
                groundPath.setStroke(Color.rgb(101, 67, 33, 0.5));
                groundPath.setStrokeWidth(1);

                // Add some depth with a drop shadow
                DropShadow groundShadow = new DropShadow();
                groundShadow.setColor(Color.rgb(0, 0, 0, 0.3));
                groundShadow.setRadius(5);
                groundShadow.setOffsetY(-2);
                groundPath.setEffect(groundShadow);

                // Add ground to the scene (behind other elements)
                anchorPane.getChildren().add(groundPath);
                groundPath.toBack();

                // Only keep background image in front of ground
                for (Node node : anchorPane.getChildren()) {
                    if (node instanceof ImageView && node.getLayoutX() == 0 && node.getLayoutY() == 0) {
                        node.toBack();
                        break;
                    }
                }

                // Add decorative elements to the ground
                addGroundDecorations(groundPath, anchorWidth, anchorHeight, groundHeight);

                // Create a fixed container for the PARASITE STATUS label on top left of land
                HBox statusLabel = new HBox();
                statusLabel.setAlignment(Pos.CENTER_LEFT);
                statusLabel.setPadding(new Insets(5, 15, 5, 15));

                // Position it on the top left of the land
                double labelX = 20; // 20px from left edge
                double labelY = anchorHeight - groundHeight - 30; // 30px above the land

                // Configure the label layout
                statusLabel.setLayoutX(labelX);
                statusLabel.setLayoutY(labelY);

                // Style the label panel with a semi-transparent background
                statusLabel.setBackground(new Background(new BackgroundFill(
                        Color.rgb(255, 255, 255, 0.85),
                        new CornerRadii(20),
                        Insets.EMPTY
                )));

                // Add border to label panel
                statusLabel.setBorder(new Border(new BorderStroke(
                        Color.rgb(0, 150, 0, 0.7),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(20),
                        new BorderWidths(2)
                )));

                // Add shadow for depth
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.rgb(0, 0, 0, 0.5));
                shadow.setRadius(8);
                shadow.setOffsetX(2);
                shadow.setOffsetY(2);
                statusLabel.setEffect(shadow);

                // Create title text
                Text statusTitle = new Text("PARASITE STATUS");
                statusTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
                statusTitle.setFill(Color.rgb(50, 50, 50));

                // Add title to label container
                statusLabel.getChildren().add(statusTitle);

                // Add the status label to the scene
                anchorPane.getChildren().add(statusLabel);

                // Create a separate, animating label for the status value
                Label parasiteValueLabel = new Label("No Parasites");

                // Add initial image
                Image noParasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
                ImageView parasiteImageView = new ImageView(noParasiteImage);
                parasiteImageView.setFitHeight(24);
                parasiteImageView.setFitWidth(24);
                parasiteValueLabel.setGraphic(parasiteImageView);

                // Style the label
                parasiteValueLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                parasiteValueLabel.setTextFill(Color.rgb(0, 120, 0));
                parasiteValueLabel.setContentDisplay(ContentDisplay.LEFT);
                parasiteValueLabel.setPadding(new Insets(5, 15, 5, 15));

                // Add background with rounded corners
                parasiteValueLabel.setBackground(new Background(new BackgroundFill(
                        Color.rgb(255, 255, 255, 0.85),
                        new CornerRadii(20),
                        Insets.EMPTY
                )));

                // Add border
                parasiteValueLabel.setBorder(new Border(new BorderStroke(
                        Color.rgb(0, 150, 0, 0.7),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(20),
                        new BorderWidths(2)
                )));

                // Add shadow
                parasiteValueLabel.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.5)));

                // Position off-screen to start
                parasiteValueLabel.setLayoutX(anchorWidth);
                parasiteValueLabel.setLayoutY(anchorHeight - groundHeight - 30);

                // Add to the scene
                anchorPane.getChildren().add(parasiteValueLabel);

                // Store reference for later updates - IMPORTANT: Store the Label directly
                parasiteStatusContainer = new HBox();
                parasiteStatusContainer.getChildren().add(parasiteValueLabel);

                // Create and start the animation
                animateParasiteStatusLabel(parasiteValueLabel, anchorWidth, anchorHeight, groundHeight);

                logger.info("Enhanced ground with parasite status display created successfully");

                // Set the initial status to "No Parasites"
                showNoParasites();
            } catch (Exception e) {
                logger.error("Error creating parasite status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void forceZigzagAnimation() {
        Platform.runLater(() -> {
            // Find the parasite status label
            for (Node node : anchorPane.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText() != null &&
                            (label.getText().contains("No Parasites") ||
                                    label.getText().contains("detected"))) {

                        logger.info("Found parasite label, applying force animation");

                        // Get anchor dimensions
                        double width = anchorPane.getWidth() > 0 ? anchorPane.getWidth() : 1000;
                        double height = anchorPane.getHeight() > 0 ? anchorPane.getHeight() : 600;

                        // Apply animation directly
                        animateParasiteStatusLabel(label, width, height, 80);
                        return;
                    }
                }
            }

            logger.warn("No parasite label found for forced animation");
        });
    }

    // Animate the parasite status label across the ground
    private void animateParasiteStatusLabel(Label statusLabel, double anchorWidth, double anchorHeight, double groundHeight) {
        try {
            // For debugging, let's make the label visually distinctive
            statusLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-padding: 8px 15px; -fx-background-radius: 20px;");

            // Stop any existing animations
            if (parasitePathTransition != null) {
                parasitePathTransition.stop();
            }

            // Create a completely new zigzag path with very obvious movement
            Path zigzagPath = new Path();

            // Start at the right edge
            MoveTo startPoint = new MoveTo(anchorWidth, anchorHeight - groundHeight - 30);
            zigzagPath.getElements().add(startPoint);

            // Create obvious zigzag segments
            double segmentWidth = 60;
            double amplitude = 50; // Very large amplitude to be clearly visible

            int numSegments = (int) (anchorWidth / segmentWidth) + 1;

            for (int i = 0; i < numSegments; i++) {
                double x = anchorWidth - (i * segmentWidth);
                double baseY = anchorHeight - groundHeight - 30;

                // Make a sharper zigzag
                double midX = x - (segmentWidth / 2);
                double downY = baseY + amplitude;

                LineTo down = new LineTo(midX, downY);
                zigzagPath.getElements().add(down);

                LineTo up = new LineTo(x - segmentWidth, baseY);
                zigzagPath.getElements().add(up);
            }

            // Create a new path transition with direct reference
            PathTransition transition = new PathTransition();
            transition.setDuration(Duration.seconds(10)); // Faster to see the effect
            transition.setPath(zigzagPath);
            transition.setNode(statusLabel);
            transition.setOrientation(PathTransition.OrientationType.NONE);
            transition.setCycleCount(Animation.INDEFINITE);
            transition.setAutoReverse(true);

            // For debugging, print when animation starts
            logger.info("Starting zigzag animation for parasite label");

            // Start the animation immediately
            transition.play();

            // Store reference
            parasitePathTransition = transition;

        } catch (Exception e) {
            logger.error("Error in zigzag animation: " + e.getMessage(), e);
        }
    }

    // If you have existing animations that need to be stopped first,
// add this helper method to stop them before starting new ones
    private void stopExistingParasiteAnimations() {
        if (parasiteStatusAnimation != null) {
            parasiteStatusAnimation.stop();
            parasiteStatusAnimation = null;
        }

        if (parasitePathTransition != null) {
            parasitePathTransition.stop();
            parasitePathTransition = null;
        }
    }

    // Add this method to ensure the parasite status is displayed correctly
// Call this when you need to create a new parasite status label
    private void createZigzagParasiteStatus() {
        try {
            // Get window dimensions
            double anchorWidth = anchorPane.getWidth();
            double anchorHeight = anchorPane.getHeight();
            double groundHeight = 80;

            // Use default values if dimensions aren't available
            if (anchorWidth <= 0) anchorWidth = 1000;
            if (anchorHeight <= 0) anchorHeight = 700;

            // Create "No Parasites" status
            Image noParasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
            ImageView parasiteImageView = new ImageView(noParasiteImage);
            parasiteImageView.setFitHeight(24);
            parasiteImageView.setFitWidth(24);

            // Create label with proper styling
            Label statusLabel = new Label("No Parasites");
            statusLabel.setGraphic(parasiteImageView);
            statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            statusLabel.setTextFill(Color.rgb(0, 120, 0));
            statusLabel.setContentDisplay(ContentDisplay.LEFT);
            statusLabel.setPadding(new Insets(5, 15, 5, 15));

            // Add background and border
            statusLabel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(255, 255, 255, 0.85),
                    new CornerRadii(20),
                    Insets.EMPTY
            )));

            statusLabel.setBorder(new Border(new BorderStroke(
                    Color.rgb(0, 150, 0, 0.7),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(20),
                    new BorderWidths(2)
            )));

            // Add shadow effect
            DropShadow shadow = new DropShadow(8, Color.rgb(0, 0, 0, 0.5));
            statusLabel.setEffect(shadow);

            // Position the label initially at the right edge of the screen
            statusLabel.setLayoutX(anchorWidth);
            statusLabel.setLayoutY(anchorHeight - groundHeight - 30);

            // Add the label to the scene
            anchorPane.getChildren().add(statusLabel);

            // Save reference for later use
            parasiteStatusLabel = statusLabel;

            // Stop any existing animations
            stopExistingParasiteAnimations();

            // Start zigzag animation
            animateParasiteStatusLabel(statusLabel, anchorWidth, anchorHeight, groundHeight);

        } catch (Exception e) {
            logger.error("Error creating zigzag parasite status: " + e.getMessage());
            e.printStackTrace();
        }
    }

// For ground decorations
// [Keep this method as is from your previous implementation]

    // Update method for showing no parasites


    // Method to create a new parasite status label if needed
    private void createNewParasiteStatusLabel(String text, String imagePath, Color textColor) {
        try {
            // Get window dimensions
            double anchorWidth = anchorPane.getWidth();
            double anchorHeight = anchorPane.getHeight();
            double groundHeight = 80;

            // Use default values if dimensions aren't available
            if (anchorWidth <= 0) anchorWidth = 1000;
            if (anchorHeight <= 0) anchorHeight = 700;

            // Create the image
            Image parasiteImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView parasiteImageView = new ImageView(parasiteImage);
            parasiteImageView.setFitHeight(24);
            parasiteImageView.setFitWidth(24);

            // Create a new Label
            Label newStatusLabel = new Label(text);
            newStatusLabel.setGraphic(parasiteImageView);
            newStatusLabel.setContentDisplay(ContentDisplay.LEFT);
            newStatusLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            newStatusLabel.setTextFill(textColor);
            newStatusLabel.setPadding(new Insets(5, 15, 5, 15));

            // Style the label
            newStatusLabel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(255, 255, 255, 0.85),
                    new CornerRadii(20),
                    Insets.EMPTY
            )));

            newStatusLabel.setBorder(new Border(new BorderStroke(
                    Color.rgb(0, 150, 0, 0.7),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(20),
                    new BorderWidths(2)
            )));

            // Add shadow
            newStatusLabel.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.5)));

            // Position it
            newStatusLabel.setLayoutX(300); // Middle of screen
            newStatusLabel.setLayoutY(anchorHeight - groundHeight - 30);

            // Add to scene
            anchorPane.getChildren().add(newStatusLabel);

            // Animate it
            animateParasiteStatusLabel(newStatusLabel, anchorWidth, anchorHeight, groundHeight);

            // Update the container reference
            if (parasiteStatusContainer == null) {
                parasiteStatusContainer = new HBox();
            } else {
                parasiteStatusContainer.getChildren().clear();
            }
            parasiteStatusContainer.getChildren().add(newStatusLabel);

            logger.info("Created new parasite status label due to missing reference");
        } catch (Exception e) {
            logger.error("Error creating new parasite status label: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update method for handling parasite events


    // Add decorative elements to the ground
    private Path createGroundArea() {
        try {
            // Get window dimensions
            double anchorWidth = anchorPane.getWidth();
            double anchorHeight = anchorPane.getHeight();

            // Use default values if dimensions aren't available yet
            if (anchorWidth <= 0) anchorWidth = 1000;
            if (anchorHeight <= 0) anchorHeight = 700;

            // Define ground height - REDUCED from 80 to 60
            double groundHeight = 60;

            // Create the main ground as a path to allow for hill shape
            Path groundPath = new Path();

            // Starting point - bottom left
            MoveTo start = new MoveTo(0, anchorHeight);
            groundPath.getElements().add(start);

            // Bottom edge - straight line to right side
            LineTo bottomRight = new LineTo(anchorWidth, anchorHeight);
            groundPath.getElements().add(bottomRight);

            // Right edge - straight line up
            LineTo rightEdge = new LineTo(anchorWidth, anchorHeight - groundHeight);
            groundPath.getElements().add(rightEdge);

            // Create hill contour with quadratic curves
            // First hill peak (small)
            QuadCurveTo hill1 = new QuadCurveTo();
            hill1.setControlX(anchorWidth * 0.8);
            hill1.setControlY(anchorHeight - groundHeight - 15);
            hill1.setX(anchorWidth * 0.7);
            hill1.setY(anchorHeight - groundHeight);
            groundPath.getElements().add(hill1);

            // Valley between hills
            QuadCurveTo valley = new QuadCurveTo();
            valley.setControlX(anchorWidth * 0.6);
            valley.setControlY(anchorHeight - groundHeight + 5);
            valley.setX(anchorWidth * 0.5);
            valley.setY(anchorHeight - groundHeight);
            groundPath.getElements().add(valley);

            // Second hill peak (medium)
            QuadCurveTo hill2 = new QuadCurveTo();
            hill2.setControlX(anchorWidth * 0.4);
            hill2.setControlY(anchorHeight - groundHeight - 25);
            hill2.setX(anchorWidth * 0.3);
            hill2.setY(anchorHeight - groundHeight);
            groundPath.getElements().add(hill2);

            // Left edge - complete the path
            LineTo leftEdge = new LineTo(0, anchorHeight - groundHeight);
            groundPath.getElements().add(leftEdge);

            // Close the path
            LineTo closePath = new LineTo(0, anchorHeight);
            groundPath.getElements().add(closePath);

            // Create a gradient for the ground (green to brown)
            LinearGradient groundGradient = new LinearGradient(
                    0, 0,           // startX, startY (top)
                    0, 1,           // endX, endY   (bottom)
                    true,           // proportional to shape size
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#3c4d35")),
                    new Stop(0.2, Color.web("#006400")),   // Dark green at left
                    new Stop(0.3, Color.web("#228B22")),   // Forest green
                    new Stop(0.4, Color.web("#6B8E23")),   // Olive green
                    new Stop(0.6, Color.web("#9ACD32")),   // Yellow-green
                    new Stop(0.9, Color.web("#eedfaf"))  // Metallic gold

            );

            groundPath.setFill(groundGradient);

            // Add a thin border
            groundPath.setStroke(Color.rgb(101, 67, 33, 0.5));
            groundPath.setStrokeWidth(1);

            // Add some depth with a drop shadow
            DropShadow groundShadow = new DropShadow();
            groundShadow.setColor(Color.rgb(0, 0, 0, 0.3));
            groundShadow.setRadius(5);
            groundShadow.setOffsetY(-2);
            groundPath.setEffect(groundShadow);

            // Add decorative elements to the ground
            addGroundDecorations(groundPath, anchorWidth, anchorHeight, groundHeight);

            return groundPath;
        } catch (Exception e) {
            logger.error("Error creating ground: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Updated method to position sprinklers with second one touching ground
//    private void addSprinklersToGround() {
//        Platform.runLater(() -> {
//            try {
//                logger.info("Adding sprinklers to ground area");
//
//                // Get window dimensions
//                double anchorWidth = anchorPane.getWidth();
//                double anchorHeight = anchorPane.getHeight();
//
//                // Use default values if dimensions aren't available yet
//                if (anchorWidth <= 0) anchorWidth = 1000;
//                if (anchorHeight <= 0) anchorHeight = 700;
//
//                // Define ground height and positions for sprinklers
//                double groundHeight = 60; // REDUCED from 80 to 60
//                double groundTop = anchorHeight - groundHeight;
//
//                // Create first sprinkler on left side (pop-up style)
//                createPopUpSprinkler(120, groundTop - 15, true);
//
//                // Create second sprinkler further to the left (impact style)
//                // Position adjusted to touch the ground - no y offset
//                createImpactSprinkler(280, groundTop, true);
//
//                logger.info("Successfully added two different sprinklers to ground area");
//            } catch (Exception e) {
//                logger.error("Error adding sprinklers to ground: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });
//    }

    // Add decorative elements to the ground
    private void addGroundDecorations(Path groundPath, double anchorWidth, double anchorHeight, double groundHeight) {
        Random random = new Random();

        // Add grass tufts along the top of the ground - FEWER tufts for shorter ground
        for (int i = 0; i < 20; i++) { // Reduced from 25 to 20
            // Random position along the ground
            double posX = random.nextDouble() * anchorWidth;

            // Determine if this should be on a hill
            double posY = anchorHeight - groundHeight;

            // Adjust Y position based on X to follow hill contours
            if (posX > anchorWidth * 0.7 && posX < anchorWidth * 0.9) {
                // Right hill
                double hillFactor = 15 * Math.sin((posX - anchorWidth * 0.7) / (anchorWidth * 0.2) * Math.PI);
                posY -= hillFactor;
            } else if (posX > anchorWidth * 0.2 && posX < anchorWidth * 0.4) {
                // Left hill
                double hillFactor = 25 * Math.sin((posX - anchorWidth * 0.2) / (anchorWidth * 0.2) * Math.PI);
                posY -= hillFactor;
            }

            // Create a grass blade group
            Group grassTuft = new Group();

            // Add 2-4 blades of grass per tuft
            int numBlades = 2 + random.nextInt(3);
            for (int j = 0; j < numBlades; j++) {
                // Create a curved line for more natural grass
                QuadCurve grassBlade = new QuadCurve();
                grassBlade.setStartX(posX + random.nextDouble() * 4 - 2);
                grassBlade.setStartY(posY);

                double endX = posX + random.nextDouble() * 8 - 4;
                double endY = posY - 4 - random.nextDouble() * 6; // SHORTER grass for shorter ground

                grassBlade.setEndX(endX);
                grassBlade.setEndY(endY);

                // Control point for curve
                grassBlade.setControlX(posX + (endX - posX) * 0.5 + (random.nextDouble() * 6 - 3));
                grassBlade.setControlY(posY - (posY - endY) * 0.3);

                // Vary the green color
                grassBlade.setStroke(Color.rgb(
                        30 + random.nextInt(50),
                        120 + random.nextInt(80),
                        30 + random.nextInt(50),
                        0.7 + random.nextDouble() * 0.3
                ));
                grassBlade.setStrokeWidth(1 + random.nextDouble());
                grassBlade.setFill(null);
                grassBlade.setStrokeLineCap(StrokeLineCap.ROUND);

                grassTuft.getChildren().add(grassBlade);
            }

            // Add to scene
            anchorPane.getChildren().add(grassTuft);
        }

        // Add fewer flowers for a shorter ground
        for (int i = 0; i < 6; i++) { // Reduced from 8 to 6
            double posX = random.nextDouble() * anchorWidth;
            double posY = anchorHeight - random.nextDouble() * (groundHeight * 0.7);

            if (random.nextBoolean()) {
                // Flower
                Circle flowerCenter = new Circle(posX, posY, 2 + random.nextDouble() * 1.5); // Smaller flowers
                flowerCenter.setFill(Color.YELLOW);

                // Petals
                String[] petalColors = {
                        "#FF69B4", "#FF1493", "#FF6347", "#FFFF00", "#FFA500"
                };
                String petalColor = petalColors[random.nextInt(petalColors.length)];

                Group flower = new Group(flowerCenter);

                // Add 5-8 petals
                int numPetals = 5 + random.nextInt(4);
                for (int p = 0; p < numPetals; p++) {
                    double angle = p * (360.0 / numPetals);
                    double rads = Math.toRadians(angle);

                    Circle petal = new Circle(
                            posX + Math.cos(rads) * 2.5, // Smaller petals
                            posY + Math.sin(rads) * 2.5,
                            1.5 + random.nextDouble() * 1.5 // Smaller petals
                    );
                    petal.setFill(Color.web(petalColor));
                    flower.getChildren().add(petal);
                }

                // Add stem
                Line stem = new Line(posX, posY, posX, posY + 4 + random.nextDouble() * 5); // Shorter stem
                stem.setStroke(Color.rgb(50, 120, 50));
                stem.setStrokeWidth(1.5);

                Group flowerWithStem = new Group(stem, flower);
                anchorPane.getChildren().add(flowerWithStem);
            }
        }

        // Add fewer small rocks for shorter ground
        for (int i = 0; i < 8; i++) { // Reduced from 10 to 8
            double posX = random.nextDouble() * anchorWidth;
            double posY = anchorHeight - random.nextDouble() * groundHeight * 0.7;

            double size = 1.5 + random.nextDouble() * 4; // Slightly smaller rocks

            Ellipse rock = new Ellipse(posX, posY, size, size * 0.7);

            // Vary the rock color
            rock.setFill(Color.rgb(
                    120 + random.nextInt(60),
                    120 + random.nextInt(60),
                    120 + random.nextInt(60),
                    0.7 + random.nextDouble() * 0.3
            ));

            // Add highlight
            Ellipse highlight = new Ellipse(posX - size * 0.3, posY - size * 0.2, size * 0.3, size * 0.2);
            highlight.setFill(Color.rgb(255, 255, 255, 0.3));

            Group rockWithHighlight = new Group(rock, highlight);
            anchorPane.getChildren().add(rockWithHighlight);
        }
    }

    // Method to initialize parasite status with ground
    public void initializeParasiteStatusWithGround() {
        Platform.runLater(() -> {
            try {
                logger.info("Initializing parasite status with ground");

                // Create ground if it doesn't exist and add it to scene
                Path groundPath = createGroundArea();
                if (groundPath != null) {
                    // Add ground to the scene (behind other elements)
                    anchorPane.getChildren().add(groundPath);
                    groundPath.toBack();

                    // Ensure the background image stays behind ground
                    for (Node node : anchorPane.getChildren()) {
                        if (node instanceof ImageView && node.getLayoutX() == 0 && node.getLayoutY() == 0) {
                            node.toBack();
                            break;
                        }
                    }
                    addSprinklersToGround();
                }

                // Create parasite status label
                createParasiteStatusWithPathAnimation();
                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(e -> addSprinklersToGround());
                delay.play();

            } catch (Exception e) {
                logger.error("Error initializing parasite status with ground: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Updated method to create and animate sprinklers with different designs
    // Updated method to position sprinklers directly on the wavy ground
    private void addSprinklersToGround() {
        Platform.runLater(() -> {
            try {
                logger.info("Adding sprinklers to ground area");

                // Get window dimensions
                double anchorWidth = anchorPane.getWidth();
                double anchorHeight = anchorPane.getHeight();

                // Use default values if dimensions aren't available yet
                if (anchorWidth <= 0) anchorWidth = 1000;
                if (anchorHeight <= 0) anchorHeight = 700;

                // From your screenshot, we can see the ground is wavy
                // We'll use fixed Y-positions that match the ground curve

                // First sprinkler (left) - position on the ground curve
                double sprinkler1X = 120;
                // Notice that the ground is higher on the left side in your screenshot
                double sprinkler1Y = anchorHeight - 50; // Position directly on the visible ground

                // Second sprinkler (right) - positioned on another part of the ground
                double sprinkler2X = 280;
                // The ground curves down a bit here
                double sprinkler2Y = anchorHeight - 45; // Position directly on the visible ground

                // Create sprinklers at these precise positions
                createSimpleSprinkler(sprinkler1X, sprinkler1Y, true);
                createSimpleSprinkler(sprinkler2X, sprinkler2Y, true);

                // Add roses and plants to the ground
                addPlantsToGround(anchorWidth, anchorHeight);

                logger.info("Successfully added sprinklers and plants to ground");
            } catch (Exception e) {
                logger.error("Error adding sprinklers to ground: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Simplified sprinkler design that's clearly visible against the ground
    private void createSimpleSprinkler(double x, double y, boolean isActive) {
        try {
            // Create sprinkler group
            Group sprinklerGroup = new Group();

            // Create base directly ON the ground
            Circle base = new Circle(x, y, 10);
            base.setFill(Color.rgb(70, 70, 70)); // Dark gray
            base.setStroke(Color.rgb(20, 20, 20));
            base.setStrokeWidth(1.5);

            // Create visible connection to the ground
            Rectangle connector = new Rectangle(x - 8, y - 5, 16, 10);
            connector.setFill(Color.rgb(60, 60, 60));
            connector.setArcWidth(5);
            connector.setArcHeight(5);

            // Create sprinkler body
            Rectangle body = new Rectangle(x - 5, y - 30, 10, 25);
            body.setFill(Color.rgb(100, 100, 100)); // Medium gray
            body.setArcWidth(3);
            body.setArcHeight(3);
            body.setStroke(Color.rgb(40, 40, 40));
            body.setStrokeWidth(1);

            // Create sprinkler head
            Circle head = new Circle(x, y - 33, 6);
            head.setFill(Color.rgb(130, 130, 130)); // Lighter gray for contrast
            head.setStroke(Color.rgb(40, 40, 40));
            head.setStrokeWidth(1);

            // Create water nozzle
            Circle nozzle = new Circle(x, y - 33, 2);
            nozzle.setFill(Color.rgb(30, 30, 30)); // Very dark gray

            // Add all parts to the group - order matters for layering
            sprinklerGroup.getChildren().addAll(connector, base, body, head, nozzle);

            // Add drop shadow for better visibility
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.6));
            shadow.setRadius(4);
            shadow.setOffsetY(2);
            sprinklerGroup.setEffect(shadow);

            // Add to scene with high view order to ensure visibility
            anchorPane.getChildren().add(sprinklerGroup);
            sprinklerGroup.setViewOrder(-2000);

            // Add water animation if active
            if (isActive) {
                animateSprinklerWater(sprinklerGroup, x, y - 33);
            }

            logger.info("Created sprinkler at exact position: x=" + x + ", y=" + y);

        } catch (Exception e) {
            logger.error("Error creating sprinkler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to add roses and plants to the ground
    private void addPlantsToGround(double anchorWidth, double anchorHeight) {
        try {
            // Add roses and other plants along the ground

            // Create a rose on the left side
            createRose(180, anchorHeight - 55);

            // Create another rose on the right side
            createRose(420, anchorHeight - 55);

            // Create a small bush on the center-left
            //createBush(240, anchorHeight - 50);

            // Create some flowers
            createFlowerPatch(340, anchorHeight - 60);

            // Create a small decorative plant on the far right
            createDecorativePlant(500, anchorHeight - 60);

            logger.info("Added roses and plants to ground area");
        } catch (Exception e) {
            logger.error("Error adding plants to ground: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create a rose with stem and flower
    private void createRose(double x, double y) {
        Group roseGroup = new Group();

        // Create stem
        Rectangle stem = new Rectangle(x - 1.5, y - 30, 3, 30);
        stem.setFill(Color.rgb(40, 100, 40)); // Dark green

        // Create leaves (2-3 small leaves on the stem)
        for (int i = 0; i < 2; i++) {
            double leafY = y - 10 - (i * 10);
            double leafSide = (i % 2 == 0) ? -1 : 1; // Alternate sides

            // Create a leaf using a polygon for a more natural shape
            Polygon leaf = new Polygon();
            leaf.getPoints().addAll(
                    x, leafY,                        // Stem attachment point
                    x + (leafSide * 10), leafY - 5,  // Tip of leaf
                    x + (leafSide * 7), leafY - 2,   // Side point
                    x + (leafSide * 3), leafY - 3    // Base curve
            );
            leaf.setFill(Color.rgb(50, 150, 50)); // Green

            roseGroup.getChildren().add(leaf);
        }

        // Create rose flower (multiple petals in a circle)
        double flowerSize = 7;
        Color roseColor = Color.rgb(220, 50, 80); // Rose red

        // Center bud
        Circle centerBud = new Circle(x, y - 35, flowerSize - 2);
        centerBud.setFill(roseColor.darker());

        // Add to group
        roseGroup.getChildren().addAll(stem, centerBud);

        // Create petals around the center
        int numPetals = 8;
        for (int i = 0; i < numPetals; i++) {
            double angle = i * (360.0 / numPetals);
            double radian = Math.toRadians(angle);

            double petalX = x + Math.cos(radian) * (flowerSize - 2);
            double petalY = (y - 35) + Math.sin(radian) * (flowerSize - 2);

            Circle petal = new Circle(petalX, petalY, flowerSize);
            petal.setFill(roseColor);

            roseGroup.getChildren().add(petal);
        }

        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(3);
        shadow.setOffsetY(2);
        roseGroup.setEffect(shadow);

        // Add to scene with high view order to ensure visibility
        anchorPane.getChildren().add(roseGroup);
        roseGroup.setViewOrder(-1500);
    }

    // Create a small bush
    private void createBush(double x, double y) {
        Group bushGroup = new Group();

        // Create several overlapping circular sections for the bush
        int numSections = 6;
        double bushWidth = 30;
        double bushHeight = 25;

        for (int i = 0; i < numSections; i++) {
            double sectionX = x - (bushWidth / 2) + (i * (bushWidth / (numSections - 1)));
            double sectionY = y - (bushHeight * 0.7);
            double sectionSize = 8 + (Math.random() * 4);

            // Vary the height a little
            if (i % 2 == 0) {
                sectionY -= 3;
            }

            Circle section = new Circle(sectionX, sectionY, sectionSize);

            // Vary the green shade slightly
            int greenVariation = (int) (Math.random() * 30);
            section.setFill(Color.rgb(
                    50 + greenVariation,
                    120 + greenVariation,
                    50 + (greenVariation / 2)
            ));

            bushGroup.getChildren().add(section);
        }

        // Create a thin trunk/stem visible at the bottom
        Rectangle trunk = new Rectangle(x - 2, y - 5, 4, 5);
        trunk.setFill(Color.rgb(80, 60, 30));
        bushGroup.getChildren().add(trunk);

        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(3);
        shadow.setOffsetY(2);
        bushGroup.setEffect(shadow);

        // Add to scene
        anchorPane.getChildren().add(bushGroup);
        bushGroup.setViewOrder(-1500);
    }

    // Create a patch of small flowers
    private void createFlowerPatch(double x, double y) {
        Group flowerGroup = new Group();

        // Add several small flowers in a cluster
        int numFlowers = 5;
        double spreadX = 25;
        double spreadY = 15;

        for (int i = 0; i < numFlowers; i++) {
            double flowerX = x - (spreadX / 2) + (Math.random() * spreadX);
            double flowerY = y - (Math.random() * spreadY);

            // Choose flower color - mix of colors for variety
            Color[] flowerColors = {
                    Color.rgb(255, 255, 100), // Yellow
                    Color.rgb(255, 100, 255), // Pink
                    Color.rgb(100, 100, 255), // Blue
                    Color.rgb(255, 150, 50)   // Orange
            };
            Color flowerColor = flowerColors[(int) (Math.random() * flowerColors.length)];

            // Create flower stem
            Line stem = new Line(flowerX, flowerY, flowerX, flowerY - 10 - (Math.random() * 5));
            stem.setStroke(Color.rgb(50, 120, 50));
            stem.setStrokeWidth(1.5);
            flowerGroup.getChildren().add(stem);

            // Create flower center
            Circle center = new Circle(flowerX, stem.getEndY(), 2);
            center.setFill(Color.rgb(200, 150, 0));
            flowerGroup.getChildren().add(center);

            // Create petals
            int numPetals = 5 + (int) (Math.random() * 3);
            for (int j = 0; j < numPetals; j++) {
                double angle = j * (360.0 / numPetals);
                double radian = Math.toRadians(angle);

                double petalX = flowerX + Math.cos(radian) * 4;
                double petalY = stem.getEndY() + Math.sin(radian) * 4;

                Circle petal = new Circle(petalX, petalY, 3);
                petal.setFill(flowerColor);

                flowerGroup.getChildren().add(petal);
            }
        }

        // Add drop shadow for depth
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(2);
        shadow.setOffsetY(1);
        flowerGroup.setEffect(shadow);

        // Add to scene
        anchorPane.getChildren().add(flowerGroup);
        flowerGroup.setViewOrder(-1500);
    }

    // Create a decorative plant (like a small fern or ornamental grass)
    private void createDecorativePlant(double x, double y) {
        Group plantGroup = new Group();

        // Create a small base/soil mound
        Circle soilMound = new Circle(x, y, 8);
        soilMound.setFill(Color.rgb(100, 70, 40)); // Brown soil
        plantGroup.getChildren().add(soilMound);

        // Add plant stems/blades
        int numStems = 7;
        for (int i = 0; i < numStems; i++) {
            double angle = -60 - (i * (60.0 / numStems)); // Spread from -60 to -120 degrees
            double radian = Math.toRadians(angle);

            double stemLength = 15 + (Math.random() * 10);

            // Create a curved path for the stem
            Path stemPath = new Path();

            MoveTo start = new MoveTo(x, y);
            stemPath.getElements().add(start);

            double controlX = x + Math.cos(radian) * (stemLength * 0.5);
            double controlY = y + Math.sin(radian) * (stemLength * 0.5);
            double endX = x + Math.cos(radian) * stemLength;
            double endY = y + Math.sin(radian) * stemLength;

            QuadCurveTo curve = new QuadCurveTo(controlX, controlY, endX, endY);
            stemPath.getElements().add(curve);

            stemPath.setStroke(Color.rgb(50 + (int) (Math.random() * 30),
                    120 + (int) (Math.random() * 30),
                    50));
            stemPath.setStrokeWidth(1 + Math.random());
            stemPath.setStrokeLineCap(StrokeLineCap.ROUND);

            plantGroup.getChildren().add(stemPath);
        }

        // Add shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(3);
        shadow.setOffsetY(2);
        plantGroup.setEffect(shadow);

        // Add to scene
        anchorPane.getChildren().add(plantGroup);
        plantGroup.setViewOrder(-1500);
    }

    // Updated water animation for better visibility
    private void animateSprinklerWater(Group sprinklerGroup, double x, double y) {
        // Create a group for water particles
        Group waterGroup = new Group();
        sprinklerGroup.getChildren().add(waterGroup);

        // Create a timeline for spraying water particles
        Timeline waterAnimation = new Timeline(
                new KeyFrame(Duration.millis(80), event -> {
                    // Create a fan pattern of water
                    for (int i = -4; i <= 4; i++) {
                        double angle = -90 + (i * 15); // -150 to -30 degrees

                        // Random chance to skip some drops for natural effect
                        if (Math.random() < 0.7) {
                            addWaterDroplet(waterGroup, x, y, angle);
                        }
                    }
                })
        );
        waterAnimation.setCycleCount(Timeline.INDEFINITE);
        waterAnimation.play();

        // Add cycling behavior
        Timeline cycleAnimation = new Timeline(
                new KeyFrame(Duration.seconds(4), e -> {
                    waterAnimation.pause();
                    waterGroup.getChildren().clear();
                }),
                new KeyFrame(Duration.seconds(6), e -> {
                    waterAnimation.play();
                })
        );
        cycleAnimation.setCycleCount(Timeline.INDEFINITE);
        cycleAnimation.play();
    }

    // Create water droplets with improved visibility
    private void addWaterDroplet(Group waterGroup, double x, double y, double angle) {
        // Convert angle to radians
        double radian = Math.toRadians(angle);

        // Create a water droplet
        Circle droplet = new Circle(2.5 + Math.random());

        // Use bright blue color with some transparency
        droplet.setFill(Color.rgb(0, 160, 255, 0.7 + Math.random() * 0.3));

        // Add a slight outline for better visibility
        droplet.setStroke(Color.rgb(0, 100, 220, 0.5));
        droplet.setStrokeWidth(0.5);

        // Set initial position
        droplet.setCenterX(x);
        droplet.setCenterY(y);

        // Add to group
        waterGroup.getChildren().add(droplet);

        // Set an extremely high view order
        droplet.setViewOrder(-3000);

        // Create arc path animation
        double speed = 0.8 + Math.random() * 0.4; // Duration in seconds
        double distance = 30 + Math.random() * 20; // Distance the droplet travels

        // Create timeline animation with 15 steps
        Timeline animation = new Timeline();
        int steps = 15;

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;

            // Calculate position along arc path with gravity
            double pathX = x + (distance * t * Math.cos(radian));
            double pathY = y + (distance * t * Math.sin(radian)) + (20 * t * t); // Add gravity

            // Gradually reduce opacity near the end
            double opacity = (i < steps * 0.7) ?
                    (0.7 + Math.random() * 0.3) :
                    ((1 - (i - steps * 0.7) / (steps * 0.3)) * 0.7);

            // Add keyframe
            animation.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(speed * t),
                            new KeyValue(droplet.centerXProperty(), pathX),
                            new KeyValue(droplet.centerYProperty(), pathY),
                            new KeyValue(droplet.opacityProperty(), opacity)
                    )
            );
        }

        // Remove when done
        animation.setOnFinished(e -> waterGroup.getChildren().remove(droplet));

        // Play the animation
        animation.play();
    }
    // Simplified water animation for visibility


    // Create highly visible water droplets
    private void addVisibleWaterDroplet(Group waterGroup, double x, double y, double angle) {
        // Convert angle to radians
        double radian = Math.toRadians(angle);

        // Create a larger, more visible droplet
        Circle droplet = new Circle(3);

        // Use a bright blue color that's easy to see
        droplet.setFill(Color.rgb(0, 150, 255, 0.8));

        // Add a stroke for extra visibility
        droplet.setStroke(Color.rgb(0, 100, 200, 0.5));
        droplet.setStrokeWidth(1);

        // Set initial position at the nozzle
        droplet.setCenterX(x);
        droplet.setCenterY(y);

        // Add to the water group
        waterGroup.getChildren().add(droplet);

        // Set a high view order to ensure droplets are visible
        droplet.setViewOrder(-2000);

        // Calculate a simple arc path for the water
        double speed = 1.0 + Math.random() * 0.5; // Consistent speed
        double distance = 40 + Math.random() * 20; // Moderate distance

        // Create animation timeline for the droplet
        Timeline timeline = new Timeline();

        // Add keyframes for the motion - 10 steps for smooth movement
        int steps = 10;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;

            // Calculate position along the arc
            double dx = distance * t * Math.cos(radian);
            double dy = distance * t * Math.sin(radian) + (15 * t * t); // Add gravity

            double newX = x + dx;
            double newY = y + dy;

            // Add keyframe
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(speed * t),
                            new KeyValue(droplet.centerXProperty(), newX),
                            new KeyValue(droplet.centerYProperty(), newY)
                    )
            );

            // Add fade out toward the end
            if (i > steps * 0.7) {
                double opacity = 1 - ((i - steps * 0.7) / (steps * 0.3));
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(speed * t),
                                new KeyValue(droplet.opacityProperty(), opacity)
                        )
                );
            }
        }

        // Remove the droplet when animation completes
        timeline.setOnFinished(e -> waterGroup.getChildren().remove(droplet));

        // Play the animation
        timeline.play();
    }

    // Create a pop-up style sprinkler (more modern, sleek design)
    private void createPopUpSprinkler(double x, double y, boolean isActive) {
        try {
            // Create sprinkler group
            Group sprinklerGroup = new Group();

            // Create underground housing - slightly wider than the pop-up part
            Rectangle housing = new Rectangle(x - 8, y + 5, 16, 20);
            housing.setFill(Color.rgb(50, 50, 50)); // Dark gray
            housing.setArcWidth(5);
            housing.setArcHeight(5);

            // Create pop-up riser - thinner tube that "pops up" from the housing
            Rectangle riser = new Rectangle(x - 4, y - 15, 8, 20);
            riser.setFill(Color.rgb(120, 120, 120)); // Light gray
            riser.setArcWidth(8);
            riser.setArcHeight(8);

            // Create sprinkler head - flat nozzle on top
            Rectangle head = new Rectangle(x - 7, y - 18, 14, 4);
            head.setFill(Color.rgb(50, 50, 50)); // Dark gray
            head.setArcWidth(4);
            head.setArcHeight(4);

            // Add water outlet slots
            for (int i = 0; i < 3; i++) {
                double slotX = x - 6 + (i * 5);
                Rectangle slot = new Rectangle(slotX, y - 18, 2, 2);
                slot.setFill(Color.rgb(30, 30, 30)); // Darker for contrast
                sprinklerGroup.getChildren().add(slot);
            }

            // Add all parts to the group
            sprinklerGroup.getChildren().addAll(housing, riser, head);

            // Add drop shadow for better visibility
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.5));
            shadow.setRadius(3);
            shadow.setOffsetY(2);
            sprinklerGroup.setEffect(shadow);

            // Add to scene
            anchorPane.getChildren().add(sprinklerGroup);

            // Add water particles if active
            if (isActive) {
                animatePopUpSprinkler(sprinklerGroup, x, y - 18); // Water comes from the top of the head
            }

        } catch (Exception e) {
            logger.error("Error creating pop-up sprinkler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create an impact style sprinkler (traditional oscillating type)
    private void createImpactSprinkler(double x, double y, boolean isActive) {
        try {
            // Create sprinkler group
            Group sprinklerGroup = new Group();

            // Create base that sits on the ground
            Circle base = new Circle(x, y, 10);
            base.setFill(Color.rgb(50, 50, 50)); // Dark gray

            // Create main body/pipe coming out of base
            Rectangle pipe = new Rectangle(x - 3, y - 25, 6, 25); // Shortened to connect with base
            pipe.setFill(Color.rgb(100, 100, 100)); // Medium gray

            // Create sprinkler head - impact style with distinctive hammer shape
            Circle head = new Circle(x, y - 28, 7);
            head.setFill(Color.rgb(80, 80, 80));
            head.setStroke(Color.rgb(40, 40, 40));
            head.setStrokeWidth(1.5);

            // Add impact arm (the "hammer" part)
            Rectangle arm = new Rectangle(x, y - 33, 15, 3);
            arm.setFill(Color.rgb(70, 70, 70));
            arm.setRotate(20); // Angled slightly

            // Add nozzle
            Polygon nozzle = new Polygon();
            nozzle.getPoints().addAll(
                    x - 4.0, y - 28.0,
                    x - 10.0, y - 31.0,
                    x - 10.0, y - 25.0
            );
            nozzle.setFill(Color.rgb(50, 50, 50));

            // Add all parts to group
            sprinklerGroup.getChildren().addAll(base, pipe, head, arm, nozzle);

            // Add drop shadow
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.5));
            shadow.setRadius(3);
            shadow.setOffsetY(2);
            sprinklerGroup.setEffect(shadow);

            // Add to scene
            anchorPane.getChildren().add(sprinklerGroup);

            // Add animation for the impact arm to rotate
            if (isActive) {
                // Rotate the arm back and forth
                RotateTransition armRotation = new RotateTransition(Duration.seconds(0.8), arm);
                armRotation.setFromAngle(20);  // Starting angle
                armRotation.setToAngle(-40);   // Target angle
                armRotation.setCycleCount(Timeline.INDEFINITE);
                armRotation.setAutoReverse(true);
                armRotation.play();

                // Water animation comes from the nozzle
                animateImpactSprinkler(sprinklerGroup, x - 10, y - 28);
            }

        } catch (Exception e) {
            logger.error("Error creating impact sprinkler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Animation for pop-up style sprinkler
    private void animatePopUpSprinkler(Group sprinklerGroup, double x, double y) {
        // Create a group for water particles
        Group waterGroup = new Group();
        sprinklerGroup.getChildren().add(waterGroup);

        // Create a fan pattern with multiple angles
        double[] angles = {-20, -40, -60, -80, -100, -120, -140};

        // Create a timeline animation that adds water droplets in fan pattern
        Timeline waterAnimation = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    // Add multiple droplets in fan pattern
                    for (double angle : angles) {
                        if (Math.random() > 0.3) { // Random chance to skip some angles for natural variation
                            addWaterDroplet(waterGroup, x, y, angle, 2 + Math.random() * 1.5);
                        }
                    }
                })
        );
        waterAnimation.setCycleCount(Timeline.INDEFINITE);
        waterAnimation.play();

        // Add a pause-resume cycle
        Timeline cycleAnimation = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> {
                    waterAnimation.pause();
                    waterGroup.getChildren().clear();
                }),
                new KeyFrame(Duration.seconds(7), e -> {
                    waterAnimation.play();
                })
        );
        cycleAnimation.setCycleCount(Timeline.INDEFINITE);
        cycleAnimation.play();
    }

    // Animation for impact style sprinkler
    private void animateImpactSprinkler(Group sprinklerGroup, double x, double y) {
        // Create a group for water particles
        Group waterGroup = new Group();
        sprinklerGroup.getChildren().add(waterGroup);

        // Create a sweeping pattern that oscillates
        final double[] currentAngle = {-30}; // Mutable holder
        final double[] direction = {-2}; // Negative for decreasing angle (moving right to left)

        // Create a timeline animation that adds water droplets in oscillating pattern
        Timeline waterAnimation = new Timeline(
                new KeyFrame(Duration.millis(50), event -> {
                    // Update angle based on direction
                    currentAngle[0] += direction[0];

                    // Reverse direction at endpoints
                    if (currentAngle[0] < -140) {
                        direction[0] = 2; // Start moving left to right
                    } else if (currentAngle[0] > -30) {
                        direction[0] = -2; // Start moving right to left
                    }

                    // Add water droplets along current angle
                    for (int i = 0; i < 3; i++) { // Multiple droplets for volume
                        double angleVariation = currentAngle[0] + (Math.random() * 10 - 5); // Small variation
                        addWaterDroplet(waterGroup, x, y, angleVariation, 2.5 + Math.random() * 2);
                    }
                })
        );
        waterAnimation.setCycleCount(Timeline.INDEFINITE);
        waterAnimation.play();

        // Add a pause-resume cycle with different timing than the pop-up sprinkler
        Timeline cycleAnimation = new Timeline(
                new KeyFrame(Duration.seconds(7), e -> {
                    waterAnimation.pause();
                    waterGroup.getChildren().clear();
                }),
                new KeyFrame(Duration.seconds(10), e -> {
                    waterAnimation.play();
                })
        );
        cycleAnimation.setCycleCount(Timeline.INDEFINITE);
        cycleAnimation.play();
    }

    // Enhanced water droplet function with more parameters
    private void addWaterDroplet(Group waterGroup, double x, double y, double angle, double speed) {
        // Randomize droplet size slightly
        double size = 2 + Math.random() * 2;

        // Create a small circle for the water droplet
        Circle droplet = new Circle(size);

        // Vary water color slightly for natural look
        double blueVariation = Math.random() * 0.2;
        droplet.setFill(Color.rgb(
                100,
                180 + (int) (blueVariation * 75),
                255,
                0.6 + Math.random() * 0.3)); // Varying blue with transparency

        // Set initial position at the sprinkler head
        droplet.setCenterX(x);
        droplet.setCenterY(y);

        // Add to the water group
        waterGroup.getChildren().add(droplet);

        // Convert angle to radians
        double radian = Math.toRadians(angle);

        // Create a path for the water particle
        Path path = new Path();

        // Start point
        MoveTo start = new MoveTo(x, y);
        path.getElements().add(start);

        // Create an arc path for natural water movement
        double distance = 80 + (Math.random() * 40); // Varying distance
        double controlX = x + Math.cos(radian) * (distance * 0.5);
        double controlY = y + Math.sin(radian) * (distance * 0.5) + 10; // Add gravity effect
        double endX = x + Math.cos(radian) * distance;
        double endY = y + Math.sin(radian) * distance + 25; // Add more gravity at the end

        QuadCurveTo curve = new QuadCurveTo(controlX, controlY, endX, endY);
        path.getElements().add(curve);

        // Create a path transition
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(speed));
        pathTransition.setNode(droplet);
        pathTransition.setPath(path);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);

        // Fade out the droplet toward the end
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(speed * 0.7), droplet);
        fadeOut.setFromValue(0.7);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(speed * 0.3));

        // Add scale transition to simulate droplet getting smaller
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(speed), droplet);
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.4);
        scaleDown.setToY(0.4);

        // Play the animations in parallel
        ParallelTransition animation = new ParallelTransition(pathTransition, fadeOut, scaleDown);
        animation.setOnFinished(e -> waterGroup.getChildren().remove(droplet));
        animation.play();
    }

    // Create parasite status with path animation
    private void createParasiteStatusWithPathAnimation() {
        try {
            // 1) Remove existing parasite status label and stop its animation if it exists
            if (parasiteStatusLabel != null && anchorPane.getChildren().contains(parasiteStatusLabel)) {
                anchorPane.getChildren().remove(parasiteStatusLabel);
                if (parasitePathTransition != null) {
                    parasitePathTransition.stop();
                }
            }

            // 2) Get window dimensions (or use default if not yet rendered)
            double anchorWidth = anchorPane.getWidth();
            double anchorHeight = anchorPane.getHeight();
            if (anchorWidth <= 0) anchorWidth = 1000;
            if (anchorHeight <= 0) anchorHeight = 700;

            // 3) Calculate Y-position in the middle of the ground
            double groundHeight = 80;
            double labelYPosition = anchorHeight - groundHeight / 2 - 10;

            // 4) Create a larger label with purple background
            Label statusLabel = new Label("No Parasites");
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 20));  // Larger font
            statusLabel.setTextFill(Color.BLACK);                            // White text
            statusLabel.setPadding(new Insets(10, 20, 10, 20));             // Extra padding

            // 5) Add icon
            Image noParasiteImage = new Image(getClass().getResourceAsStream("/images/Parasites/noParasite.png"));
            ImageView parasiteImageView = new ImageView(noParasiteImage);
            parasiteImageView.setFitHeight(24);
            parasiteImageView.setFitWidth(24);
            statusLabel.setGraphic(parasiteImageView);
            statusLabel.setContentDisplay(ContentDisplay.LEFT);

            // 6) Purple background, white border, drop shadow
            statusLabel.setBackground(new Background(new BackgroundFill(
                    Color.LIGHTSTEELBLUE,
                    new CornerRadii(20),
                    Insets.EMPTY
            )));
            statusLabel.setBorder(new Border(new BorderStroke(
                    Color.WHITE,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(20),
                    new BorderWidths(2)
            )));
            statusLabel.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.5)));

            // 7) Create a path (right to left) along the ground area
            Path horizontalPath = new Path();
            MoveTo startPoint = new MoveTo(anchorWidth - 100, labelYPosition);
            LineTo endPoint = new LineTo(350, labelYPosition);
            horizontalPath.getElements().addAll(startPoint, endPoint);

            // 8) Add the label to the scene and store it
            anchorPane.getChildren().add(statusLabel);
            parasiteStatusLabel = statusLabel;

            // 9) Create a PathTransition for slower horizontal movement
            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(40));               // Slower horizontal speed
            pathTransition.setPath(horizontalPath);
            pathTransition.setNode(statusLabel);
            pathTransition.setOrientation(PathTransition.OrientationType.NONE);
            pathTransition.setCycleCount(PathTransition.INDEFINITE);
            pathTransition.setInterpolator(Interpolator.LINEAR);

            // 10) Create a small vertical oscillation (up/down) while moving
            TranslateTransition verticalOscillation = new TranslateTransition(Duration.seconds(2), statusLabel);
            verticalOscillation.setFromY(-5);
            verticalOscillation.setToY(5);
            verticalOscillation.setCycleCount(TranslateTransition.INDEFINITE);
            verticalOscillation.setAutoReverse(true);

            // 11) Store reference to the path transition and start both animations
            parasitePathTransition = pathTransition;
            pathTransition.play();
            verticalOscillation.play();

            logger.info("Parasite status with path animation (ground area) created successfully");
        } catch (Exception e) {
            logger.error("Error creating parasite status with path animation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Animate the status value box across the entire ground with a zigzag path
    private void animateStatusValueAcrossGround(HBox statusValue, double anchorWidth, double anchorHeight, double groundHeight) {
        // Create a zigzag path that follows the contour of the ground
        Path zigzagPath = new Path();

        // Starting point at the right edge to enter from right
        MoveTo startPoint = new MoveTo(anchorWidth, anchorHeight - groundHeight + 30);
        zigzagPath.getElements().add(startPoint);

        // Create a series of zigzag segments across the entire width
        double amplitude = 15; // Height of zigzag
        double segmentWidth = 40; // Width of each zigzag segment

        // Calculate number of segments needed to cover the width
        int numSegments = (int) (anchorWidth / segmentWidth) + 1;

        for (int i = 0; i < numSegments; i++) {
            // Calculate x position for this segment
            double x = anchorWidth - (i * segmentWidth);

            // Adjust y for ground contour at this position
            double baseY = anchorHeight - groundHeight + 30;

            // Apply hill adjustments similar to the grass tufts
            if (x > anchorWidth * 0.7 && x < anchorWidth * 0.9) {
                // Right hill adjustment
                baseY -= 10 * Math.sin((x - anchorWidth * 0.7) / (anchorWidth * 0.2) * Math.PI);
            } else if (x > anchorWidth * 0.2 && x < anchorWidth * 0.4) {
                // Left hill adjustment
                baseY -= 15 * Math.sin((x - anchorWidth * 0.2) / (anchorWidth * 0.2) * Math.PI);
            }

            // Calculate zigzag points
            double downY = baseY + amplitude;
            double midX = x - (segmentWidth / 2);

            // Add zigzag points
            LineTo down = new LineTo(midX, downY);
            zigzagPath.getElements().add(down);

            // Next horizontal point
            LineTo up = new LineTo(x - segmentWidth, baseY);
            zigzagPath.getElements().add(up);
        }

        // Create path transition
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(15)); // Slower movement
        pathTransition.setPath(zigzagPath);
        pathTransition.setNode(statusValue);
        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
        pathTransition.setCycleCount(Timeline.INDEFINITE);
        pathTransition.setAutoReverse(true);
        pathTransition.setInterpolator(Interpolator.LINEAR);

        // Start the animation
        pathTransition.play();
    }

    // Create a zigzag animation for status content within the panel
    private void animateStatusContentZigZag(HBox statusContent, double availableWidth) {
        // Create a zigzag path for the animation
        Path zigzagPath = new Path();

        // Start at right edge (to make content scroll in from right)
        MoveTo startPoint = new MoveTo(availableWidth, 0);
        zigzagPath.getElements().add(startPoint);

        // Create a series of zigzag segments
        double amplitude = 8; // Height of zigzag
        double segmentWidth = 30; // Width of each zigzag segment

        // Calculate number of segments needed to cover twice the available width
        // (so content moves completely off-screen to the left and back)
        int numSegments = (int) (availableWidth * 2 / segmentWidth) + 1;

        for (int i = 0; i < numSegments; i++) {
            // Zigzag down
            LineTo down = new LineTo(
                    availableWidth - (i + 0.5) * segmentWidth,
                    amplitude
            );

            // Zigzag up
            LineTo up = new LineTo(
                    availableWidth - (i + 1) * segmentWidth,
                    0
            );

            zigzagPath.getElements().addAll(down, up);
        }

        // Create path transition
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(12)); // Slower movement
        pathTransition.setPath(zigzagPath);
        pathTransition.setNode(statusContent);
        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
        pathTransition.setCycleCount(Timeline.INDEFINITE);
        pathTransition.setAutoReverse(true);
        pathTransition.setInterpolator(Interpolator.LINEAR);

        // Start the animation
        pathTransition.play();
    }

    // Updated method to show "No Parasites" status
    private void setupParasiteZigzagAnimation() {
        // Add a delay to ensure the anchor pane is fully initialized
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> createZigzagParasiteStatus());
        delay.play();
    }

    private void positionPesticideBoxAtBottomRight(HBox pesticideBox) {
        try {
            // Get the width and height of the anchor pane
            double anchorWidth = anchorPane.getWidth();
            double anchorHeight = anchorPane.getHeight();

            // Use default values if dimensions aren't available yet
            if (anchorWidth <= 0) anchorWidth = 1000;
            if (anchorHeight <= 0) anchorHeight = 700;

            // Calculate position - position it more to the right and up by 50px
            double xPos = anchorWidth / 2 + 150; // Further to the right
            double yPos = anchorHeight - 130;    // Up by 50px from previous position

            // Position the pesticide box
            pesticideBox.setLayoutX(xPos);
            pesticideBox.setLayoutY(yPos);

            logger.info("Pesticide box positioned at adjusted position: " + xPos + ", " + yPos);
        } catch (Exception e) {
            logger.error("Error positioning pesticide box: " + e.getMessage());

            // Fallback position - still on the right side and higher up
            pesticideBox.setLayoutX(700);
            pesticideBox.setLayoutY(630);
        }
    }



    /**
     * Shows a cell bouncing and blurring effect for 30 seconds instead of spray.
     *
     * @param row Row where the pesticide is being applied
     * @param col Column where the pesticide is being applied
     */
    private void showCellBounceAndBlurEffect(int row, int col) {
        Platform.runLater(() -> {
            try {
                // Update the status to active
                updatePesticideStatus("ACTIVE", true);

                // Decrease the pesticide level
                if (pesticideLevelBar != null) {
                    double currentLevel = pesticideLevelBar.getProgress();
                    updatePesticideLevel(currentLevel - 0.05);
                }

                // Find the cell at this position
                StackPane cellPane = null;

                // Search for the cell in the grid
                for (Node node : gridPane.getChildren()) {
                    if (node instanceof StackPane) {
                        Integer nodeRow = GridPane.getRowIndex(node);
                        Integer nodeCol = GridPane.getColumnIndex(node);

                        if (nodeRow != null && nodeCol != null &&
                                nodeRow == row && nodeCol == col) {
                            cellPane = (StackPane) node;
                            break;
                        }
                    }
                }

                // If cell not found, create a new one
                if (cellPane == null) {
                    cellPane = new StackPane();
                    cellPane.setPrefSize(80, 80); // Adjust size as needed

                    // Add light green background
                    cellPane.setStyle("-fx-background-color: rgba(150, 230, 150, 0.4); -fx-background-radius: 5;");

                    gridPane.add(cellPane, col, row);

                    // Make sure it doesn't replace any existing content
                    cellPane.toBack();
                }

                // Make final copy of cellPane for use in lambdas
                final StackPane finalCellPane = cellPane;

                // Store original style if any
                final String originalStyle = finalCellPane.getStyle();

                // Apply initial blur and glow effect
                GaussianBlur blur = new GaussianBlur(0);
                finalCellPane.setEffect(blur);

                // Create a glowing border
                finalCellPane.setStyle(
                        originalStyle +
                                "-fx-border-color: rgba(0, 200, 0, 0.7);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-color: rgba(150, 230, 150, 0.3);" +
                                "-fx-background-radius: 5;"
                );

                // Create the bounce animation
                TranslateTransition bounce = new TranslateTransition(Duration.millis(200), finalCellPane);
                bounce.setFromY(0);
                bounce.setToY(-5);
                bounce.setCycleCount(6); // 3 full bounces
                bounce.setAutoReverse(true);
                bounce.play();

                // Create the blur animation for pulsing effect
                Timeline blurAnimation = new Timeline();

                // Add blur pulsing effect with keyframes
                for (int i = 0; i < 30; i++) { // 30 second duration
                    double time = i * 1.0; // 1 second per pulse

                    // Blur increases
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 0.5),
                                    new KeyValue(blur.radiusProperty(), 5.0)
                            )
                    );

                    // Blur decreases
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 1.0),
                                    new KeyValue(blur.radiusProperty(), 2.0)
                            )
                    );

                    // Border glow increases - using final variables for lambda
                    final int currentIndex = i; // Create a final copy for the lambda
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 0.5),
                                    event -> {
                                        finalCellPane.setStyle(
                                                originalStyle +
                                                        "-fx-border-color: rgba(0, 220, 0, 0.9);" +
                                                        "-fx-border-width: 2;" +
                                                        "-fx-border-radius: 5;" +
                                                        "-fx-background-color: rgba(150, 230, 150, 0.5);" +
                                                        "-fx-background-radius: 5;"
                                        );
                                    }
                            )
                    );

                    // Border glow decreases - using final variables for lambda
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 1.0),
                                    event -> {
                                        finalCellPane.setStyle(
                                                originalStyle +
                                                        "-fx-border-color: rgba(0, 180, 0, 0.7);" +
                                                        "-fx-border-width: 2;" +
                                                        "-fx-border-radius: 5;" +
                                                        "-fx-background-color: rgba(150, 230, 150, 0.3);" +
                                                        "-fx-background-radius: 5;"
                                        );
                                    }
                            )
                    );
                }

                // Set a final keyframe to remove the effect
                blurAnimation.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(30),
                                new KeyValue(blur.radiusProperty(), 0.0)
                        )
                );

                // Play the animations
                blurAnimation.play();

                // Remove effects after 30 seconds
                PauseTransition pauseTransition = new PauseTransition(Duration.seconds(30));
                pauseTransition.setOnFinished(event -> {
                    // Reset cell style
                    finalCellPane.setStyle(originalStyle);
                    finalCellPane.setEffect(null);

                    // Update status back to ready
                    updatePesticideStatus("READY", false);
                });
                pauseTransition.play();

                logger.info("Applied cell bounce and blur effect at row " + row + ", column " + col);
            } catch (Exception e) {
                logger.error("Error showing cell effect: " + e.getMessage());
                e.printStackTrace();

                // Reset status if there was an error
                updatePesticideStatus("READY", false);
            }
        });
    }


    /**
     * Shows a simple, lightweight pesticide spray animation at the specified cell for 20 seconds.
     *
     * @param row Row where the pesticide is being applied
     * @param col Column where the pesticide is being applied
     */
    private void showSimplePesticideSpray(int row, int col) {
        Platform.runLater(() -> {
            try {
                // Update the status to active
                updatePesticideStatus("ACTIVE", true);

                // Decrease the pesticide level by a small amount
                if (pesticideLevelBar != null) {
                    double currentLevel = pesticideLevelBar.getProgress();
                    updatePesticideLevel(currentLevel - 0.05);
                }

                // Create a group for spray particles
                Group sprayGroup = new Group();
                gridPane.add(sprayGroup, col, row);
                GridPane.setHalignment(sprayGroup, HPos.CENTER);
                GridPane.setValignment(sprayGroup, VPos.CENTER);

                // Create a light green mist effect
                for (int i = 0; i < 8; i++) {
                    // Create a circle for the mist
                    Circle mist = new Circle(5 + random.nextDouble() * 12);

                    // Light green with high transparency
                    mist.setFill(Color.rgb(
                            150 + random.nextInt(50),
                            220 + random.nextInt(35),
                            150 + random.nextInt(50),
                            0.15 + random.nextDouble() * 0.15
                    ));

                    // Position randomly around the center
                    double offsetX = -15 + random.nextDouble() * 30;
                    double offsetY = -15 + random.nextDouble() * 30;
                    mist.setCenterX(offsetX);
                    mist.setCenterY(offsetY);

                    // Add to spray group
                    sprayGroup.getChildren().add(mist);

                    // Animate the mist expanding and fading - slower for longer duration
                    ScaleTransition st = new ScaleTransition(Duration.seconds(3), mist);
                    st.setFromX(0.7);
                    st.setFromY(0.7);
                    st.setToX(2.0);
                    st.setToY(2.0);

                    FadeTransition ft = new FadeTransition(Duration.seconds(3), mist);
                    ft.setFromValue(0.3);
                    ft.setToValue(0.1); // Don't fade completely, just to low opacity

                    // Play animations together
                    ParallelTransition pt = new ParallelTransition(st, ft);
                    pt.setOnFinished(e -> {
                        // Create a new mist particle to replace this one - maintains effect for longer
                        Circle newMist = new Circle(10 + random.nextDouble() * 10);
                        newMist.setFill(Color.rgb(
                                150 + random.nextInt(50),
                                220 + random.nextInt(35),
                                150 + random.nextInt(50),
                                0.1 + random.nextDouble() * 0.1
                        ));

                        // Position randomly
                        newMist.setCenterX(-15 + random.nextDouble() * 30);
                        newMist.setCenterY(-15 + random.nextDouble() * 30);

                        // Add to spray group
                        sprayGroup.getChildren().add(newMist);

                        // Animate with fade only
                        FadeTransition fade = new FadeTransition(Duration.seconds(3), newMist);
                        fade.setFromValue(0.2);
                        fade.setToValue(0);
                        fade.play();
                    });
                    pt.play();
                }

                // Remove after 20 seconds as requested
                PauseTransition pauseTransition = new PauseTransition(Duration.seconds(20));
                pauseTransition.setOnFinished(event -> {
                    gridPane.getChildren().remove(sprayGroup);

                    // Update status back to ready
                    updatePesticideStatus("READY", false);
                });
                pauseTransition.play();

            } catch (Exception e) {
                logger.error("Error showing pesticide spray: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }




    // Update the display parasite event handler
    private void handleDisplayParasiteEvent(ParasiteDisplayEvent event) {
        logger.info("Day: " + logDay + " Parasite displayed at row " + event.getRow() +
                " and column " + event.getColumn() + " with name " +
                event.getParasite().getName());

        // Load the image for the parasite
        String imageName = "/images/" + event.getParasite().getImageName();
        Image parasiteImage = new Image(getClass().getResourceAsStream(imageName));
        ImageView parasiteImageView = new ImageView(parasiteImage);

        parasiteImageView.setFitHeight(50);
        parasiteImageView.setFitWidth(50);

        // Use the row and column from the event
        int row = event.getRow();
        int col = event.getColumn();

        // Place the parasite image on the grid with offset
        GridPane.setRowIndex(parasiteImageView, row);
        GridPane.setColumnIndex(parasiteImageView, col);
        GridPane.setHalignment(parasiteImageView, HPos.RIGHT);
        GridPane.setValignment(parasiteImageView, VPos.BOTTOM);
        gridPane.getChildren().add(parasiteImageView);

        // Create a pause transition before applying pesticide
        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        pause.setOnFinished(_ -> {
            // Remove the parasite image
            gridPane.getChildren().remove(parasiteImageView);

            // Show simple pesticide spray
            showSimplePesticideSpray(row, col);
        });

        pause.play();
    }
    // Updated method to handle parasite events

    /**
     * Shows a cell bouncing with yellow dots and light purple effect for 30 seconds.
     *
     * @param row Row where the pesticide is being applied
     * @param col Column where the pesticide is being applied
     */
    private void showCellBounceAndPurpleEffect(int row, int col) {
        Platform.runLater(() -> {
            try {
                // Update the status to active
                updatePesticideStatus("ACTIVE", true);

                // Decrease the pesticide level
                if (pesticideLevelBar != null) {
                    double currentLevel = pesticideLevelBar.getProgress();
                    updatePesticideLevel(currentLevel - 0.05);
                }

                // Find the cell at this position
                StackPane cellPane = null;

                // Search for the cell in the grid
                for (Node node : gridPane.getChildren()) {
                    if (node instanceof StackPane) {
                        Integer nodeRow = GridPane.getRowIndex(node);
                        Integer nodeCol = GridPane.getColumnIndex(node);

                        if (nodeRow != null && nodeCol != null &&
                                nodeRow == row && nodeCol == col) {
                            cellPane = (StackPane) node;
                            break;
                        }
                    }
                }

                // If cell not found, create a new one
                if (cellPane == null) {
                    cellPane = new StackPane();
                    cellPane.setPrefSize(80, 80); // Adjust size as needed

                    // Add light purple background
                    cellPane.setStyle("-fx-background-color: rgba(220, 200, 255, 0.4); -fx-background-radius: 5;");

                    gridPane.add(cellPane, col, row);

                    // Make sure it doesn't replace any existing content
                    cellPane.toBack();
                }

                // Make final copy of cellPane for use in lambdas
                final StackPane finalCellPane = cellPane;

                // Store original style if any
                final String originalStyle = finalCellPane.getStyle();

                // Apply initial blur and glow effect
                GaussianBlur blur = new GaussianBlur(0);
                finalCellPane.setEffect(blur);

                // Create a glowing border with light purple
                finalCellPane.setStyle(
                        originalStyle +
                                "-fx-border-color: rgba(180, 120, 220, 0.7);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-color: rgba(220, 200, 255, 0.3);" +
                                "-fx-background-radius: 5;"
                );

                // Create a more noticeable bounce animation
                TranslateTransition bounce = new TranslateTransition(Duration.millis(150), finalCellPane);
                bounce.setFromY(0);
                bounce.setToY(-10); // More pronounced bounce
                bounce.setCycleCount(10); // More bounces
                bounce.setAutoReverse(true);
                bounce.setInterpolator(Interpolator.EASE_BOTH); // Smoother bounce
                bounce.play();

                // Create group for yellow dots
                Group dotsGroup = new Group();
                finalCellPane.getChildren().add(dotsGroup);

                // Add initial yellow dots
                addYellowDots(dotsGroup, 10);

                // Animation to continuously add and fade dots
                Timeline dotsAnimation = new Timeline(
                        new KeyFrame(Duration.millis(800), event -> {
                            // Add new dots periodically
                            addYellowDots(dotsGroup, 5);
                        })
                );
                dotsAnimation.setCycleCount(36); // 36 cycles × 800ms = ~29 seconds
                dotsAnimation.play();

                // Create the blur animation for pulsing effect
                Timeline blurAnimation = new Timeline();

                // Add blur pulsing effect with keyframes
                for (int i = 0; i < 30; i++) { // 30 second duration
                    double time = i * 1.0; // 1 second per pulse

                    // Blur increases
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 0.5),
                                    new KeyValue(blur.radiusProperty(), 5.0)
                            )
                    );

                    // Blur decreases
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 1.0),
                                    new KeyValue(blur.radiusProperty(), 2.0)
                            )
                    );

                    // Border glow increases - using final variables for lambda
                    final int currentIndex = i; // Create a final copy for the lambda
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 0.5),
                                    event -> {
                                        finalCellPane.setStyle(
                                                originalStyle +
                                                        "-fx-border-color: rgba(200, 120, 255, 0.9);" +
                                                        "-fx-border-width: 2;" +
                                                        "-fx-border-radius: 5;" +
                                                        "-fx-background-color: rgba(220, 200, 255, 0.5);" +
                                                        "-fx-background-radius: 5;"
                                        );
                                    }
                            )
                    );

                    // Border glow decreases - using final variables for lambda
                    blurAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 1.0),
                                    event -> {
                                        finalCellPane.setStyle(
                                                originalStyle +
                                                        "-fx-border-color: rgba(160, 100, 200, 0.7);" +
                                                        "-fx-border-width: 2;" +
                                                        "-fx-border-radius: 5;" +
                                                        "-fx-background-color: rgba(220, 200, 255, 0.3);" +
                                                        "-fx-background-radius: 5;"
                                        );
                                    }
                            )
                    );
                }

                // Set a final keyframe to remove the effect
                blurAnimation.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(30),
                                new KeyValue(blur.radiusProperty(), 0.0)
                        )
                );

                // Play the animations
                blurAnimation.play();

                // Remove effects after 30 seconds
                PauseTransition pauseTransition = new PauseTransition(Duration.seconds(30));
                pauseTransition.setOnFinished(event -> {
                    // Reset cell style
                    finalCellPane.setStyle(originalStyle);
                    finalCellPane.setEffect(null);

                    // Remove the dots group
                    finalCellPane.getChildren().remove(dotsGroup);

                    // Update status back to ready
                    updatePesticideStatus("READY", false);
                });
                pauseTransition.play();

                logger.info("Applied cell bounce and purple effect at row " + row + ", column " + col);
            } catch (Exception e) {
                logger.error("Error showing cell effect: " + e.getMessage());
                e.printStackTrace();

                // Reset status if there was an error
                updatePesticideStatus("READY", false);
            }
        });
    }







    private void positionPesticideBoxExactLocation(Pane pesticideBox) {
        try {
            // Get the width and height of the anchor pane
            double anchorWidth = anchorPane.getWidth();
            double anchorHeight = anchorPane.getHeight();

            // Use default values if dimensions aren't available yet
            if (anchorWidth <= 0) anchorWidth = 1000;
            if (anchorHeight <= 0) anchorHeight = 700;

            // Calculate position - SHIFT LEFT BY 80px AND UP BY 30px
            double xPos = anchorWidth - 150 - 230; // From right edge, shifted 80px left
            double yPos = anchorHeight -108 - 80; // From bottom, shifted 30px up

            // Position the pesticide box
            pesticideBox.setLayoutX(xPos);
            pesticideBox.setLayoutY(yPos);

            logger.info("Pesticide box positioned at adjusted location: " + xPos + ", " + yPos);
        } catch (Exception e) {
            logger.error("Error positioning pesticide box: " + e.getMessage());

            // Fallback position - also adjusted
            pesticideBox.setLayoutX(770); // 850 - 80
            pesticideBox.setLayoutY(550); // 580 - 30
        }
    }

    /**
     * Shows a particle burst effect when refilling the pesticide.
     */
    private void showRefillParticles() {
        // Find the position of the pesticide box
        double pestX = 0;
        double pestY = 0;

        // Try to find the box by looking for the level bar
        if (pesticideLevelBar != null) {
            try {
                Bounds bounds = pesticideLevelBar.localToScene(pesticideLevelBar.getBoundsInLocal());
                pestX = bounds.getMinX() + bounds.getWidth() / 2;
                pestY = bounds.getMinY() + bounds.getHeight() / 2;
            } catch (Exception e) {
                // Use fallback position
                pestX = anchorPane.getWidth() - 100;
                pestY = anchorPane.getHeight() - 1200;
            }
        }

        // Create a group for particles
        Group particleGroup = new Group();
        anchorPane.getChildren().add(particleGroup);

        // Generate particles
        final double centerX = pestX;
        final double centerY = pestY;

        // Create particles
        for (int i = 0; i < 20; i++) {
            // Create a small circle for the particle
            Circle particle = new Circle(2 + random.nextDouble() * 3);

            // Random color between blue and purple
            Color color = Color.rgb(
                    100 + random.nextInt(100), // R
                    100 + random.nextInt(100), // G
                    200 + random.nextInt(55),  // B
                    0.7 + random.nextDouble() * 0.3 // Alpha
            );

            particle.setFill(color);

            // Position at the center
            particle.setCenterX(centerX);
            particle.setCenterY(centerY);

            // Add to the group
            particleGroup.getChildren().add(particle);

            // Calculate random direction
            double angle = random.nextDouble() * 360;
            double distance = 30 + random.nextDouble() * 50;

            // Calculate end position
            double endX = centerX + distance * Math.cos(Math.toRadians(angle));
            double endY = centerY + distance * Math.sin(Math.toRadians(angle));

            // Create movement animation
            Timeline timeline = new Timeline();

            // Add keyframes for movement and fading
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(particle.centerXProperty(), centerX),
                            new KeyValue(particle.centerYProperty(), centerY),
                            new KeyValue(particle.opacityProperty(), 1.0)
                    )
            );

            // Move in an arc rather than straight line
            // Add more keyframes for curved motion
            for (int j = 1; j < 10; j++) {
                double t = j / 10.0;
                double arcX = centerX + (endX - centerX) * t;
                double arcY = centerY + (endY - centerY) * t - Math.sin(Math.PI * t) * 20; // Arc upward

                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.millis(800 * t),
                                new KeyValue(particle.centerXProperty(), arcX),
                                new KeyValue(particle.centerYProperty(), arcY)
                        )
                );
            }

            // Final position with fade out
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(800),
                            new KeyValue(particle.centerXProperty(), endX),
                            new KeyValue(particle.centerYProperty(), endY),
                            new KeyValue(particle.opacityProperty(), 0.0)
                    )
            );

            // Play the animation
            timeline.play();

            // Remove particle and group when done
            timeline.setOnFinished(e -> {
                particleGroup.getChildren().remove(particle);
                if (particleGroup.getChildren().isEmpty()) {
                    anchorPane.getChildren().remove(particleGroup);
                }
            });
        }
    }



    private void addYellowDots(Group dotsGroup, int count) {
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            // Create a small yellow dot with varied sizes
            Circle dot = new Circle(1.5 + random.nextDouble() * 2.5);

            // Light yellow color with transparency
            dot.setFill(Color.rgb(
                    255,
                    255,
                    150 + random.nextInt(105), // From light yellow to almost white
                    0.7 + random.nextDouble() * 0.3
            ));

            // Position randomly within the cell
            dot.setCenterX(-30 + random.nextDouble() * 60);
            dot.setCenterY(-30 + random.nextDouble() * 60);

            // Add dot to the group
            dotsGroup.getChildren().add(dot);

            // Create fade animation
            FadeTransition fade = new FadeTransition(Duration.seconds(2 + random.nextDouble() * 3), dot);
            fade.setFromValue(0.9);
            fade.setToValue(0);
            fade.setOnFinished(e -> dotsGroup.getChildren().remove(dot));

            // Add slight drift upward with more natural movement
            TranslateTransition drift = new TranslateTransition(Duration.seconds(2 + random.nextDouble() * 3), dot);
            drift.setByY(-15 - random.nextDouble() * 20); // More upward movement
            drift.setByX(-5 + random.nextDouble() * 10);
            drift.setInterpolator(Interpolator.SPLINE(0.1, 0.9, 0.3, 0.7)); // Custom interpolator for smoother motion

            // Play animations together
            ParallelTransition pt = new ParallelTransition(fade, drift);
            pt.play();
        }
    }

    // Modified pestControl method to use the enhanced cell effect
    private void pestControl(String imagePestControlName, int row, int col) {
        // Use the enhanced cell effect with yellow dots and application trail
        showEnhancedCellEffect(row, col);
    }

    /**
     * Updates the pesticide system status.
     *
     * @param status   The new status text (e.g., "READY" or "ACTIVE")
     * @param isActive Whether the system is currently active
     */
    public void updatePesticideStatus(String status, boolean isActive) {
        if (pesticideStatusLabel != null) {
            Platform.runLater(() -> {
                // Update the text
                pesticideStatusLabel.setText(status);

                // Update color based on status
                if (isActive) {
                    pesticideStatusLabel.setTextFill(Color.rgb(200, 0, 0)); // Red for ACTIVE

                    // Add a pulsing animation when active
                    Timeline pulse = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(pesticideStatusLabel.scaleXProperty(), 1)),
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(pesticideStatusLabel.scaleYProperty(), 1)),
                            new KeyFrame(Duration.millis(500),
                                    new KeyValue(pesticideStatusLabel.scaleXProperty(), 1.1)),
                            new KeyFrame(Duration.millis(500),
                                    new KeyValue(pesticideStatusLabel.scaleYProperty(), 1.1)),
                            new KeyFrame(Duration.millis(1000),
                                    new KeyValue(pesticideStatusLabel.scaleXProperty(), 1)),
                            new KeyFrame(Duration.millis(1000),
                                    new KeyValue(pesticideStatusLabel.scaleYProperty(), 1))
                    );
                    pulse.setCycleCount(Timeline.INDEFINITE);

                    // Store the animation for stopping later
                    pesticideStatusAnimation = pulse;
                    pulse.play();

                } else {
                    pesticideStatusLabel.setTextFill(Color.rgb(0, 100, 0)); // Green for READY

                    // Stop any existing animation
                    if (pesticideStatusAnimation != null) {
                        pesticideStatusAnimation.stop();
                        pesticideStatusAnimation = null;

                        // Reset the scale
                        pesticideStatusLabel.setScaleX(1.0);
                        pesticideStatusLabel.setScaleY(1.0);
                    }
                }
            });
        }
    }

    /**
     * Updates the pesticide level indicator with improved visual feedback.
     *
     * @param level The new level value (0.0 to 1.0)
     */
    public void updatePesticideLevel(double level) {
        if (pesticideLevelBar != null) {
            Platform.runLater(() -> {
                // Get current level for animation
                double currentLevel = pesticideLevelBar.getProgress();
                double targetLevel = Math.max(0.0, Math.min(1.0, level)); // Ensure within range

                // Create animation to transition to new level
                Timeline levelAnimation = new Timeline();

                // Add keyframes for smooth transition
                for (int i = 0; i <= 10; i++) {
                    double progress = i / 10.0;
                    double intermediateLevel = currentLevel + (targetLevel - currentLevel) * progress;

                    levelAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.millis(i * 30),
                                    new KeyValue(pesticideLevelBar.progressProperty(), intermediateLevel)
                            )
                    );
                }

                // Change color based on level with purple theme
                if (targetLevel < 0.3) {
                    // Red-pink for low level
                    levelAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.millis(300),
                                    new KeyValue(pesticideLevelBar.styleProperty(), "-fx-accent: #FF6EB4;")
                            )
                    );
                } else if (targetLevel < 0.6) {
                    // Medium purple for medium level
                    levelAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.millis(300),
                                    new KeyValue(pesticideLevelBar.styleProperty(), "-fx-accent: #BA55D3;")
                            )
                    );
                } else {
                    // Darker purple for high level
                    levelAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.millis(300),
                                    new KeyValue(pesticideLevelBar.styleProperty(), "-fx-accent: #9370DB;")
                            )
                    );
                }

                // Play the animation
                levelAnimation.play();
            });
        }
    }
    /**
     * Creates an enhanced vertical pesticide system status box with
     * better styling to match the screenshot.
     */
    private void createEnhancedVerticalPesticideBox() {
        Platform.runLater(() -> {
            try {
                // Create the main container - using VBox for vertical layout
                VBox pesticideBox = new VBox(10); // Increased spacing for better layout
                pesticideBox.setPadding(new Insets(12));
                pesticideBox.setPrefWidth(120); // Slightly wider than before
                pesticideBox.setPrefHeight(100); // Taller for vertical layout
                pesticideBox.setAlignment(Pos.CENTER);

                // Style with light purple background and border (matching screenshot)
                pesticideBox.setBackground(new Background(new BackgroundFill(
                        Color.rgb(240, 220, 235, 0.9), // Slightly more opaque to match screenshot
                        new CornerRadii(10),
                        Insets.EMPTY
                )));

                pesticideBox.setBorder(new Border(new BorderStroke(
                        Color.rgb(160, 120, 200, 0.7), // Purple border
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(10),
                        new BorderWidths(2)
                )));

                // Add icon at the top


                ImageView pesticideIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/pControl1.png")));
                pesticideIcon.setFitHeight(32);
                pesticideIcon.setFitWidth(28);

                // Add title - "PESTICIDE" (shortened from "PESTICIDE SYSTEM")
                Label titleLabel = new Label("PESTICIDE");
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                titleLabel.setTextFill(Color.rgb(80, 40, 120)); // Darker purple text
                HBox titleContainer = new HBox(5);
                titleContainer.setAlignment(Pos.CENTER);
                titleContainer.getChildren().addAll(pesticideIcon, titleLabel);
                // Add status label
                Label statusLabel = new Label("READY");
                statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                statusLabel.setTextFill(Color.rgb(0, 100, 0)); // Green for READY state
                statusLabel.setPadding(new Insets(5, 0, 5, 0)); // Add padding above and below

                // Add level label
                Label levelLabel = new Label("LEVEL");
                levelLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
                levelLabel.setTextFill(Color.rgb(80, 40, 120)); // Purple text

                VBox progressContainer = new VBox(2);
                progressContainer.setPadding(new Insets(2, 0, 8, 0)); // Spacing above and below
                progressContainer.setAlignment(Pos.CENTER); // Center align the level bar

// Add level indicator (progress bar)
                ProgressBar levelBar = new ProgressBar(0.8); // Start at 80%
                levelBar.setPrefWidth(90); // Slightly narrower to fit better
                levelBar.setPrefHeight(10); // Smaller height
                levelBar.setMinHeight(10); // Ensure minimum height is set
                levelBar.setMaxHeight(10); // Ensure maximum height is set
                levelBar.setStyle("-fx-accent: #9370DB;"); // Medium purple progress color

// Make progress bar visible
                levelBar.setVisible(true);
                levelBar.setManaged(true);

                progressContainer.getChildren().add(levelBar);
                // Add a refill button with improved styling
                Button refillButton = new Button("Refill");
                refillButton.setPrefWidth(80);
                refillButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #f0e6ff, #d8c6ff);" +
                                "-fx-border-color: #9370DB;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 5;" +
                                "-fx-text-fill: #4B0082;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 5 10 5 10;" +
                                "-fx-cursor: hand;"
                );

                // Define styles as constants to avoid lambda issues
                final String normalStyle =
                        "-fx-background-color: linear-gradient(to bottom, #f0e6ff, #d8c6ff);" +
                                "-fx-border-color: #9370DB;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 5;" +
                                "-fx-text-fill: #4B0082;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 5 10 5 10;" +
                                "-fx-cursor: hand;";

                final String hoverStyle =
                        "-fx-background-color: linear-gradient(to bottom, #e0d0ff, #c0a0ff);" +
                                "-fx-border-color: #9370DB;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 5;" +
                                "-fx-text-fill: #4B0082;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 5 10 5 10;" +
                                "-fx-cursor: hand;";

                // Add hover effect using the constant styles
                refillButton.setOnMouseEntered(e -> {
                    refillButton.setStyle(hoverStyle);
                });

                refillButton.setOnMouseExited(e -> {
                    refillButton.setStyle(normalStyle);
                });

                // Store references for the lambda expressions
                final Label finalStatusLabel = statusLabel;
                final ProgressBar finalLevelBar = levelBar;

                // Handle refill button click - improved with animation
                refillButton.setOnAction(e -> {
                    // Create a filling animation
                    Timeline fillAnimation = new Timeline();

                    // Current progress value - must be effectively final
                    final double currentValue = finalLevelBar.getProgress();

                    // Add keyframes for smooth filling animation
                    for (int i = 0; i <= 20; i++) { // More steps for smoother animation
                        // Need to make i effectively final for use in lambda
                        final int index = i;

                        double newValue = currentValue + ((1.0 - currentValue) * index / 20.0);
                        fillAnimation.getKeyFrames().add(
                                new KeyFrame(Duration.millis(index * 30),
                                        new KeyValue(finalLevelBar.progressProperty(), newValue)
                                )
                        );

                        // Change color during animation - avoiding lambda issues
                        if (index < 10) {
                            final String colorStyle = "-fx-accent: #" + String.format("%02x", 147 + index * 5) + "70DB;";
                            fillAnimation.getKeyFrames().add(
                                    new KeyFrame(Duration.millis(index * 30),
                                            new KeyValue(finalLevelBar.styleProperty(), colorStyle)
                                    )
                            );
                        } else {
                            final String colorStyle = "-fx-accent: #9370" + String.format("%02x", 219 - (index-10) * 5) + ";";
                            fillAnimation.getKeyFrames().add(
                                    new KeyFrame(Duration.millis(index * 30),
                                            new KeyValue(finalLevelBar.styleProperty(), colorStyle)
                                    )
                            );
                        }
                    }

                    // Final color
                    fillAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.millis(600),
                                    new KeyValue(finalLevelBar.styleProperty(), "-fx-accent: #9370DB;")
                            )
                    );

                    // Add a slight scaling animation for feedback
                    ScaleTransition scale = new ScaleTransition(Duration.millis(200), refillButton);
                    scale.setFromX(1.0);
                    scale.setFromY(1.0);
                    scale.setToX(0.95);
                    scale.setToY(0.95);
                    scale.setCycleCount(2);
                    scale.setAutoReverse(true);

                    // Play the animations
                    fillAnimation.play();
                    scale.play();

                    // Update status temporarily - need to use a final copy
                    final String oldStatus = finalStatusLabel.getText();
                    finalStatusLabel.setText("REFILLING");
                    finalStatusLabel.setTextFill(Color.rgb(0, 100, 200)); // Blue during refill

                    // Add visual feedback with a particle burst
                    showRefillParticles();

                    // Reset status after a short delay
                    PauseTransition statusReset = new PauseTransition(Duration.millis(700));
                    statusReset.setOnFinished(event -> {
                        finalStatusLabel.setText(oldStatus);
                        finalStatusLabel.setTextFill(Color.rgb(0, 100, 0)); // Back to green
                    });
                    statusReset.play();

                    logger.info("Pesticide refilled to 100%");
                });

                // Add all components to the vertical box
                pesticideBox.getChildren().addAll(
                       titleContainer,
                        statusLabel,
                        levelLabel,
                        progressContainer,
                        refillButton
                );

                // Add drop shadow
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.rgb(0, 0, 0, 0.3));
                shadow.setRadius(8);
                shadow.setOffsetY(3);
                pesticideBox.setEffect(shadow);

                // Add to the scene
                anchorPane.getChildren().add(pesticideBox);

                // Position it at the adjusted position - exactly where shown in screenshot
                // Using exact coordinates from the screenshot
                positionPesticideBoxExactLocation(pesticideBox);

                // Store references for later updates
                this.pesticideStatusLabel = statusLabel;
                this.pesticideLevelBar = levelBar;

                logger.info("Enhanced vertical pesticide system box created");
            } catch (Exception e) {
                logger.error("Error creating pesticide status box: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Shows a cell bouncing with yellow dots and light purple effect for 30 seconds,
     * with enhanced visual feedback.
     * @param row Row where the pesticide is being applied
     * @param col Column where the pesticide is being applied
     */
    private void showEnhancedCellEffect(int row, int col) {
        Platform.runLater(() -> {
            try {
                // Show "ACTIVE" status
                updatePesticideStatus("ACTIVE", true);

                // Decrease the pesticide level
                if (pesticideLevelBar != null) {
                    double currentLevel = pesticideLevelBar.getProgress();
                    updatePesticideLevel(currentLevel - 0.1); // Larger decrease for more visual feedback
                }

                // Display application particles from pesticide box to cell
                showApplicationTrail(row, col);

                // Find the cell at this position
                StackPane cellPane = null;

                // Search for the cell in the grid
                for (Node node : gridPane.getChildren()) {
                    if (node instanceof StackPane) {
                        Integer nodeRow = GridPane.getRowIndex(node);
                        Integer nodeCol = GridPane.getColumnIndex(node);

                        if (nodeRow != null && nodeCol != null &&
                                nodeRow == row && nodeCol == col) {
                            cellPane = (StackPane) node;
                            break;
                        }
                    }
                }

                // If cell not found, create a new one
                if (cellPane == null) {
                    cellPane = new StackPane();
                    cellPane.setPrefSize(80, 80);

                    // Add light purple background
                    cellPane.setStyle("-fx-background-color: rgba(220, 200, 255, 0.4); -fx-background-radius: 5;");

                    gridPane.add(cellPane, col, row);

                    // Make sure it doesn't replace any existing content
                    cellPane.toBack();
                }

                // Make final copy of cellPane for use in lambdas
                final StackPane finalCellPane = cellPane;

                // Store original style if any
                final String originalStyle = finalCellPane.getStyle();

                // Apply initial blur and glow effect
                GaussianBlur blur = new GaussianBlur(0);
                finalCellPane.setEffect(blur);

                // Create a glowing border with initial flash
                // Use string constants to avoid lambda issues
                final String flashStyle =
                        "-fx-border-color: rgba(255, 255, 255, 0.9);" +
                                "-fx-border-width: 3;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-color: rgba(255, 255, 255, 0.5);" +
                                "-fx-background-radius: 5;";

                final String purpleGlowStyle =
                        originalStyle +
                                "-fx-border-color: rgba(180, 120, 220, 0.8);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-color: rgba(220, 200, 255, 0.3);" +
                                "-fx-background-radius: 5;";

                // Apply initial style
                finalCellPane.setStyle(flashStyle);

                // Transition to purple glow
                PauseTransition initialFlash = new PauseTransition(Duration.millis(150));
                initialFlash.setOnFinished(e -> {
                    finalCellPane.setStyle(purpleGlowStyle);
                });
                initialFlash.play();

                // Create a more pronounced bounce animation
                TranslateTransition bounce = new TranslateTransition(Duration.millis(120), finalCellPane);
                bounce.setFromY(0);
                bounce.setToY(-12); // More pronounced bounce
                bounce.setCycleCount(8); // Fewer but more noticeable bounces
                bounce.setAutoReverse(true);
                bounce.setInterpolator(Interpolator.SPLINE(0.1, 0.9, 0.2, 0.8)); // Custom interpolator for better bounce
                bounce.play();

                // Create group for yellow dots
                Group dotsGroup = new Group();
                finalCellPane.getChildren().add(dotsGroup);

                // Add initial yellow dots
                addYellowDots(dotsGroup, 15); // More initial dots

                // Create a dots timeline for periodic additions of dots
                // Using KeyFrame without lambda expressions
                EventHandler<ActionEvent> dotsHandler = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        addYellowDots(dotsGroup, 5);
                    }
                };

                Timeline dotsAnimation = new Timeline(
                        new KeyFrame(Duration.millis(600), dotsHandler)
                );
                dotsAnimation.setCycleCount(45); // 45 cycles × 600ms = ~27 seconds
                dotsAnimation.play();

                // Create style strings for the glow animation to avoid lambda issues
                final String glowStyleBright =
                        originalStyle +
                                "-fx-border-color: rgba(220, 120, 255, 0.9);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-color: rgba(220, 200, 255, 0.4);" +
                                "-fx-background-radius: 5;";

                final String glowStyleDim =
                        originalStyle +
                                "-fx-border-color: rgba(160, 100, 200, 0.7);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-color: rgba(220, 200, 255, 0.3);" +
                                "-fx-background-radius: 5;";

                // Create a purple pulsing glow animation using safely constructed timelines
                Timeline glowAnimation = new Timeline();

                // Add blur pulsing effect with keyframes
                for (int i = 0; i < 15; i++) { // 15 pulses over 30 seconds
                    double time = i * 2.0; // 2 seconds per pulse

                    // Blur increases
                    glowAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 0.5),
                                    new KeyValue(blur.radiusProperty(), 5.0)
                            )
                    );

                    // Blur decreases
                    glowAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(time + 1.0),
                                    new KeyValue(blur.radiusProperty(), 2.0)
                            )
                    );

                    // Glow increases - using frames not lambdas
                    final double brightTime = time + 1.0;
                    glowAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(brightTime),
                                    new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            finalCellPane.setStyle(glowStyleBright);
                                        }
                                    }
                            )
                    );

                    // Glow decreases - using frames not lambdas
                    final double dimTime = time + 2.0;
                    glowAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(dimTime),
                                    new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            finalCellPane.setStyle(glowStyleDim);
                                        }
                                    }
                            )
                    );
                }

                // Create a final fade out animation
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.0), dotsGroup);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                KeyValue blurEnd = new KeyValue(blur.radiusProperty(), 0.0);
                Timeline blurRemoval = new Timeline(new KeyFrame(Duration.seconds(1.0), blurEnd));

                // Add all animations to a sequential transition
                SequentialTransition sequence = new SequentialTransition(
                        glowAnimation,
                        new ParallelTransition(fadeOut, blurRemoval)
                );

                // Play the sequence
                sequence.play();

                // Remove effects after 30 seconds
                PauseTransition pauseTransition = new PauseTransition(Duration.seconds(30));
                pauseTransition.setOnFinished(event -> {
                    // Reset cell style
                    finalCellPane.setStyle(originalStyle);
                    finalCellPane.setEffect(null);

                    // Remove the dots group
                    finalCellPane.getChildren().remove(dotsGroup);

                    // Update status back to ready
                    updatePesticideStatus("READY", false);
                });
                pauseTransition.play();

                logger.info("Applied enhanced cell effect at row " + row + ", column " + col);
            } catch (Exception e) {
                logger.error("Error showing cell effect: " + e.getMessage());
                e.printStackTrace();

                // Reset status if there was an error
                updatePesticideStatus("READY", false);
            }
        });
    }

    /**
     * Shows an animation trail from pesticide box to the target cell.
     * Uses proper final variables to avoid lambda issues.
     */
    private void showApplicationTrail(final int targetRow, final int targetCol) {
        try {
            // Get pesticide box position (approximated from level bar)
            double startX = 0;
            double startY = 0;

            if (pesticideLevelBar != null) {
                try {
                    Bounds bounds = pesticideLevelBar.localToScene(pesticideLevelBar.getBoundsInLocal());
                    startX = bounds.getMinX() + bounds.getWidth() / 2;
                    startY = bounds.getMinY() + bounds.getHeight() / 2;
                } catch (Exception e) {
                    // Fallback to approximate position
                    startX = anchorPane.getWidth() - 100;
                    startY = anchorPane.getHeight() - 150;
                }
            } else {
                // Fallback to approximate position
                startX = anchorPane.getWidth() - 100;
                startY = anchorPane.getHeight() - 150;
            }

            // Get target cell position
            double endX = 0;
            double endY = 0;

            // Find the node at the target position in the grid
            for (Node node : gridPane.getChildren()) {
                if (node instanceof StackPane) {
                    Integer nodeRow = GridPane.getRowIndex(node);
                    Integer nodeCol = GridPane.getColumnIndex(node);

                    if (nodeRow != null && nodeCol != null &&
                            nodeRow == targetRow && nodeCol == targetCol) {
                        // Get the target cell's position
                        Bounds bounds = node.localToScene(node.getBoundsInLocal());
                        endX = bounds.getMinX() + bounds.getWidth() / 2;
                        endY = bounds.getMinY() + bounds.getHeight() / 2;
                        break;
                    }
                }
            }

            // If we couldn't find the cell, use an approximation
            if (endX == 0 && endY == 0) {
                // Approximated grid position
                endX = gridPane.getLayoutX() + targetCol * 80 + 40;
                endY = gridPane.getLayoutY() + targetRow * 80 + 40;
            }

            // Make variables effectively final
            final double finalStartX = startX;
            final double finalStartY = startY;
            final double finalEndX = endX;
            final double finalEndY = endY;

            // Determine a curved path between the points
            // (using a quadratic curve for natural arc)
            final double controlX = (finalStartX + finalEndX) / 2 - (finalEndY - finalStartY) * 0.3; // Offset for curve
            final double controlY = (finalStartY + finalEndY) / 2 - (finalStartX - finalEndX) * 0.3; // Offset for curve

            // Now create the particle trail
            final Group trailGroup = new Group();
            anchorPane.getChildren().add(trailGroup);

            // Create multiple particles that follow this path
            for (int i = 0; i < 40; i++) { // 40 particles for a dense trail
                final int particleIndex = i; // Make i effectively final

                // Delayed start for each particle
                PauseTransition delay = new PauseTransition(Duration.millis(particleIndex * 15));

                delay.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        createTrailParticle(
                                trailGroup,
                                finalStartX,
                                finalStartY,
                                finalEndX,
                                finalEndY,
                                controlX,
                                controlY,
                                particleIndex
                        );
                    }
                });

                delay.play();
            }

        } catch (Exception e) {
            logger.error("Error showing application trail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a single particle for the application trail.
     * Extracted to separate method to avoid lambda issues.
     */
    private void createTrailParticle(
            final Group trailGroup,
            final double startX,
            final double startY,
            final double endX,
            final double endY,
            final double controlX,
            final double controlY,
            final int index
    ) {
        // Create a particle
        Circle particle = new Circle(2 + random.nextDouble() * 2);

        // Vary colors between purple and yellow
        Color color;
        if (index % 3 == 0) {
            // Yellow
            color = Color.rgb(
                    255,
                    255,
                    150 + random.nextInt(105),
                    0.7 + random.nextDouble() * 0.3
            );
        } else {
            // Purple
            color = Color.rgb(
                    180 + random.nextInt(75),
                    120 + random.nextInt(80),
                    220 + random.nextInt(35),
                    0.7 + random.nextDouble() * 0.3
            );
        }

        particle.setFill(color);

        // Add to the group
        trailGroup.getChildren().add(particle);

        // Create a path using quadratic curve
        Path path = new Path();
        MoveTo moveTo = new MoveTo(startX, startY);
        QuadCurveTo quadTo = new QuadCurveTo(controlX, controlY, endX, endY);
        path.getElements().addAll(moveTo, quadTo);

        // Set up the path transition
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(600));
        pathTransition.setPath(path);
        pathTransition.setNode(particle);

        // Add a fade out toward the end
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), particle);
        fadeOut.setFromValue(0.8);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(400));

        // Add slight growth animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), particle);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.5);
        scale.setToY(1.5);

        // Use a final reference to the group for the cleanup handler
        final Group finalGroup = trailGroup;

        // Play the animations with an event handler instead of a lambda
        ParallelTransition pt = new ParallelTransition(pathTransition, fadeOut, scale);
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                finalGroup.getChildren().remove(particle);
                if (finalGroup.getChildren().isEmpty()) {
                    anchorPane.getChildren().remove(finalGroup);
                }
            }
        });
        pt.play();
    }

}
