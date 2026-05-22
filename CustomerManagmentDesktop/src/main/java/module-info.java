module com.javap.customermanagmentdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires okhttp3;

    opens com.javap.customermanagmentdesktop to javafx.graphics, javafx.fxml;
    opens com.javap.customermanagmentdesktop.model to com.fasterxml.jackson.databind;
    opens com.javap.customermanagmentdesktop.ui to javafx.fxml, javafx.graphics;

    exports com.javap.customermanagmentdesktop;
    exports com.javap.customermanagmentdesktop.model;
    exports com.javap.customermanagmentdesktop.service;
    exports com.javap.customermanagmentdesktop.ui;
    exports com.javap.customermanagmentdesktop.util;
}