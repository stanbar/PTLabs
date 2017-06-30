package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    @FXML
    Button btnPickFile;

    @FXML
    Button btnSendFiles;

    @FXML
    VBox vBoxRoot;

    @FXML
    TableView<SendFileTask> tableView = new TableView<>();

    private List<File> selectedFilesList;

    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private List<SendFileTask> taskList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<SendFileTask, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        fileNameColumn.setPrefWidth(85);

        TableColumn<SendFileTask, String> fileStatusColumn = new TableColumn<>("Status");
        fileStatusColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        fileStatusColumn.setPrefWidth(85);

        TableColumn<SendFileTask, Double> fileProgressColumn = new TableColumn<>("Progress");
        fileProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        fileProgressColumn.setPrefWidth(150);
        fileProgressColumn
                .setCellFactory(ProgressBarTableCell.forTableColumn());

        tableView.getColumns().addAll(fileNameColumn, fileStatusColumn, fileProgressColumn);


        btnPickFile.setOnAction(this::promptLoadFiles);
        btnSendFiles.setOnAction(this::sendFiles);

    }

    public void promptLoadFiles(ActionEvent event) {
        Window window = btnPickFile.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        selectedFilesList = fileChooser.showOpenMultipleDialog(window);
        if (selectedFilesList != null) {
            taskList = new ArrayList<>();
            selectedFilesList.parallelStream()
                    .map(file -> new SendFileTask(this, file))
                    .collect(Collectors.toList())
                    .forEach(file -> {
                        tableView.getItems().add(file);
                        taskList.add(file);
                    });
        }

    }

    private void sendFiles(ActionEvent event) {

        taskList.parallelStream()
                .forEach((task) -> task.setStatus("In Queue..."));
        taskList.parallelStream()
                .forEach((task) -> executorService.submit(task));

    }


}
