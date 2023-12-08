package fr.umontpellier.controller;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import fr.umontpellier.view.SaveManagerView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserAuthController {

    @FXML
    public TextField username, password;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void login() {
        String username = this.username.getText();
        String password = this.password.getText();

        if (authenticateWithLDAP(username, password)) {
            System.out.println("Connexion réussie!");
            new SaveManagerView();
            stage.close();
        } else {
            System.out.println("Échec de la connexion.");
        }

    }

    private boolean authenticateWithLDAP(String username, String password) {
        try {
            LDAPConnection connection = new LDAPConnection("localhost", 389);
            connection.bind("uid=" + username + ",ou=users,dc=example,dc=org", password);
            connection.close();
            return true;
        } catch (LDAPException e) {
            e.printStackTrace();
            return false;
        }
    }

}