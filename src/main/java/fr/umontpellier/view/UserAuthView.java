package fr.umontpellier.view;

import fr.umontpellier.controller.UserAuthController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class UserAuthView extends Stage {

    public UserAuthView(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-auth.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/material-style.css").toExternalForm());
            this.resizableProperty().setValue(Boolean.FALSE);
            this.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/image/icon.png")));
            UserAuthController controller = loader.getController();
            controller.setStage(this);this.setTitle("Authentification");
            this.setScene(scene);
            this.sizeToScene();
            this.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

