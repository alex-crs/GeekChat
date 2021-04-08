package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        InputStream iconStream =
                getClass().getResourceAsStream("icon.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(140);
        FXMLLoader mainWindow = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent main = mainWindow.load();
        primaryStage.setTitle("ChatForOne");
        Scene scene = new Scene(main, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Controller controller = mainWindow.getController();
                controller.disconnect();
            }
        });
    }


    public static void main(String[] args) {

        launch(args);
    }
}
