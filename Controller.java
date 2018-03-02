package sample;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Button Play;
    @FXML
    private TextField Location;

    public void playAudio(ActionEvent event){
        CharSequence loc = Location.getCharacters();
        String audioFileLocation = loc.toString();
        WavReader wv = new WavReader(audioFileLocation);
        wv.playAudio();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
