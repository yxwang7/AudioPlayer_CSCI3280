package audioplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("playerPanel.fxml"));
        primaryStage.setTitle("Test audio player");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("playerPanel.fxml"));
        Pane pane = (Pane) loader.load();
        Controller controller =
                loader.<Controller>getController();
        /*while(true){
            if(Thread.currentThread().isInterrupted())
            {
                controller.shunDown();
                break;
            }
        }*/

    }

    public static void main(String[] args) {
        launch(args);
    }
}
