package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Deps;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class FileUploadController {
    private Deps deps;
    private Stage stage;
    @FXML
    private Button uploadButton;
    public FileUploadController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }
    @FXML
    private void initialize() {
        uploadButton.setOnAction(event -> openFileChooser());
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Document");
        fileChooser.getExtensionFilters().addAll(

                new FileChooser.ExtensionFilter("csv", "*.csv")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            // Handle the file (e.g., upload it or read its contents)
            System.out.println("File selected: " + file.getAbsolutePath());
        }
    }

    public void showStage(Pane root){
        Scene scene = new Scene(root,600,600);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Screen Configuration");
        stage.show();
    }
}
