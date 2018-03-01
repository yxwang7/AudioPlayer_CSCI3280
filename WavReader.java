/**
 * Created and edited by Spike;
 * @author Spike WANG
 * For course CSCI 3280;
 * Date: 2018/3/1
 */


package wavreader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class WavReader {
    String filePath;
    infoPair[] info;
    infoPair[] extendInfo;
    infoPair[] fact;
    FileInputStream audio;
    SourceDataLine line;
    
    class infoPair{
        String name;
        String value;
        String type;
    }
    
    public WavReader(String path){
        filePath = path;
        try{
            audio = new FileInputStream(path);
        }
        catch(FileNotFoundException e){
            System.out.println("Audio file not found!");
        }   
    }
    
    private byte[] endConvert(byte[] info){
        int length = info.length;
        byte[] result = new byte[length];
        for(int i = 0; i < length; i++) result[i] = info[length - 1 - i];
        return result;
    }
    
    void read(){
        if(audio == null)   System.out.println("There is no audio file!");
        else{
            info  = new infoPair[13];
            for(int i = 0; i < 13; i++) info[i] = new infoPair();
            info[0].name = "chunkID"; info[0].type = "String";
            info[1].name = "chunkSize"; info[1].type = "int";
            info[2].name = "Format"; info[2].type = "String";
            info[3].name = "SubChunkID1"; info[3].type = "String";
            info[4].name = "Subchunk1Size"; info[4].type = "int";
            info[5].name = "AudioFormat"; info[5].type = "short";
            info[6].name = "NumChannels"; info[6].type = "short";
            info[7].name = "SampleRate"; info[7].type = "int";
            info[8].name = "ByteRate"; info[8].type = "int";
            info[9].name = "BlockAlign"; info[9].type = "short";
            info[10].name = "BitsPerSample"; info[10].type = "short";
            info[11].name = "Subchunk2ID"; info[11].type = "String";
            info[12].name = "Subchunk2Size"; info[12].type = "int";
            int[] length = {4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4};
            int[] endian = {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1}; //0: big; 1: little;
            for(int i = 0; i < 13; i++){
                try{
                    if(i != 11)
                    {
                        byte[] store = new byte[length[i]];
                        audio.read(store);
                        if(endian[i] == 1) store = endConvert(store);
                        if(info[i].type.equals("int"))
                        {
                            ByteBuffer buffer = ByteBuffer.wrap(store);
                            int result = buffer.getInt();
                            info[i].value = Integer.toString(result);
                        }
                        else if(info[i].type.equals("short"))
                        {
                            ByteBuffer buffer = ByteBuffer.wrap(store);
                            short result = buffer.getShort();
                            info[i].value = Short.toString(result);
                        }
                        else info[i].value = new String(store);
                    }
                    else{
                        byte[] extSize = new byte[2];
                        audio.read(extSize);
                        byte[] store = new byte[4];
                        
                        if(extSize[0] == 0 && extSize[1] == 0){
                            audio.read(store);
                        }
                        else if(extSize[0] == 0 && extSize[1] == 16){
                            //audio.skip(22); audio.read(store);
                            extendInfo = new infoPair[3];
                            extendInfo[0].name = "ValidBitPerSample"; extendInfo[0].type = "short"; 
                            extendInfo[1].name = "dwChannelMask"; extendInfo[1].type = "int"; 
                            extendInfo[3].name = "subFormat"; extendInfo[2].type = "UUID";
                            int[] exSize = {2, 4, 16};
                            int[] exEndian = {1, 1, 0};
                            for(int j = 0; j < 3; j++){
                                byte[] storeB = new byte[exSize[i]];
                                audio.read(storeB);
                                if(endian[i] == 1) storeB = endConvert(storeB);
                                ByteBuffer buffer = ByteBuffer.wrap(storeB);
                                if(extendInfo[i].type.equals("int"))
                                {
                                    int result = buffer.getInt();
                                    extendInfo[i].value = Integer.toString(result);
                                }
                                else if(extendInfo[i].type.equals("short"))
                                {
                                    short result = buffer.getShort();
                                    extendInfo[i].value = Short.toString(result);
                                }
                                else 
                                {
                                    long num1 = buffer.getLong();
                                    long num2 = buffer.getLong();
                                    UUID ID   = new UUID(num1, num2);
                                    extendInfo[i].value = ID.toString();
                                }
                            }
                        }
                        else{
                            store[0] = extSize[0]; store[1] = extSize[1];
                            audio.read(extSize);
                            store[2] = extSize[0]; store[3] = extSize[1];
                        }
                        
                        String target = new String(store);
                        while(!target.equals("data")){
                            //Read fact chunk here
                            byte[] chunkSize = new byte[4];
                            audio.read(chunkSize);
                            chunkSize = endConvert(chunkSize);
                            int size = ByteBuffer.wrap(chunkSize).getInt();
                            audio.skip(size);
                            store = new byte[length[i]];
                            audio.read(store);
                            target = new String(store);
                        }
                        /*byte[] store = new byte[4];
                        audio.read(store);
                        String target = new String(store);*/
                        while(!target.equals("data")){
                            byte[] chunkSize = new byte[4];
                            audio.read(chunkSize);
                            chunkSize = endConvert(chunkSize);
                            int size = ByteBuffer.wrap(chunkSize).getInt();
                            audio.skip(size);
                            store = new byte[length[i]];
                            audio.read(store);
                            target = new String(store);
                        }
                        info[11].value = target;
                    }
                }
                catch(IOException e){
                    System.out.println("Unexpexted of file end!");
                }
            }
        }
    }
    
    void playAudio(){
        if(info == null) read();
        
        int bitDepth = Integer.parseInt(info[10].value);
        boolean signed = bitDepth > 8;
        int dataLength = Integer.parseInt(info[12].value);
        AudioFormat af = new AudioFormat(
            Integer.parseInt(info[7].value), 
            Integer.parseInt(info[10].value), 
            Integer.parseInt(info[6].value),
            signed, false);
        DataLine.Info inf = new DataLine.Info(SourceDataLine.class, af, dataLength);
        try {
            SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(inf);
            soundLine.open(af);
            soundLine.start();
            byte[] data = new byte[dataLength];
            audio.read(data);
            soundLine.write(data, 0, data.length);

        } catch (LineUnavailableException ex) {
            System.out.println("The line is unavailable");
        } catch (IOException ex) {
            System.out.println("End of file!");
        }
    }
    
    public static void main(String[] args) {
        String filePath = "true_love.wav";
        WavReader reader = new WavReader(filePath);
        reader.read();
        for(int i = 0; i < 13; i++){
            System.out.println(reader.info[i].name + ": " + reader.info[i].value);
        }
        reader.playAudio();
    }
}
