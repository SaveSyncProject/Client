package fr.umontpellier.client;

import fr.umontpellier.view.UserAuthView;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApplication extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            new UserAuthView();
        }

        public static void main(String[] args) {
            launch(args);
        }
}
