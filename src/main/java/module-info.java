module bubbleSorting {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.antlr.antlr4.runtime;

    // Se usi Jackson/SLF4J direttamente
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires java.net.http;
    requires java.prefs;
    requires jdk.jfr;
    requires org.json;

    // Esportazioni “normali”
    exports it.unical.informatica;
    exports it.unical.informatica.controller;
    exports it.unical.informatica.model;
    exports it.unical.informatica.view;
    exports it.unical.informatica.asp to org.antlr.antlr4.runtime;


    // Aperture per riflessione (FXML; EmbASP usa riflessione -> apri senza target)
    opens it.unical.informatica to javafx.fxml;
    opens it.unical.informatica.controller to javafx.fxml;
    opens it.unical.informatica.model to javafx.fxml;
    opens it.unical.informatica.asp to javafx.fxml;

}
