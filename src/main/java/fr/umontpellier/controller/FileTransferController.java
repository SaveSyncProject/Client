package fr.umontpellier.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileTransferController {

    @FXML
    Button browseButton;

    @FXML
    TextField folderPath;

    private Stage stage;
    private Socket socket;

    private ObjectOutputStream out;
    private BufferedReader in;

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

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
