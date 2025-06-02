package org.example.imagesprocessingjavafx;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.UnaryOperator;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class HelloApplication extends Application {

    private static final String APP_NAME = "Image Processing";
    private static final String AUTHOR_INFO = "Author: Aleksander Lyskawa, 275462";

    private Stage primaryStage;
    private ComboBox<String> operationComboBox;
    private Button executeButton;
    private Button saveImageButton;
    private Button scaleButton;
    private Button rotateLeftButton;
    private Button rotateRightButton;

    private ImageView originalImageView;
    private ImageView processedImageView;

    private Image currentOriginalImage;
    private Image currentProcessedImage;
    private boolean operationsPerformed = false;

    private final String[] availableOperations = {
            "Negative",
            "Thresholding",
            "Contour"
    };

    private double originalImageWidth = 0;
    private double originalImageHeight = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(APP_NAME);

        HBox topPane = createTopPane();
        VBox centerPane = createCenterPane();

        Label footerLabel = new Label(AUTHOR_INFO);
        StackPane bottomPane = new StackPane(footerLabel);
        bottomPane.setAlignment(Pos.CENTER_RIGHT);


        BorderPane rootLayout = new BorderPane();
        rootLayout.setTop(topPane);
        rootLayout.setCenter(centerPane);
        rootLayout.setBottom(bottomPane);

        Scene scene = new Scene(rootLayout, 850, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        updateButtonStates();
    }

    private HBox createTopPane() {
        ImageView logoView = new ImageView();
        try (InputStream logoStream = getClass().getResourceAsStream("/PWrLogo.png")) {
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                logoView.setImage(logo);
                logoView.setFitHeight(40);
                logoView.setPreserveRatio(true);
            } else {
                System.err.println("Logo file not found in resources.");
            }
        } catch (Exception e) {
            System.err.println("Error loading logo");
        }

        Label appTitleLabel = new Label(APP_NAME);
        appTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        return new HBox(logoView);
    }

    private VBox createCenterPane() {
        Label welcomeLabel = new Label("Welcome to Image Processing!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        operationComboBox = new ComboBox<>();
        operationComboBox.setPromptText("Choose operation...");
        operationComboBox.getItems().add(null);
        operationComboBox.getItems().addAll(availableOperations);
        operationComboBox.setValue(null);
        operationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());

        executeButton = new Button("Execute");
        executeButton.setOnAction(e -> handleExecuteOperation());

        HBox operationSelectionControls = new HBox(10, new Label("Operations from the list:"), operationComboBox, executeButton);
        operationSelectionControls.setAlignment(Pos.CENTER_LEFT);
        operationSelectionControls.setPadding(new Insets(0, 0, 10, 0));

        scaleButton = new Button("Scale Image");
        scaleButton.setOnAction(e -> handleScaleAction());

        rotateLeftButton = new Button("Rotate left (↺)");
        rotateLeftButton.setOnAction(e -> handleRotateAction(-90));

        rotateRightButton = new Button("Rotate right (↻)");
        rotateRightButton.setOnAction(e -> handleRotateAction(90));

        HBox directOperationControls = new HBox(10, scaleButton, rotateLeftButton, rotateRightButton);
        directOperationControls.setAlignment(Pos.CENTER_LEFT);
        directOperationControls.setPadding(new Insets(0, 0, 10, 0));

        Button loadImageButton = new Button("Load Image (.jpg)");
        loadImageButton.setOnAction(e -> handleLoadImage());

        saveImageButton = new Button("Save Image");
        saveImageButton.setOnAction(e -> handleSaveImage());

        HBox fileControls = new HBox(10, loadImageButton, saveImageButton);
        fileControls.setAlignment(Pos.CENTER_LEFT);

        originalImageView = new ImageView();
        originalImageView.setFitWidth(350);
        originalImageView.setFitHeight(350);
        originalImageView.setPreserveRatio(true);
        Label originalLabel = new Label("Original:");

        processedImageView = new ImageView();
        processedImageView.setFitWidth(350);
        processedImageView.setFitHeight(350);
        processedImageView.setPreserveRatio(true);
        Label processedLabel = new Label("After operations:");

        VBox originalImagePane = new VBox(5, originalLabel, originalImageView);
        originalImagePane.setAlignment(Pos.CENTER);
        VBox processedImagePane = new VBox(5, processedLabel, processedImageView);
        processedImagePane.setAlignment(Pos.CENTER);

        GridPane imageViewsPane = new GridPane();
        imageViewsPane.add(originalImagePane, 0, 0); // kolumna 0, wiersz 0
        imageViewsPane.add(processedImagePane, 1, 0); // kolumna 1, wiersz 0
        imageViewsPane.setHgap(20); // odstęp
        imageViewsPane.setAlignment(Pos.CENTER);
        imageViewsPane.setPadding(new Insets(10));
        ColumnConstraints col1 = new ColumnConstraints(); // ustawienia kolumn
        col1.setPercentWidth(50); // 50% szerokosci
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        imageViewsPane.getColumnConstraints().addAll(col1, col2);

        VBox centerPane = new VBox(10, welcomeLabel, operationSelectionControls, directOperationControls, fileControls, imageViewsPane);
        centerPane.setPadding(new Insets(15));
        VBox.setVgrow(imageViewsPane, Priority.ALWAYS);

        return centerPane;
    }

    private void showAlert(AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private void updateButtonStates() {
        boolean imageLoaded = (currentOriginalImage != null);
        executeButton.setDisable(!imageLoaded || operationComboBox.getValue() == null);
        saveImageButton.setDisable(!imageLoaded);
        operationComboBox.setDisable(!imageLoaded);
        scaleButton.setDisable(!imageLoaded);
        rotateLeftButton.setDisable(!imageLoaded);
        rotateRightButton.setDisable(!imageLoaded);
    }

    private void handleLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Images", "*.jpg"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            String fileName = selectedFile.getName().toLowerCase();
            if (!fileName.endsWith(".jpg")) {
                showAlert(AlertType.ERROR, "Wrong format", null, "Please select a .jpg file.");
                return;
            }
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                if (currentOriginalImage != null)
                    currentOriginalImage = null;
                if (currentProcessedImage != null)
                    currentProcessedImage = null;
                processedImageView.setImage(null);
                System.gc(); // garbage collection to free memory

                currentOriginalImage = new Image(fis);
                originalImageWidth = currentOriginalImage.getWidth();
                originalImageHeight = currentOriginalImage.getHeight();
                operationsPerformed = false;
                currentProcessedImage = null;

                originalImageView.setImage(currentOriginalImage);
                processedImageView.setImage(null);
                showAlert(AlertType.INFORMATION, "Success", null, "File loaded successfully: " + selectedFile.getName());
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Loading Error", null, "Could not load image: ");

            }
        }
        updateButtonStates();
    }

    private TextField createNumericTextField(int initialValue) {
        TextField textField = new TextField(String.valueOf(initialValue));
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([0-9]{1,4})?")) {
                if (!newText.isEmpty()) {
                    try {
                        int val = Integer.parseInt(newText);
                        if (val >= 1 && val <= 3000)
                            return change;
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    return change;
                }
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
        return textField;
    }


    private void handleScaleAction() {
        if (currentOriginalImage == null)
            return;

        Image sourceImage = (currentProcessedImage != null) ? currentProcessedImage : currentOriginalImage;

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Scaling Image");
        dialog.setHeaderText("Enter new dimensions (1-3000).");
        dialog.initOwner(primaryStage);

        ButtonType scaleButtonType = new ButtonType("Change dimensions", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scaleButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField widthField = createNumericTextField((int) sourceImage.getWidth());
        TextField heightField = createNumericTextField((int) sourceImage.getHeight());

        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthField, 1, 0);

        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightField, 1, 1);

        Button restoreOriginalButton = new Button("Restore Original Dimensions");
        restoreOriginalButton.setOnAction(event -> {
            widthField.setText(String.valueOf((int) originalImageWidth));
            heightField.setText(String.valueOf((int) originalImageHeight));
        });
        grid.add(restoreOriginalButton, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == scaleButtonType) {
                return new Pair<>(widthField.getText(), heightField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(dimensions -> {
            try {
                int newWidth = Integer.parseInt(dimensions.getKey());
                int newHeight = Integer.parseInt(dimensions.getValue());

                ImageView tempImageView = new ImageView(sourceImage);
                tempImageView.setFitWidth(newWidth);
                tempImageView.setFitHeight(newHeight);
                tempImageView.setPreserveRatio(false);

                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);

                currentProcessedImage = tempImageView.snapshot(params, null);
                processedImageView.setImage(currentProcessedImage);


                showAlert(AlertType.INFORMATION, "Scaling", null, "Image has been scaled.");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Scaling", "Error while scaling", e.getMessage());
            }
        });

        updateButtonStates();
        operationsPerformed = true;
    }


    private void handleRotateAction(double angle) {
        if (currentOriginalImage == null) return;

        Image sourceImage = (currentProcessedImage != null) ? currentProcessedImage : currentOriginalImage;

        double newWidth = sourceImage.getHeight();
        double newHeight = sourceImage.getWidth();

        Canvas canvas = new Canvas(newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (angle == 90) {
            gc.translate(newWidth, 0); // przesunięcie do nowego początku
            gc.rotate(90);
        } else if (angle == -90) {
            gc.translate(0, newHeight); // przesunięcie do nowego początku
            gc.rotate(-90);
        }


        gc.drawImage(sourceImage, 0, 0); // rysowanie obrazu na canvasie

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT); // ustawienie przezroczystości tła

        currentProcessedImage = canvas.snapshot(params, null); // tworzenie nowego obrazu z canvasu
        processedImageView.setImage(currentProcessedImage); // ustawienie przetworzonego obrazu
        showAlert(AlertType.INFORMATION, "Rotation", null, "Image rotated with  " + (int) angle + " degrees");

        updateButtonStates();
        operationsPerformed = true;
    }

    private void handleExecuteOperation() {
        String selectedOperation = operationComboBox.getValue();
        Image sourceImage = (currentProcessedImage != null) ? currentProcessedImage : currentOriginalImage;

        switch (selectedOperation) {
            case "Negative":
                performNegative(sourceImage);
                break;
            case "Thresholding":
                promptThresholding(sourceImage);
                break;
            case "Contour":
                performContouring(sourceImage);
                break;
            default:
                showAlert(AlertType.INFORMATION, "Note", null, "Operation '" + selectedOperation + "' is not implemented.");
                break;
        }


        updateButtonStates();
    }


    private void handleSaveImage() {
        if (currentOriginalImage == null) {
            showAlert(AlertType.ERROR, "Error", null, "No image to save");
            return;
        }
        Image imageToSave = (currentProcessedImage != null) ? currentProcessedImage : currentOriginalImage;

        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Save image as..");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fileNameField = new TextField();
        fileNameField.setPromptText("Filename");
        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        if (!operationsPerformed) {
            Label warningNoOpsLabel = new Label("No operations were executed!");
            warningNoOpsLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            content.getChildren().add(warningNoOpsLabel);
        }
        content.getChildren().addAll(new Label("Enter filename (3-100 chars, without .jpg):"), fileNameField, validationLabel);
        dialogPane.setContent(content);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Save");
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String fileNameText = fileNameField.getText().trim();
            if (fileNameText.length() < 3) {
                validationLabel.setText("Enter at least 3 characters");
                event.consume();
            } else if (fileNameText.length() > 100) {
                validationLabel.setText("Name too long (max 100)");
                event.consume();
            } else {
                validationLabel.setText("");
            }
        });
        dialog.setResultConverter(dialogButton -> (dialogButton == ButtonType.OK && !fileNameField.getText().isEmpty()) ? fileNameField.getText().trim() : null);

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(fileName -> {
            if (fileName.isEmpty()) {
                showAlert(AlertType.ERROR, "Wrong file name ", null, "Filename cant be empty!");
                return;
            }
            String finalFileNameWithExt = fileName + ".jpg";
            Path picturesDir = Paths.get(System.getProperty("user.home"), "Desktop");
            try {
                if (!Files.exists(picturesDir)) Files.createDirectories(picturesDir);
                File outputFile = new File(picturesDir.toFile(), finalFileNameWithExt);
                if (outputFile.exists()) {
                    showAlert(AlertType.ERROR, "Save error", null, "File " + finalFileNameWithExt + " already exists");
                    return;
                }

                BufferedImage bImage = SwingFXUtils.fromFXImage(imageToSave, null);
                BufferedImage rgbImage = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.drawImage(bImage, 0, 0, null);
                g2d.dispose();

                ImageIO.write(rgbImage, "jpg", outputFile);
                showAlert(AlertType.INFORMATION, "Sukces", null, "Image saved: " + outputFile.getAbsolutePath());

            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error saving file (IOException)", "Cannot save file: " + finalFileNameWithExt, e.getMessage());
            }
        });
    }


    //################ DODANE OPERACJE ##################

    private void performNegative(Image sourceImage) {

        WritableImage negativeImage = new WritableImage((int) sourceImage.getWidth(), (int) sourceImage.getHeight()); //  image with the same size as the source
        PixelReader pixelReader = sourceImage.getPixelReader(); // pixel reader
        PixelWriter pixelWriter = negativeImage.getPixelWriter(); // pixel writer

        for (int y = 0; y < sourceImage.getHeight(); y++) { // row
            for (int x = 0; x < sourceImage.getWidth(); x++) { // column
                Color color = pixelReader.getColor(x, y); // reads pixel color
                pixelWriter.setColor(x, y, new Color(
                        1.0 - color.getRed(), // inverts red
                        1.0 - color.getGreen(), // inverts green
                        1.0 - color.getBlue(), // inverts blue
                        color.getOpacity() // keeps the original opacity
                ));
            }
        }

        currentProcessedImage = negativeImage; // sets the processed image to the negative image
        processedImageView.setImage(currentProcessedImage); // displays the negative image in the image view
        showAlert(AlertType.INFORMATION, "Negatyw", null, "Negatyw został wygenerowany pomyślnie!"); // shows an information alert that the negative was generated
        operationsPerformed = true; // marks that an image operation has been performed
    }

    private void promptThresholding(Image sourceImage) {
        Dialog<Integer> dialog = new Dialog<>(); // creates a new dialog that will return an integer
        dialog.setTitle("Thresholding");
        dialog.setHeaderText("Enter threshold value (0-255).");
        dialog.initOwner(primaryStage); // sets the main application window as the dialog's owner

        ButtonType thresholdButtonType = new ButtonType("Execute", ButtonBar.ButtonData.OK_DONE); // defines an "Execute" button treated as the OK button
        dialog.getDialogPane().getButtonTypes().addAll(thresholdButtonType, ButtonType.CANCEL); // adds "Execute" and "Cancel" buttons to the dialog

        Spinner<Integer> thresholdSpinner = new Spinner<>(0, 255, 128); // creates a spinner to select a value between 0 and 255, default is 128
        thresholdSpinner.setEditable(true); // allows the user to manually enter a number in the spinner

        // adds a listener to validate the text input of the spinner
        thresholdSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // allows only digits
                thresholdSpinner.getEditor().setText(newValue.replaceAll("\\D", "")); // removes non digit chars
            }
            try {
                int val = Integer.parseInt(thresholdSpinner.getEditor().getText()); // tries to parse the input as integer
                if (val < 0) thresholdSpinner.getValueFactory().setValue(0); // if value is below 0, set to 0
                if (val > 255) thresholdSpinner.getValueFactory().setValue(255); // if value is above 255, set to 255
            } catch (NumberFormatException e) { // if input is not a valid number
                if (!thresholdSpinner.getEditor().getText().isEmpty()) // if input is not empty
                    thresholdSpinner.getValueFactory().setValue(0); // set to default value 0
            }
        });

        VBox content = new VBox(10, new Label("Threshold:"), thresholdSpinner); // creates a vertical layout
        content.setPadding(new Insets(20)); // sets padding around the content
        dialog.getDialogPane().setContent(content); // sets the content of the dialog

        dialog.setResultConverter(dialogButton -> // defines what result the dialog should return
                (dialogButton == thresholdButtonType) ? thresholdSpinner.getValue() : null // return spinner value if "Execute" was pressed, otherwise null
        );

        Optional<Integer> result = dialog.showAndWait(); // shows the dialog and waits for user interaction
        result.ifPresent(threshold -> performThresholding(sourceImage, threshold)); // if a value was provided call the thresholding method with it
    }


    private void performThresholding(Image sourceImage, int threshold) {

        WritableImage thresholdedImage = new WritableImage((int) sourceImage.getWidth(), (int) sourceImage.getHeight()); // creates image with the same dimensions as the source image
        PixelReader pixelReader = sourceImage.getPixelReader(); // pixel reader
        PixelWriter pixelWriter = thresholdedImage.getPixelWriter(); // pixel writer

        for (int y = 0; y < sourceImage.getHeight(); y++) { // image rows
            for (int x = 0; x < sourceImage.getWidth(); x++) { // image columns
                Color color = pixelReader.getColor(x, y); // reads current pixel color
                double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0; // average intensity of the pixel
                pixelWriter.setColor(x, y, (gray * 255 < threshold) ? Color.BLACK : Color.WHITE); // writes black or white depending on whether the gray value is below the threshold
            }
        }

        processedImageView.setImage(thresholdedImage); // displays the processed image in the image view

        showAlert(AlertType.INFORMATION, "Thresholding", null, "Threshold executed successfully!"); // shows an information alert that the thresholding was completed

        operationsPerformed = true;
    }


    private void performContouring(Image sourceImage) {

        WritableImage contouredImage = new WritableImage((int) sourceImage.getWidth(), (int) sourceImage.getHeight()); // image with the same size as the source
        PixelReader pixelReader = sourceImage.getPixelReader(); // pixel reader
        PixelWriter pixelWriter = contouredImage.getPixelWriter(); // pixel writer
        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}}; // sobel X
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}; // sobel Y kernel

        int width = (int) sourceImage.getWidth(); // image width
        int height = (int) sourceImage.getHeight(); // image height

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, Color.WHITE); // sets each pixel to white
            }
        }

        for (int y = 1; y < height - 1; y++) { // rows skipping the border pixels
            for (int x = 1; x < width - 1; x++) { // columns skipping the borders
                double gx = 0, gy = 0; // gradient values for x and y directions

                for (int i = -1; i <= 1; i++) { // kernel rows
                    for (int j = -1; j <= 1; j++) { // kernel columns
                        Color color = pixelReader.getColor(x + j, y + i); // reads the color of the neighboring pixel
                        double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0; // converts to grayscale
                        gx += gray * sobelX[i + 1][j + 1]; // applies the horizontal kernel
                        gy += gray * sobelY[i + 1][j + 1]; // applies the vertical kernel
                    }
                }

                // calculates the magnitude of the gradient
                if ( Math.sqrt(gx * gx + gy * gy) > 0.25) { // if magnitude exceeds threshold set Black as  edge
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
        }

        processedImageView.setImage(contouredImage);
        showAlert(AlertType.INFORMATION, "Contouring", null, "Contouring executed successfully!");
        operationsPerformed = true;
    }


    public static void main(String[] args) {
        launch(args);
    }
}





