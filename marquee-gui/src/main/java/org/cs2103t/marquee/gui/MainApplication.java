package org.cs2103t.marquee.gui;

import java.io.IOException;

import org.cs2103t.marquee.gui.ui.MainMenu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Marquee GUI.
 */
public class MainApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        MainMenu mainMenu = new MainMenu();
        Scene scene = new Scene(mainMenu);
        stage.setTitle("Marquee");
        stage.setScene(scene);
        stage.setMinWidth(mainMenu.getMinWidth());
        stage.setMinHeight(mainMenu.getMinHeight());
        stage.show();
    }
}
