package fr.umontpellier.controller;

import fr.umontpellier.model.Backup;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileTransferController {

    @FXML
    Button exportBrowseButton, exportButton, importButton, importBrowseButton;

    @FXML
    TextField folderPathField, extensionField, destinationPathField;

    private Stage stage;
    private Socket socket;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

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
            out.writeObject("RESTORE_REQUEST");
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
                        break;
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
}