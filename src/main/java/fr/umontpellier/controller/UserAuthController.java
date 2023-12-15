package fr.umontpellier.controller;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
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
    private BufferedReader in;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void login() {
        String username = this.username.getText();
        String password = this.password.getText();
        String host = this.host.getText();
        int port = 1234;

        try {
            SSLSocket sslSocket = createSSLSocket(host, port);
            out = new ObjectOutputStream(sslSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

            out.writeObject(new User(username, password));

            String response = in.readLine();
            if ("OK".equals(response)) {
                stage.close();
                FileBackupView fileBackupView = new FileBackupView(sslSocket, in, out);
                fileBackupView.show();
            } else {
                textInfo.setText("Identifiants incorrects");
                imageAttention.setVisible(true);
                sslSocket.close(); // Fermer la socket si l'authentification échoue
            }
        } catch (Exception e) {
            e.printStackTrace();
            textInfo.setText("Erreur lors de l'établissement de la connexion SSL.");
            imageAttention.setVisible(true);
        }
    }

    private SSLSocket createSSLSocket(String host, int port) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, KeyManagementException, FileNotFoundException {
        URL truststoreResource = getClass().getResource("/ssl/myClientKeystore.jks");
        if (truststoreResource == null) {
            throw new FileNotFoundException("Le fichier 'myClientKeystore.jks' est introuvable.");
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
