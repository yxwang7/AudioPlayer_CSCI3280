package audioplayer; /**
 * Created and edited by Spike;
 * @author Spike WANG
 * For course project of CSCI 3280: Introduction to multimedia system;
 * Date: 2018/3/1
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.sound.sampled.*;

public class WavReader {
    String filePath;
    infoPair[] info;
    infoPair[] extendInfo;
    int dataLength;
    int headSize;
    AudioFormat fmt;

    class infoPair{
        String name;
        String value;
        String type;
    }

    AudioFormat getFormat(){
        int bitDepth = Integer.parseInt(info[10].value);
        boolean signed = bitDepth > 8;
        AudioFormat af = new AudioFormat(
                Integer.parseInt(info[7].value),
                Integer.parseInt(info[10].value),
                Integer.parseInt(info[6].value),
                signed, false);
        return af;
    }

    public WavReader(String path){
        filePath = path;
        read();
        fmt = getFormat();
    }

    private byte[] endConvert(byte[] info){
        int length = info.length;
        byte[] result = new byte[length];
        for(int i = 0; i < length; i++) result[i] = info[length - 1 - i];
        return result;
    }

    public infoPair[] audioInfo(){
        if(info == null){
            System.out.println("No audio has been read!");
            return null;
        }
        else
            return info;
    }

    void read(){
        try{
            FileInputStream audio = new FileInputStream(filePath);
            headSize = 0;
            info  = new infoPair[13];
            for(int i = 0; i < 13; i++) info[i] = new infoPair();
            info[0].name = "ChunkID";
            info[1].name = "ChunkSize";
            info[2].name = "Format";
            info[3].name = "SubChunkID1";
            info[4].name = "Subchunk1Size";
            info[5].name = "AudioFormat";
            info[6].name = "NumChannels";
            info[7].name = "SampleRate";
            info[8].name = "ByteRate";
            info[9].name = "BlockAlign";
            info[10].name = "BitsPerSample";
            info[11].name = "Subchunk2ID";
            info[12].name = "Subchunk2Size";
            final int[] length = {4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4};
            final int[] type   = {0, 1, 0, 0, 1, 2, 2, 1, 1, 2, 2, 0, 1};// 0: String, 1: int, 2: short
            final int[] endian = {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1}; //0: big; 1: little;
            for(int i = 0; i < 13; i++){
                try{
                    if(i != 11)
                    {
                        byte[] store = new byte[length[i]];
                        headSize += length[i];
                        audio.read(store);
                        if(endian[i] == 1) store = endConvert(store);
                        switch(type[i]){
                            case 0:{
                                info[i].type = "String";
                                info[i].value = new String(store);
                                break;
                            }
                            case 1:{
                                info[i].type = "int";
                                ByteBuffer buffer = ByteBuffer.wrap(store);
                                int result = buffer.getInt();
                                info[i].value = Integer.toString(result);
                                break;
                            }
                            case 2:{
                                info[i].type = "short";
                                ByteBuffer buffer = ByteBuffer.wrap(store);
                                short result = buffer.getShort();
                                info[i].value = Short.toString(result);
                                break;
                            }
                        }
                    }
                    else{
                        byte[] extSize = new byte[2];
                        audio.read(extSize);
                        headSize += 2;
                        byte[] store = new byte[4];

                        if(extSize[0] == 0 && extSize[1] == 0){
                            audio.read(store);
                            headSize += 4;
                        }
                        else if(extSize[0] == 0 && extSize[1] == 16){
                            //audio.skip(22); audio.read(store);
                            extendInfo = new infoPair[3];
                            extendInfo[0].name = "ValidBitPerSample"; extendInfo[0].type = "short";
                            extendInfo[1].name = "dwChannelMask"; extendInfo[1].type = "int";
                            extendInfo[3].name = "subFormat"; extendInfo[2].type = "UUID";
                            int[] exSize = {2, 4, 16};
                            for(int j = 0; j < 3; j++){
                                byte[] storeB = new byte[exSize[j]];
                                audio.read(storeB);
                                headSize += exSize[j];
                                ByteBuffer buffer = ByteBuffer.wrap(storeB);
                                switch(j)
                                {
                                    case 0:
                                    {
                                        short result = buffer.getShort();
                                        extendInfo[0].value = Short.toString(result);
                                        break;
                                    }
                                    case 1:
                                    {
                                        int result = buffer.getInt();
                                        extendInfo[1].value = Integer.toString(result);
                                        break;
                                    }
                                    case 2:
                                    {
                                        long num1 = buffer.getLong();
                                        long num2 = buffer.getLong();
                                        UUID ID   = new UUID(num1, num2);
                                        extendInfo[2].value = ID.toString();
                                        break;
                                    }
                                }
                            }
                        }
                        else{
                            store[0] = extSize[0]; store[1] = extSize[1];
                            audio.read(extSize);
                            headSize += 2;
                            store[2] = extSize[0]; store[3] = extSize[1];
                        }

                        String target = new String(store);
                        while(!target.equals("data")){
                            //Read fact chunk here
                            byte[] chunkSize = new byte[4];
                            audio.read(chunkSize);
                            headSize += 4;
                            chunkSize = endConvert(chunkSize);
                            int size = ByteBuffer.wrap(chunkSize).getInt();
                            audio.skip(size);
                            headSize += size;
                            store = new byte[length[i]];
                            audio.read(store);
                            headSize += 4;
                            target = new String(store);
                        }
                    /*byte[] store = new byte[4];
                    audio.read(store);
                    String target = new String(store);*/
                        info[11].value = target;
                    }
                }
                catch(IOException e){
                    System.out.println("Unexpexted of file end!");
                }
            }
            dataLength = Integer.parseInt(info[12].value);
        }
        catch(IOException e){//Handle
            System.out.println("File is not found!");
        }
    }

    public double getAudioTime(long restDataLength){
        if(info != null)
            return 1.0 * restDataLength * 8 /(1.0 * Integer.parseInt(info[7].value)) / (1.0 * Integer.parseInt(info[6].value) * Integer.parseInt(info[10].value));
        else return -1;
    }

    public static void main(String[] args) {
        String filePath = "true_love.wav";
        WavReader reader = new WavReader(filePath);
        for(int i = 0; i < 13; i++){
            System.out.println(reader.info[i].name + ": " + reader.info[i].value);
        }
    }
}
