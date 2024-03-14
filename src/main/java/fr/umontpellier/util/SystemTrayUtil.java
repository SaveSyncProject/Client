package fr.umontpellier.util;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.stage.Stage;

public class SystemTrayUtil {

    public static void addToSystemTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        Platform.setImplicitExit(false);

        try {
            BufferedImage trayIconImage = ImageIO.read(Objects.requireNonNull(SystemTrayUtil.class.getResourceAsStream("/image/icon.png")));
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            TrayIcon trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));

            ActionListener showListener = e -> Platform.runLater(stage::show);
            trayIcon.addActionListener(showListener);

            PopupMenu popup = new PopupMenu();
            MenuItem openItem = new MenuItem("Open");
            openItem.addActionListener(showListener);
            popup.add(openItem);

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                Platform.exit();
                SystemTray.getSystemTray().remove(trayIcon);
                System.exit(0);
            });
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);

            SystemTray.getSystemTray().add(trayIcon);

            stage.setOnCloseRequest(e -> {
                stage.hide();
                e.consume();
            });
        } catch (IOException e) {
            System.err.println("Icon file not found.");
        } catch (AWTException e) {
            System.err.println("Unable to add the icon to system tray.");
        }
    }
}