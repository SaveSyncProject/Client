package fr.umontpellier.controller;

import fr.umontpellier.model.Backup;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileTransferController {

    @FXML
    Button exportBrowseButton, exportButton, importButton, importBrowseButton;
    @FXML
    Button viewFilesButton, restoreFilesButton, deleteFilesButton;

    @FXML
    TextField folderPathField, extensionField, destinationPathField;

    @FXML
    private ListView<String> backupListView;
    @FXML
    private ListView<String> filesListView;

    private Stage stage;
    private Socket socket;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Configurez la ListView pour autoriser la sélection multiple
        filesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        backupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /*
     * Méthode pour exporter un dossier entier au serveur
     */
    public void onExport() {
        try {
            out.writeObject("SAVE_REQUEST");
            List<String> extensions = Arrays.asList(this.extensionField.getText().split(" "));
            Backup backup = new Backup(folderPathField.getText(), extensions);
            out.writeObject(backup);
            showBackupSuccessPopup("Sauvegarde Réussie", "La sauvegarde des fichiers a été effectuée avec succès.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onImport() {
        if (socket == null || out == null || in == null) {
            showErrorPopup("Erreur de connexion", "La connexion au serveur n'est pas établie.");
            return;
        }
        try {
            // Envoyer une demande de restauration au serveur
            out.writeObject("RESTORE_ALL_REQUEST");
            out.flush();

            // Attendre et traiter la réponse du serveur
            receiveAndRestoreFiles(destinationPathField.getText());

            // Afficher un message de succès à la fin de la restauration
            showBackupSuccessPopup("Restauration Réussie", "Les fichiers ont été restaurés avec succès.");
        } catch (IOException e) {
            showErrorPopup("Erreur de Restauration", "Une erreur est survenue lors de la tentative de restauration des fichiers.");
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
                        break; // Fin de la restauration
                    }

                    File file = new File(restoreDirectory, relativeFilePath);
                    file.getParentFile().mkdirs(); // Créer les dossiers parents si nécessaire

                    // Lire et enregistrer le contenu du fichier
                    try (FileOutputStream fileOut = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fileOut.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
            unzip(restoreDirectory + "/backup.zip", restoreDirectory);

        } catch (ClassNotFoundException e) {
            showErrorPopup("Erreur de Restauration", "Erreur lors de la réception des données du serveur.");
            e.printStackTrace();
        }
    }

    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                extractFile(zipIn, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
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
        directoryChooser.setTitle("Sélectionnez un Dossier");
        File selectedDirectory = directoryChooser.showDialog(importBrowseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            destinationPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public void onExportSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionnez un Dossier");
        File selectedDirectory = directoryChooser.showDialog(exportBrowseButton.getScene().getWindow());
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

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }


    @FXML
    private void onRefreshBackups() {
        try {
            if (out != null) {
                out.writeObject("LIST_BACKUPS_REQUEST");
                out.flush();

                Object response = in.readObject();
                if (response instanceof List) {
                    List<String> backups = (List<String>) response;
                    backupListView.getItems().setAll(backups);
                }
            } else {
                showErrorPopup("Erreur de connexion", "La connexion au serveur n'est pas établie.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showErrorPopup("Erreur", "Impossible de rafraîchir la liste des sauvegardes.");
        }
    }



    // Méthode pour afficher les fichiers dans une sauvegarde sélectionnée
    @FXML
    private void onViewFiles() {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        if (selectedBackup != null) {
            try {
                out.writeObject("LIST_FILES_REQUEST");
                out.writeObject(selectedBackup);
                out.flush();

                Object response = in.readObject();
                if (response instanceof List) {
                    List<String> files = (List<String>) response;
                    filesListView.getItems().setAll(files);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showErrorPopup("Erreur", "Impossible d'obtenir la liste des fichiers.");
            }
        } else {
            showErrorPopup("Sélection manquante", "Veuillez sélectionner une sauvegarde pour afficher ses fichiers.");
        }
    }


    // Méthode pour restaurer les fichiers sélectionnés
    @FXML
    private void onPartialRestoreFiles() {
        List<String> selectedFiles = new ArrayList<>(filesListView.getSelectionModel().getSelectedItems());
        if (!selectedFiles.isEmpty()) {
            try {
                // On récupère la backup sélectionnée
                String selectedBackup = backupListView.getSelectionModel().getSelectedItem();

                List<String> filesWithBackupPath = selectedFiles.stream()
                        .map(file -> Paths.get(selectedBackup, file).toString())
                        .collect(Collectors.toList());

                out.writeObject("RESTORE_PARTIAL_REQUEST");
                out.writeObject(filesWithBackupPath);
                out.flush();

                // Dossier de destination pour la restauration
                String userHome = System.getProperty("user.home");
                Path destinationDirectory = Paths.get(userHome, "Documents", "RestoredFiles");

                Object response;
                while ((response = in.readObject()) != null) {
                    if (response.equals("RESTORE_COMPLETE")) {
                        break;
                    }

                    Path destFile = destinationDirectory.resolve((String) response);
                    Files.createDirectories(destFile.getParent());
                    try (OutputStream fileOut = Files.newOutputStream(destFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fileOut.write(buffer, 0, bytesRead);
                        }
                    }
                }
                showBackupSuccessPopup("Restauration Réussie", "Les fichiers sélectionnés ont été restaurés.");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showErrorPopup("Erreur de Restauration", "Une erreur est survenue lors de la tentative de restauration des fichiers.");
            }
        } else {
            showErrorPopup("Sélection requise", "Veuillez sélectionner des fichiers à restaurer.");
        }
    }


    // Méthode pour supprimer les fichiers sélectionnés
    @FXML
    private void onDeleteFiles() {
        List<String> selectedFiles = filesListView.getSelectionModel().getSelectedItems();
        if (!selectedFiles.isEmpty()) {
            try {
                // Préfixez chaque fichier avec le nom du dossier de sauvegarde avant de l'envoyer
                String selectedBackup = backupListView.getSelectionModel().getSelectedItem();

                List<String> fullPaths = selectedFiles.stream()
                        .map(fileName -> selectedBackup + "/" + fileName)
                        .collect(Collectors.toList());

                out.writeObject("DELETE_FILES_REQUEST");
                out.writeObject(fullPaths);
                out.flush();


                String response = (String) in.readObject();
                if ("SUCCESS".equals(response)) {
                    showBackupSuccessPopup("Suppression Réussie", "Les fichiers sélectionnés ont été supprimés.");
                } else {
                    showErrorPopup("Erreur de Suppression", "Certains fichiers n'ont pas pu être supprimés.");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showErrorPopup("Erreur de Suppression", "Une erreur est survenue lors de la suppression des fichiers.");
            }
        }
    }

    @FXML
    private void onDeleteBackup() {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        if (selectedBackup != null) {
            try {
                out.writeObject("DELETE_BACKUP_REQUEST");
                out.writeObject(selectedBackup);
                out.flush();

                String response = (String) in.readObject();
                if ("SUCCESS".equals(response)) {
                    showBackupSuccessPopup("Suppression Réussie", "La sauvegarde a été supprimée.");
                    // Rafraîchir la liste des sauvegardes ici, si nécessaire
                } else {
                    showErrorPopup("Erreur de Suppression", "La sauvegarde n'a pas pu être supprimée.");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showErrorPopup("Erreur de Suppression", "Une erreur est survenue lors de la suppression de la sauvegarde.");
            }
        } else {
            showErrorPopup("Sélection requise", "Veuillez sélectionner un backup à supprimer.");
        }
    }







}