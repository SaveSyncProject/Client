package fr.umontpellier.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserController {

    public UserController() {
        System.out.println("UserController");
    }

    @FXML
    public TextField username, password;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void login() {
        String username = this.username.getText();
        String password = this.password.getText();
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        stage.close();
    }

}