package com.dio;

import com.dio.view.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        new SceneNavigator(stage, getParameters().getRaw()).showMenu();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
