package fr.umontpellier.controller;

import fr.umontpellier.model.Backup;
import fr.umontpellier.service.AlertNotificationService;
import fr.umontpellier.service.UserNotificationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static fr.umontpellier.util.SystemTrayUtil.addToSystemTray;

public class FileTransferController {

    @FXML
    Button exportBrowseButton, exportButton, importButton, importBrowseButton, autoExportBrowseButton;
    @FXML
    TextField folderPathField, extensionField, destinationPathField, autoBackupDirectoryField, autoBackupIntervalField;
    @FXML
    CheckBox runInBackgroundCheckbox;
    @FXML
    ToggleButton autoBackupToggleButton;
    @FXML
    private ListView<String> backupListView;
    @FXML
    private ListView<String> filesListView;

    private final UserNotificationService notificationService;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Stage stage;
    private Socket socket;
    private ScheduledExecutorService scheduler;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean addedToTray = false;

    public FileTransferController() {
        this.notificationService = new AlertNotificationService();
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        updateCloseBehavior(runInBackgroundCheckbox.isSelected());

        runInBackgroundCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateCloseBehavior(newValue));
    }

    private void updateCloseBehavior(boolean runInBackground) {
        stage.setOnCloseRequest(event -> {
            if (runInBackground) {
                stage.hide();
                event.consume();
                if (!addedToTray) {
                    Platform.runLater(() -> addToSystemTray(stage));
                    addedToTray = true;
                }
            } else {
                shutdownApplication();
            }
        });
    }

    private void shutdownApplication() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.exit();
        System.exit(0);
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
    private void handleRunInBackgroundAction() {
        boolean runInBackground = runInBackgroundCheckbox.isSelected();
        updateCloseBehavior(runInBackground);
    }

    @FXML
    public void onExport() {
        boolean success = executeBackup(folderPathField.getText());
        if (success) {
            notificationService.showSuccessPopup("Backup Successful", "The files have been backed up successfully.");
        } else {
            notificationService.showErrorPopup("Backup Error", "An error occurred during the backup.");
        }
        onRefreshBackups();
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
                notificationService.showErrorPopup("Connection Error", "Error while communicating with the server.");
            }
        } else {
            notificationService.showErrorPopup("Selection Required", "Please select a backup to view its files.");
        }
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
                notificationService.showErrorPopup("Connection Error", "Error while communicating with the server.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            notificationService.showErrorPopup("Connection Error", "Error while communicating with the server.");
        }
    }

    @FXML
    public void onToggleAutoBackup() {
        if (autoBackupToggleButton.isSelected()) {
            boolean started = startAutomaticBackup();
            if (!started) {
                autoBackupToggleButton.setSelected(false);
            } else {
                autoBackupToggleButton.setText("Disable Automatic Backup");
            }
        } else {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
            autoBackupToggleButton.setText("Enable Automatic Backup");
        }
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

    private boolean executeBackup(String folderPath) {
        try {
            out.writeObject("CREATE_BACKUP");
            List<String> extensions = Arrays.asList(extensionField.getText().split(" "));
            Backup backup = new Backup(folderPath, extensions);
            out.writeObject(backup);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void executeFullRestore(String restoreDirectory) throws IOException {
        List<String> selectedBackups = new ArrayList<>(backupListView.getSelectionModel().getSelectedItems());
        if(selectedBackups.isEmpty() || restoreDirectory == null){
            notificationService.showErrorPopup("Selection Required", "Please select a backup to restore and a destination folder.");
            return;
        }

        String selectedBackup = selectedBackups.get(0);
        sendRequest("RESTORE_BACKUP", selectedBackup);
        receiveAndRestoreFiles(restoreDirectory);
        notificationService.showSuccessPopup("Restore Successful", "The files have been successfully restored.");
    }

    private void executePartialRestore() throws IOException {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        List<String> selectedFiles = new ArrayList<>(filesListView.getSelectionModel().getSelectedItems());

        if (!selectedFiles.isEmpty() && destinationPathField.getText() != null && selectedBackup != null) {
            sendRequest("RESTORE_FILE", selectedBackup, selectedFiles);
            receiveAndRestoreFiles(destinationPathField.getText());
            notificationService.showSuccessPopup("Restore Successful", "The files have been successfully restored.");
        } else {
            notificationService.showErrorPopup("Selection Required", "Please select a backup, files to restore, and a destination folder.");
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
            notificationService.showErrorPopup("Selection Required", "Please select a backup to delete.");
        }
    }

    private void sendRequest(String requestType, Object... params) {
        executor.submit(() -> {
            try {
                out.writeObject(requestType);
                for (Object param : params) {
                    out.writeObject(param);
                }
                out.flush();
            } catch (IOException e) {
                Platform.runLater(() -> notificationService.showErrorPopup("Connection Error", "Error while communicating with the server."));
                e.printStackTrace();
            }
        });
    }

    private void handleResponse(String successMessage, String errorMessage) {
        try {
            String response = (String) in.readObject();
            if ("SUCCESS".equals(response)) {
                notificationService.showSuccessPopup(successMessage, errorMessage);
            } else {
                notificationService.showErrorPopup("Error", errorMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            notificationService.showErrorPopup("Error", errorMessage);
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
            notificationService.showErrorPopup("Restore Error", "Error while receiving data from the server.");
            e.printStackTrace();
        }
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

    public void onAutoExportSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        File selectedDirectory = directoryChooser.showDialog(autoExportBrowseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            autoBackupDirectoryField.setText(selectedDirectory.getAbsolutePath());
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

    private boolean startAutomaticBackup() {
        if (autoBackupDirectoryField.getText().isEmpty()) {
            notificationService.showErrorPopup("Backup Folder Error", "Please specify a folder to backup.");
            return false;
        }

        int intervalMinutes;
        try {
            intervalMinutes = Integer.parseInt(autoBackupIntervalField.getText());
            if (intervalMinutes <= 0) {
                throw new NumberFormatException("Interval must be positive.");
            }
        } catch (NumberFormatException e) {
            notificationService.showErrorPopup("Backup Interval Error", "The backup interval is not a valid number.");
            return false;
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable backupTask = () -> {
            try {
                executeBackup(autoBackupDirectoryField.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(backupTask, 0, intervalMinutes, TimeUnit.MINUTES);
        return true;
    }

}