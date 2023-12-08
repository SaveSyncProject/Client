module fr.umontpellier.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;

    exports fr.umontpellier.model;
    opens fr.umontpellier.model;

    exports fr.umontpellier.controller;
    opens fr.umontpellier.controller;

    exports fr.umontpellier.view;
    opens fr.umontpellier.view;

    exports fr.umontpellier.client;
    opens fr.umontpellier.client;
}