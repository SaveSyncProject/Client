package fr.umontpellier.view;

import fr.umontpellier.controller.FileTransferController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileBackupView extends Stage {

    public FileBackupView(Socket socket, BufferedReader in, ObjectOutputStream out) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/save-manager.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/material-style.css").toExternalForm());
            this.resizableProperty().setValue(Boolean.FALSE);
            FileTransferController controller = loader.getController();
            controller.setStage(this);
            controller.setSocket(socket);
            this.setTitle("SaveSync");
            this.setScene(scene);
            this.sizeToScene();
            this.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
