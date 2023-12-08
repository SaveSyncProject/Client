package fr.umontpellier.view;

import fr.umontpellier.controller.UserController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserAuthView extends Stage {

    public UserAuthView(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-auth.fxml"));
            Scene scene = new Scene(loader.load());
            this.resizableProperty().setValue(Boolean.FALSE);
            UserController controller = loader.getController();
            controller.setStage(this);
            this.setTitle("Authentification");
            this.setScene(scene);
            this.sizeToScene();
            this.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

