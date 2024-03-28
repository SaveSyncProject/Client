package fr.umontpellier;

import fr.umontpellier.view.FileBackupView;
import fr.umontpellier.view.UserAuthView;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new FileBackupView(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}