module fr.umontpellier.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires unboundid.ldapsdk;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    exports fr.umontpellier.model;
    opens fr.umontpellier.model;

    exports fr.umontpellier.controller;
    opens fr.umontpellier.controller;

    exports fr.umontpellier.view;
    opens fr.umontpellier.view;

    exports fr.umontpellier;
    opens fr.umontpellier;
    exports fr.umontpellier.util;
    opens fr.umontpellier.util;
    exports fr.umontpellier.service;
    opens fr.umontpellier.service;
}