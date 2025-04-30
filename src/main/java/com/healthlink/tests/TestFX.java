package com.healthlink.tests;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TestFX extends Application {
    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("JavaFX Works!"), 300, 200));
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}