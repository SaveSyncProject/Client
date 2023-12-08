package fr.umontpellier.view;

import fr.umontpellier.controller.SaveManagerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SaveManagerView extends Stage {

    public SaveManagerView() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/save-manager.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/material-style.css").toExternalForm());
            this.resizableProperty().setValue(Boolean.FALSE);
            SaveManagerController controller = loader.getController();
            controller.setStage(this);
            this.setTitle("SaveSync");
            this.setScene(scene);
            this.sizeToScene();
            this.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
