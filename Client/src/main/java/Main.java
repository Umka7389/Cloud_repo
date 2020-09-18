import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("StartPanel.fxml"));
        primaryStage.setTitle("Cloud repository");
        primaryStage.setScene(new Scene(root,800,600));
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            ServerConnectionController.stopConnection();
            Platform.exit();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
