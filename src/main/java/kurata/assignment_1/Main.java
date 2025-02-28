package kurata.assignment_1;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private int currentImageIndex = 0;
    private File[] imageFiles;
    private ImageView fullImageView;
    private StackPane root;
    private VBox thumbnailGrid;
    private BorderPane fullView;
    private VBox albumSelectionScreen;
    private String currentAlbum;
    private final Map<String, String> albumPaths = new HashMap<>();
    private final Map<String, Image> imageCache = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Gallery");

        // Define album paths
        albumPaths.put("Cars", "C:/Users/kurat/OneDrive/Documentos/Assignment_1/src/images/cars");
        albumPaths.put("Food", "C:/Users/kurat/OneDrive/Documentos/Assignment_1/src/images/food");
        albumPaths.put("Shoes", "C:/Users/kurat/OneDrive/Documentos/Assignment_1/src/images/shoes");

        // Create album selection screen
        albumSelectionScreen = createAlbumSelectionScreen();
        root = new StackPane(albumSelectionScreen);

        Scene scene = new Scene(root, 800, 600);

        // Load the CSS file
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createAlbumSelectionScreen() {
        VBox albumBox = new VBox(20);
        albumBox.setAlignment(Pos.CENTER);
        albumBox.setPadding(new Insets(20));

        Label title = new Label("Select an Album");
        title.getStyleClass().add("heading");

        HBox albumButtons = new HBox(20);
        albumButtons.setAlignment(Pos.CENTER);

        for (String album : albumPaths.keySet()) {
            VBox albumContainer = new VBox(10);
            albumContainer.setAlignment(Pos.CENTER);

            ImageView albumIcon = new ImageView(new Image("file:C:/Users/kurat/OneDrive/Documentos/Assignment_1/src/images/icon.jpg", 100, 100, true, true));
            Button albumButton = new Button(album);
            albumButton.getStyleClass().add("button");
            albumButton.setOnAction(e -> loadAlbum(album));

            albumContainer.getChildren().addAll(albumIcon, albumButton);
            albumButtons.getChildren().add(albumContainer);
        }

        albumBox.getChildren().addAll(title, albumButtons);
        return albumBox;
    }

    private void loadAlbum(String album) {
        currentAlbum = album;
        File folder = new File(albumPaths.get(album));
        if (folder.exists() && folder.isDirectory()) {
            imageFiles = folder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") ||
                            name.toLowerCase().endsWith(".jpg") ||
                            name.toLowerCase().endsWith(".jpeg"));
        } else {
            System.out.println("Directory not found: " + album);
            return;
        }

        thumbnailGrid = createThumbnailGrid();
        fullView = createFullImageView();
        root.getChildren().setAll(thumbnailGrid);
    }

    private VBox createThumbnailGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        int col = 0, row = 0;
        if (imageFiles != null) {
            for (File file : imageFiles) {
                ImageView thumbnail = new ImageView();
                thumbnail.setFitWidth(150);
                thumbnail.setPreserveRatio(true);
                thumbnail.getStyleClass().add("image-view");
                thumbnail.setOnMouseClicked(e -> showFullImage(file));
                grid.add(thumbnail, col++, row);
                if (col == 4) {
                    col = 0;
                    row++;
                }

                // Load image in background
                loadImageAsync(file, thumbnail);
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        Button backButton = new Button("Back to Albums");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> root.getChildren().setAll(albumSelectionScreen));

        VBox vbox = new VBox(10, new Label(currentAlbum), scrollPane, backButton);
        vbox.setAlignment(Pos.CENTER);
        return vbox;
    }

    private void loadImageAsync(File file, ImageView imageView) {
        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                String filePath = file.toURI().toString();
                if (imageCache.containsKey(filePath)) {
                    return imageCache.get(filePath);
                } else {
                    Image image = new Image(filePath, 150, 0, true, true);
                    imageCache.put(filePath, image);
                    return image;
                }
            }
        };

        loadImageTask.setOnSucceeded(event -> imageView.setImage(loadImageTask.getValue()));
        new Thread(loadImageTask).start();
    }

    private BorderPane createFullImageView() {
        BorderPane fullView = new BorderPane();

        fullImageView = new ImageView();
        fullImageView.setPreserveRatio(true);
        fullImageView.fitWidthProperty().bind(fullView.widthProperty().multiply(0.8));

        Button prevButton = new Button("Previous");
        Button backButton = new Button("Back to Thumbnails");
        Button nextButton = new Button("Next");

        prevButton.getStyleClass().add("button");
        backButton.getStyleClass().add("button");
        nextButton.getStyleClass().add("button");

        prevButton.setOnAction(e -> showPreviousImage());
        backButton.setOnAction(e -> root.getChildren().setAll(thumbnailGrid));
        nextButton.setOnAction(e -> showNextImage());

        HBox controls = new HBox(10, prevButton, backButton, nextButton);
        controls.setAlignment(Pos.CENTER);

        fullView.setCenter(fullImageView);
        fullView.setBottom(controls);
        return fullView;
    }

    private void showFullImage(File file) {
        fullImageView.setImage(new Image(file.toURI().toString()));
        for (int i = 0; i < imageFiles.length; i++) {
            if (imageFiles[i].equals(file)) {
                currentImageIndex = i;
                break;
            }
        }
        root.getChildren().setAll(fullView);
    }

    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            fullImageView.setImage(new Image(imageFiles[currentImageIndex].toURI().toString()));
        }
    }

    private void showNextImage() {
        if (currentImageIndex < imageFiles.length - 1) {
            currentImageIndex++;
            fullImageView.setImage(new Image(imageFiles[currentImageIndex].toURI().toString()));
        }
    }
}