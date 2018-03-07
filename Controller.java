package audioplayer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Button play;
    @FXML
    private TextField loc;
    @FXML
    private Slider volumeSlider;
    public Thread playThread;
    public WavPlayer player;

    /*public void playAudio(ActionEvent event){
        WavPlayer wp = new WavPlayer(loc.getCharacters().toString());
        playThread = new Thread(
                new WavPlayer.playAudio()
        );
        wp.initialDataLine();
        playThread.start();
    }*/

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(player != null){
                   float volume = (float) volumeSlider.getValue();
                   volume /= (volumeSlider.getMax() - volumeSlider.getMin());
                   volume = volume * (WavPlayer.volume.getMaximum() - WavPlayer.volume.getMinimum()) + WavPlayer.volume.getMinimum(); //Later adjust;
                    if(playThread.isAlive()){
                        player.changeVolume(volume);
                    }
                }

            }
        });

        play.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                player = new WavPlayer(loc.getCharacters().toString());
                playThread = new Thread(
                        new WavPlayer.playAudio()
                );
                player.initialDataLine();
                playThread.start();
            }
        });
    }
}
