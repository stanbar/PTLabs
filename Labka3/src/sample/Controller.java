package sample;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Controller implements Initializable {

    @FXML
    Button btnPickFile;

    @FXML
    Button btnPickOutput;

    @FXML
    VBox vBoxRoot;

    @FXML
    TableView<ImageProcessingJob> tableView;

    @FXML
    Slider poolSlider;


    TableColumn<ImageProcessingJob, String> imageNameColumn;

    TableColumn<ImageProcessingJob, Double> progressColumn;

    TableColumn<ImageProcessingJob, String> statusColumn;


    private Set<ImageProcessingJob> jobs = new HashSet<>();


    private Path outputDirPath = Paths.get(System.getProperty("user.home"), "outputLab3");

    private int poolSize = 4;
    private Thread thread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageNameColumn = new TableColumn<>("Name");
        imageNameColumn.setCellValueFactory( //nazwa pliku
                p -> new SimpleStringProperty(p.getValue().getFile().getName()));

        statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory( //status przetwarzania
                p -> p.getValue().getStatusProperty());

        progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellFactory( //wykorzystanie paska postępu
                ProgressBarTableCell.forTableColumn());
        progressColumn.setCellValueFactory( //postęp przetwarzania
                p -> p.getValue().getProgressProperty().asObject());

        tableView.getColumns().addAll(imageNameColumn, statusColumn, progressColumn);
        poolSlider.setMin(1);
        poolSlider.setValue(4);
        poolSlider.setMax(4);

        poolSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                poolSize = newValue.intValue();
            }
        });
    }

    @FXML
    public void promptLoadFiles(ActionEvent event) {
        Window window = btnPickFile.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        List<File> selectedFilesList = fileChooser.showOpenMultipleDialog(window);
        if (selectedFilesList != null) {
            selectedFilesList.forEach(f -> jobs.add(new ImageProcessingJob(f)));
            jobs.forEach((task) -> task.setStatus("In Queue..."));
            tableView.getItems().addAll(jobs);

        }

    }

    @FXML
    public void promptOutputDir(ActionEvent event) {
        Window window = btnPickOutput.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File outputDir = directoryChooser.showDialog(window);


        if (outputDir != null) {
            outputDirPath = Paths.get(outputDir.getPath());

        }

    }

    @FXML
    public void processBackgroundSequential(ActionEvent event) {
        jobs.forEach((job) -> job.setProgress(0.0));
        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            jobs.forEach(this::processImage);

            long finishTime = System.currentTimeMillis();
            showProcessTime("Sequential", finishTime - startTime);
        }).start();
    }

    @FXML
    public void processBackgroundCommonPool(ActionEvent event) {
        jobs.forEach((job) -> job.setProgress(0.0));
        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            backgroundJob();

            long finishTime = System.currentTimeMillis();
            showProcessTime("with commonPool", finishTime - startTime);
        }).start();
    }


    @FXML
    public void processBackgroundFixedPool(ActionEvent event) {
        jobs.forEach((job) -> job.setProgress(0.0));

        ForkJoinPool pool = new ForkJoinPool(poolSize);
        pool.submit(this::backgroundJob);

    }

    public void backgroundJob() {
        long startTime = System.currentTimeMillis();
        jobs.parallelStream().forEach(this::processImage);
        long finishTime = System.currentTimeMillis();
        showProcessTime("with " + poolSize + " threads", finishTime - startTime);

    }

    private void showProcessTime(String method, long time) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, String.format("Processing %s took %d", method, time));
            alert.showAndWait();
        });


    }

    public void processImage(ImageProcessingJob job) {
        System.out.println("Processing Image with file: " + job.getFile().getName());
        Platform.runLater(() -> job.setStatus("Processing..."));
        convertToGrayscale(job.getFile(), outputDirPath, job.getProgressProperty());
        Platform.runLater(() -> job.setStatus("Done"));

    }

    public void convertToGrayscale(File originalFile, Path outputDirPath, DoubleProperty progressProp) {
        try {

            //wczytanie oryginalnego pliku do pamięci
            BufferedImage original = ImageIO.read(originalFile);

            //przygotowanie bufora na grafikę w skali szarości
            BufferedImage grayscale = new BufferedImage(
                    original.getWidth(), original.getHeight(), original.getType());
            //przetwarzanie piksel po pikselu
            for (int i = 0; i < original.getWidth(); i++) {
                for (int j = 0; j < original.getHeight(); j++) {
                    //pobranie składowych RGB
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    //obliczenie jasności piksela dla obrazu w skali szarości
                    int luminosity = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                    //przygotowanie wartości koloru w oparciu o obliczoną jaskość
                    int newPixel =
                            new Color(luminosity, luminosity, luminosity).getRGB();
                    //zapisanie nowego piksela w buforze
                    grayscale.setRGB(i, j, newPixel);
                }
                //obliczenie postępu przetwarzania jako liczby z przedziału [0, 1]
                double progress = (1.0 + i) / original.getWidth();
                //aktualizacja własności zbindowanej z paskiem postępu w tabeli
                Platform.runLater(() -> progressProp.set(progress));
            }
            //przygotowanie ścieżki wskazującej na plik wynikowy

            Path outputPath =
                    Paths.get(outputDirPath.toString(), originalFile.getName());
            Files.createDirectories(outputPath.getParent());
            Files.deleteIfExists(outputPath);
            Files.createFile(outputPath);


            //zapisanie zawartości bufora do pliku na dysku
            ImageIO.write(grayscale, "jpg", outputPath.toFile());
        } catch (IOException ex) {
            //translacja wyjątku
            throw new RuntimeException(ex);
        }
    }


    public void stop() {
        System.out.println("stop() w controllerze");
    }
}
