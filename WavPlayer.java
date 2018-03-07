package audioplayer;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class WavPlayer {
    static WavReader head;
    static SourceDataLine audioLine;
    static Object playLock;
    static boolean playState;
    public static FloatControl volume;
    static boolean volumeFlag;

    public WavPlayer(String location){
        head = new WavReader(location);
        volume = null;
    }

    public static void reloadAudio(String newLocation){
        head = new WavReader(newLocation);
        playLock = null;
        volume = null;
    }

    static AudioInputStream getAudioStream(String path,
                                           AudioFormat fmt, int length){
        try{
            FileInputStream fis = new FileInputStream(path);
            return new AudioInputStream(fis, fmt, length);
        }
        catch(FileNotFoundException e){
            System.out.println("File not found!");
        }
        return null;
    }

    static void initialDataLine(){
        DataLine.Info inf = new DataLine.Info(SourceDataLine.class, head.fmt, head.dataLength);
        try{
            audioLine = (SourceDataLine) AudioSystem.getLine(inf);
            audioLine.open(head.fmt);
            volume = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
            volumeFlag = false;
        }
        catch(LineUnavailableException e){
            //Handling exception with unavailable line
            System.out.println("SourceDataLine is unavailable!");
        }
    }

    static void closeDataLine(){
        if(audioLine != null)
        {
            audioLine.drain();
            audioLine.close();
        }
    }

     static class playAudio implements Runnable{
        AudioInputStream ais;
        int bufferSize;

        public playAudio(){
            this.ais = getAudioStream(head.filePath,
                    head.fmt, head.dataLength);
            this.bufferSize = 128;
        }

        public playAudio(AudioInputStream ais, int bufferSize){
            this.ais = ais;
            this.bufferSize = bufferSize;
        }

        @Override
        public void run() {
            playLock = new Object();
            synchronized (playLock){
                playState = true;
                audioLine.start();
                byte[] data = new byte[bufferSize];
                try{
                    while(ais.read(data) != -1)
                    {
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

    static synchronized void checkPlayState(String s){//test code, to be modified after release of GUI
        if(s.equals("p")){
            playState = false;
        } else if(s.equals("r")){
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
        Thread play = new Thread(new WavPlayer.playAudio());
        play.start();
        while(play.isAlive()){
            float vol = sc.nextFloat();
            wp.changeVolume(vol);
        }
    }
}
