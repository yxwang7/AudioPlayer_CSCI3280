package audioplayer;

import javafx.scene.control.Slider;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class WavPlayer {
    WavReader head;
    SourceDataLine audioLine;
    Object playLock;
    boolean playState;
    FloatControl volume;
    boolean volumeFlag;
    public playTime restTime;

    class playTime{
        public double time;
        playTime(double time){
            this.time = time;
        }
    }


    public void stop(){
        head = null;
        audioLine.drain();
        audioLine.stop();
        playState = false;
        volume = null;
        volumeFlag = true;
    }

    public WavPlayer(String location){
        head = new WavReader(location);
        volume = null;
        restTime = new playTime(-1);
    }

    public void reloadAudio(String newLocation){
        head = new WavReader(newLocation);
        volume = null;
    }

    static AudioInputStream getAudioStream(String path,
                                           AudioFormat fmt, long length){
        try{
            FileInputStream fis = new FileInputStream(path);
            return new AudioInputStream(fis, fmt, length);
        }
        catch(FileNotFoundException e){
            System.out.println("File not found!");
        }
        return null;
    }

    void initialDataLine(){
        DataLine.Info inf = new DataLine.Info(SourceDataLine.class, head.fmt, (int)head.dataLength);
        try{
            audioLine = (SourceDataLine) AudioSystem.getLine(inf);
            audioLine.open(head.fmt);
            volume = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
            volumeFlag = false;
            synchronized(restTime){
                if(restTime.time == -1)
                    restTime.time = head.getAudioTime(head.dataLength);
            }
        }
        catch(LineUnavailableException e){
            //Handling exception with unavailable line
            System.out.println("SourceDataLine is unavailable!");
        }
    }

    void closeDataLine(){
        if(audioLine != null)
        {
            audioLine.drain();
            audioLine.close();
        }
    }

    public playAudio getPlayAudio(){
        return new playAudio();
    }

    class playAudio implements Runnable{
        AudioInputStream ais;
        int bufferSize;

        public playAudio(){
            this.ais = getAudioStream(head.filePath,
                    head.fmt, head.dataLength);
            this.bufferSize = 44100;
        }

        public playAudio(AudioInputStream ais, int bufferSize, Slider bar){
            this.ais = ais;
            this.bufferSize = bufferSize;
        }

        @Override
        public void run() {
            playLock = new Object();
            synchronized (playLock){
                playState = true;
                int currentSize = head.dataLength;
                synchronized (restTime){
                    restTime.time = head.getAudioTime(currentSize);
                }
                audioLine.start();
                byte[] data = new byte[bufferSize];

                try{
                    int dataRead;
                    while((dataRead = ais.read(data)) != -1)
                    {
                        synchronized (restTime){
                            restTime.time -= head.getAudioTime(dataRead);
                        }
                        while(!playState) playLock.wait();
                        audioLine.write(data, 0, data.length);
                    }
                }

                catch(IOException e){

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                closeDataLine();
            }
        }
    }

    /*synchronized void checkPlayState(String s){//test code, to be modified after release of GUI
        if(s.equals("p")){
            playState = false;
        } else if(s.equals("r")){
            playState = true;
            synchronized (playLock) {
                playLock.notify();
            }
        }
    }*/

    synchronized void pause(){
        if(playState) playState = false;
    }

    synchronized void resume(){
        if(!playState){
            playState = true;
            synchronized (playLock) {
                playLock.notify();
            }
        }
    }

    public void changeVolume(float vol){
        volumeFlag = true;
        volume.setValue(vol);
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        //loc = sc.nextLine();
        String loc = "true_love.wav";
        WavPlayer wp = new WavPlayer(loc);
        wp.initialDataLine();
        System.out.println("Max: " + wp.volume.getMaximum());
        System.out.println("Min: " + wp.volume.getMinimum());
        /*Thread play = new Thread(new WavPlayer.playAudio());
        play.start();
        while(play.isAlive()){
            float vol = sc.nextFloat();
            wp.changeVolume(vol);*/
        //}
    }
}
