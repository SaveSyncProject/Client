package fr.umontpellier.view;

import fr.umontpellier.controller.FileTransferController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class FileBackupView extends Stage {

    public FileBackupView(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/backup-manager.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/material-style.css").toExternalForm());
            this.resizableProperty().setValue(Boolean.FALSE);
            this.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/image/icon.png")));
            FileTransferController controller = loader.getController();
            controller.setStage(this);
            controller.setSocket(socket);
            controller.setIn(in);
            controller.setOut(out);
            this.setTitle("SaveSync");
            this.setScene(scene);
            this.sizeToScene();
            this.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
