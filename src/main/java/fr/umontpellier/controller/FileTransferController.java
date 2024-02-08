package fr.umontpellier.controller;

import fr.umontpellier.model.Backup;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileTransferController {

    @FXML
    Button exportBrowseButton, exportButton, importButton, importBrowseButton;
    @FXML
    TextField folderPathField, extensionField, destinationPathField;
    @FXML
    private ListView<String> backupListView;
    @FXML
    private ListView<String> filesListView;
    @FXML
    private VBox fileContainer;

    private Stage stage;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        filesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        backupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        backupListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onViewFiles();
            }
        });
    }

    @FXML
    public void onExport() {
        executeBackup();
        onRefreshBackups();
    }

    @FXML
    public void onImport() throws IOException{
        executeFullRestore(destinationPathField.getText());
    }

    @FXML
    private void onDeleteFiles() {
        executeFileDeletion();
    }

    @FXML
    private void onPartialRestoreFiles() throws IOException {
        executePartialRestore();
    }

    @FXML
    private void onDeleteBackup() {
        executeBackupDeletion();
    }

    private void executeBackup() {
        try {
            out.writeObject("CREATE_BACKUP");
            List<String> extensions = Arrays.asList(extensionField.getText().split(" "));
            Backup backup = new Backup(folderPathField.getText(), extensions);
            out.writeObject(backup);
            showBackupSuccessPopup("Backup Successful", "The files have been backed up successfully.");
        } catch (IOException e) {
            showErrorPopup("Backup Error", "An error occurred during the backup.");
            e.printStackTrace();
        }
    }

    private void executeFullRestore(String restoreDirectory) throws IOException {
        List<String> selectedBackups = new ArrayList<>(backupListView.getSelectionModel().getSelectedItems());
        if(selectedBackups.isEmpty() || restoreDirectory == null){
            showErrorPopup("Selection Required", "Please select a backup to restore and a destination folder.");
            return;
        }

        String selectedBackup = selectedBackups.get(0);
        sendRequest("RESTORE_BACKUP", selectedBackup);
        receiveAndRestoreFiles(restoreDirectory);
        showBackupSuccessPopup("Restore Successful", "The files have been successfully restored.");
    }

    /**
     * Requete de restauration partielle
     * @throws IOException
     */
    private void executePartialRestore() throws IOException {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        List<String> selectedFiles = new ArrayList<>(filesListView.getSelectionModel().getSelectedItems());

        if (!selectedFiles.isEmpty() && destinationPathField.getText() != null && selectedBackup != null) {
            sendRequest("RESTORE_FILE", selectedBackup, selectedFiles);
            receiveAndRestoreFiles(destinationPathField.getText());
            showBackupSuccessPopup("Restore Successful", "The files have been successfully restored.");
        } else {
            showErrorPopup("Selection Required", "Please select a backup, files to restore, and a destination folder.");
        }
    }

    private void executeFileDeletion() {
        List<String> selectedFiles = filesListView.getSelectionModel().getSelectedItems();
        if (!selectedFiles.isEmpty()) {
            String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
            List<String> fullPaths = selectedFiles.stream()
                    .map(fileName -> selectedBackup + "/" + fileName)
                    .collect(Collectors.toList());
            sendRequest("DELETE_FILE", fullPaths);
            handleResponse("Delete Successful", "The files have been deleted successfully.");
        }
        onViewFiles();
    }

    private void executeBackupDeletion() {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        if (selectedBackup != null) {
            sendRequest("DELETE_BACKUP", selectedBackup);
            handleResponse("Delete Successful", "The backup has been deleted successfully.");
            onRefreshBackups();
            filesListView.getItems().clear();
        } else {
            showErrorPopup("Selection Required", "Please select a backup to delete.");
        }
    }

    private void sendRequest(String requestType, Object... params) {
        try {
            out.writeObject(requestType);
            for (Object param : params) {
                out.writeObject(param);
            }
            out.flush();
        } catch (IOException e) {
            showErrorPopup("Connection Error", "Error while communicating with the server.");
            e.printStackTrace();
        }
    }

    private void handleResponse(String successMessage, String errorMessage) {
        try {
            String response = (String) in.readObject();
            if ("SUCCESS".equals(response)) {
                showBackupSuccessPopup(successMessage, errorMessage);
            } else {
                showErrorPopup("Error", errorMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            showErrorPopup("Error", errorMessage);
            e.printStackTrace();
        }
    }

    private void receiveAndRestoreFiles(String restoreDirectory) throws IOException {
        try {
            while (true) {
                Object response = in.readObject();
                if (response instanceof String) {
                    String relativeFilePath = (String) response;
                    if ("RESTORE_COMPLETE".equals(relativeFilePath)) {
                        break;
                    }

                    File file = new File(restoreDirectory, relativeFilePath);
                    file.getParentFile().mkdirs();

                    try (FileOutputStream fileOut = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fileOut.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            showErrorPopup("Restore Error", "Error while receiving data from the server.");
            e.printStackTrace();
        }
    }

    private void showErrorPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showBackupSuccessPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onImportSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        File selectedDirectory = directoryChooser.showDialog(importBrowseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            destinationPathField.setText(selectedDirectory.getAbsolutePath());
        }

    }

    public void onExportSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        File selectedDirectory = directoryChooser.showDialog(exportBrowseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            folderPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    @FXML
    private void onRefreshBackups() {
        try {
            if (out != null) {
                out.writeObject("READ_BACKUP");
                out.flush();
                
                Object response = in.readObject();
                if (response instanceof List) {
                    List<String> backups = (List<String>) response;
                    backupListView.getItems().setAll(backups);
                }
            } else {
                showErrorPopup("Connection Error", "Error while communicating with the server.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showErrorPopup("Connection Error", "Error while communicating with the server.");
        }
    }

    @FXML
    private void onViewFiles() {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        if (selectedBackup != null) {
            try {
                out.writeObject("READ_FILE");
                out.writeObject(selectedBackup);
                out.flush();

                Object response = in.readObject();
                if (response instanceof List) {
                    List<String> files = (List<String>) response;
                    filesListView.getItems().setAll(files);
                }
                else{

                    filesListView.getItems().clear();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showErrorPopup("Connection Error", "Error while communicating with the server.");
            }
        } else {
            showErrorPopup("Selection Required", "Please select a backup to view its files.");
        }
    }

}