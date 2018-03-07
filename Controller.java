package audioplayer;
import javafx.application.Platform;
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
import javafx.concurrent.*;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Thread.sleep;

public class Controller implements Initializable {
    @FXML
    private Button play;
    @FXML
    private TextField loc;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider progressSlider;
    @FXML
    private Button pause;
    @FXML
    private Button stop;

    public Thread timeProgress;
    public Thread playThread;
    public WavPlayer player;

    class prgBar implements Runnable{
        Slider bar;

        public prgBar(Slider bar){
            this.bar = bar;
        }

        public void run() {
            bar.setMax(player.restTime.time);
            double max = bar.getMax();
            Runnable rn = new Runnable() {
                @Override
                public void run() {
                    bar.setValue(max - player.restTime.time);
                }
            };
            while(player.restTime.time >= 0){
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (player.restTime) {
                    Platform.runLater(rn);
                }
            }
        }
    }

    /*public void playAudio(ActionEvent event){
        WavPlayer wp = new WavPlayer(loc.getCharacters().toString());
        playThread = new Thread(
                new WavPlayer.playAudio()
        );
        wp.initialDataLine();
        playThread.start();
    }*/

    public void shunDown(){
        timeProgress.interrupt();
        player.stop();
        playThread.interrupt();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        volumeSlider.setValue(98);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(player != null){
                   float volume = (float) volumeSlider.getValue();
                   volume /= (volumeSlider.getMax() - volumeSlider.getMin());
                   volume *= (pow(10, player.volume.getMaximum() / 20.0) - pow(10, player.volume.getMinimum() / 20.0));
                   volume = (float) log10(volume) * 20;//Later adjust;
                    player.changeVolume(volume);
                }
            }
        });

        play.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(player == null || player.head == null)
                {
                    //player = new WavPlayer(loc.getCharacters().toString());
                    player = new WavPlayer("true_love.wav");
                    playThread = new Thread(
                            player.getPlayAudio()
                    );
                    player.initialDataLine();
                    player.volume.setValue(0);
                    timeProgress = new Thread(new prgBar(progressSlider));
                    timeProgress.setDaemon(true);
                    playThread.start();
                    timeProgress.start();
                }
                else
                {
                    if(!player.playState){
                        synchronized (player)
                        {
                            player.resume();
                        }
                    }
                }
            }
        });

        pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                synchronized(player){
                    player.pause();
                }
            }
        });

        stop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                synchronized(player){
                    player.stop();
                    player.notify();
                    playThread.interrupt();
                }
            }
        });
    }
}
