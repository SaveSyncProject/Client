package fr.umontpellier.controller;

import fr.umontpellier.model.User;
import fr.umontpellier.view.FileBackupView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;

public class UserAuthController {

    @FXML
    private TextField username, password, host;

    @FXML
    private Text textInfo;

    @FXML
    private ImageView imageAttention;

    private Stage stage;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void login() {
        String username = this.username.getText();
        String password = this.password.getText();
        String hostInput = this.host.getText();
        int port = 1234;

        try {
            String[] hostParts = hostInput.split(":");
            String host = hostParts[0];
            if (hostParts.length > 1) {
                port = Integer.parseInt(hostParts[1]); // Utilise le port spécifié par l'utilisateur
            }
            SSLSocket sslSocket = createSSLSocket(host, port);
            out = new ObjectOutputStream(sslSocket.getOutputStream());
            in = new ObjectInputStream(sslSocket.getInputStream());

            out.writeObject(new User(username, password));

            String response = (String) in.readObject();
            if ("OK".equals(response)) {
                stage.close();
                new FileBackupView(sslSocket, in, out).show();
            } else {
                textInfo.setText("Incorrect username or password.");
                imageAttention.setVisible(true);
                sslSocket.close();
            }
        } catch (NumberFormatException e) {
            textInfo.setText("Invalid port number.");
            imageAttention.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            textInfo.setText("Unable to connect to the server.");
            imageAttention.setVisible(true);
        }
    }

    private SSLSocket createSSLSocket(String host, int port) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, KeyManagementException, FileNotFoundException {
        URL truststoreResource = getClass().getResource("/ssl/myClientKeystore.jks");
        if (truststoreResource == null) {
            throw new FileNotFoundException("The 'myClientKeystore.jks' file is not found.");
        }

        String truststorePassword = "miaoumiaou";
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(truststoreResource.openStream(), truststorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        return (SSLSocket) sslSocketFactory.createSocket(host, port);
    }
}