package fr.umontpellier.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class SaveManagerController {

    @FXML
    Button browseButton;

    @FXML
    TextField folderPath;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void onSave() {
        // TODO
    }
    
    public void onSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionnez un Dossier");
        File selectedDirectory = directoryChooser.showDialog(browseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            folderPath.setText(selectedDirectory.getAbsolutePath());
        }
    }
    
}
