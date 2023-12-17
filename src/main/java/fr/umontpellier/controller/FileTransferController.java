package fr.umontpellier.controller;

import fr.umontpellier.model.Backup;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class FileTransferController {

    @FXML
    Button browseButton;

    @FXML
    TextField folderPathField, extensionField;

    private Stage stage;
    private Socket socket;

    private ObjectOutputStream out;
    private BufferedReader in;

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void onSave() {
        try {
            List<String> extensions = Arrays.asList(this.extensionField.getText().split(" "));
            Backup backup = new Backup(folderPathField.getText(), extensions);
            System.out.println(backup.getDirectoryPath());
            System.out.println(backup.getFileExtensions());
            out.writeObject(backup);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void onSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionnez un Dossier");
        File selectedDirectory = directoryChooser.showDialog(browseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            folderPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public void showBackupSuccessPopup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sauvegarde Réussie");
        alert.setHeaderText(null);
        alert.setContentText("La sauvegarde des fichiers a été effectuée avec succès.");
        alert.showAndWait();
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }
}
