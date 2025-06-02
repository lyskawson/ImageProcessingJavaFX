# Image Processing with JavaFX

A simple JavaFX application for performing basic image processing operations on JPG images. Users can load an image, apply various transformations and filters, and save the processed result.

<p align="center">
  <img src="https://github.com/user-attachments/assets/1552b27c-cf47-48fc-862d-f0d7a9cadaa9" width=75% />
</p>


## Features

*   **Load JPG Images:** Load images exclusively in the `.jpg` format.
*   **Side-by-Side View:** Displays the original image and the processed image concurrently.
*   **Image Operations:**
    *   **Negative:** Inverts the colors of the image.
    *   **Thresholding:** Converts the image to black and white based on a user-defined threshold value.
    *   **Contour Detection:** Highlights edges in the image using a Sobel operator.
    *   **Scaling:** Resizes the image to user-specified dimensions, with an option to restore original dimensions.
    *   **Rotation:** Rotates the image 90 degrees to the left or right.
*   **Save Processed Image:** Save the modified image (or the original if no operations were performed) as a new `.jpg` file to the user's Desktop.
*   **User Interface:**
    *   Intuitive controls for loading, selecting operations, executing, and saving.
    *   Dialogs for user input (e.g., threshold value, scaling dimensions, save filename).
    *   Informative alerts for success, errors, or warnings.
*   **File Handling:**
    *   Validates file format on load (only `.jpg`).
    *   Prompts for a filename (3-100 characters) upon saving.
    *   Checks if a file with the same name already exists on the Desktop before saving.

## Implemented Operations

The application supports the following image processing operations:

### 1. Negative
Converts the image to its photographic negative. Each pixel's color component `c` is transformed to `1.0 - c`.

### 2. Thresholding
Converts the image to a binary (black and white) image.
*   A dialog prompts the user to enter a threshold value (0-255).
*   Pixels with an average grayscale intensity below the threshold become black; others become white.

### 3. Contour Detection
Detects and highlights edges in the image using the Sobel operator.
*   The image is first converted to grayscale internally.
*   The Sobel operator is applied to detect gradients, and pixels exceeding a certain gradient magnitude are marked as edges (black on a white background).

### 4. Scale Image
Allows resizing of the image.
*   A dialog prompts for the new width and height (values between 1 and 3000 pixels).
*   An option to "Restore Original Dimensions" is available.
*   The image is scaled to the new dimensions (aspect ratio is not preserved by default in the current scaling implementation).

### 5. Rotate Image
Rotates the image by a fixed angle.
*   **Rotate Left (↺):** Rotates the image 90 degrees counter-clockwise.
*   **Rotate Right (↻):** Rotates the image 90 degrees clockwise.

## How to Use

1.  **Launch the Application.**
2.  **Load an Image:**
    *   Click the "Load Image (.jpg)" button.
    *   A file chooser dialog will appear. Select a `.jpg` image file.
    *   The selected image will be displayed in the "Original" view.
3.  **Apply Operations:**
    *   **From the ComboBox:**
        *   Select an operation (e.g., "Negative", "Thresholding", "Contour") from the "Operations from the list" dropdown.
        *   Click the "Execute" button.
        *   For operations requiring input (like "Thresholding"), a dialog will appear.
    *   **Direct Operations:**
        *   Click "Scale Image", "Rotate left (↺)", or "Rotate right (↻)" for immediate actions.
        *   "Scale Image" will open a dialog for new dimensions.
    *   The result of the operation will appear in the "After operations" view. Operations can be chained (i.e., applied to the already processed image).
4.  **Save Image:**
    *   Click the "Save Image" button.
    *   A dialog will prompt you to enter a filename (3-100 characters, without the `.jpg` extension).
    *   The image will be saved as a `.jpg` file to your Desktop.
    *   If no operations were performed, a warning will be shown, and the original image will be saved.

## File Handling Details

*   **Loading:** Only `.jpg` files are accepted. An error message is shown for other formats.
*   **Saving:**
    *   Images are saved in `.jpg` format.
    *   The default save location is the user's Desktop directory.
    *   The user is prompted for a filename (3-100 characters, excluding the extension).
    *   The application checks if a file with the chosen name already exists on the Desktop and will show an error if it does to prevent accidental overwrites.

## Prerequisites

*   Java Development Kit (JDK) 11 or later (preferably one that includes JavaFX, like Liberica JDK Full, or with JavaFX SDK set up separately).
*   JavaFX SDK (if not included with your JDK). The application uses `javafx.controls`, `javafx.graphics`, and `javafx.swing` modules.

## Running the Application

1.  **Compile the Code:**
    If your JDK does not include JavaFX, you need to specify the module path and add the required modules.
    ```bash
    javac --module-path /path/to/your/javafx-sdk/lib --add-modules javafx.controls,javafx.graphics,javafx.swing HelloApplication.java
    ```
    Replace `/path/to/your/javafx-sdk/lib` with the actual path to your JavaFX SDK library.

2.  **Run the Application:**
    ```bash
    java --module-path /path/to/your/javafx-sdk/lib --add-modules javafx.controls,javafx.graphics,javafx.swing org.example.imagesprocessingjavafx.HelloApplication
    ```
    Again, replace `/path/to/your/javafx-sdk/lib` accordingly. If using an IDE like IntelliJ IDEA, Eclipse, or NetBeans, these settings can usually be configured within the project or run configuration.

## Author

*   Aleksander Lyskawa

## Note on Logo

The application attempts to load a logo named `PWrLogo.png` from the resources. If this file is not found, an error message will be printed to the console, but the application will otherwise function normally.