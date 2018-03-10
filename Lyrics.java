/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import lyrics.AnotherList;
import lyrics.SynchronousLyrics;
/**
 *
 * @author Sleepy
 */

public class Lyrics {

    /**
     * @param filePath
     * @return 
     */
    public static List<String> readTxtFileIntoStringArrList(String filePath)
    {
        List<String> list = new ArrayList<String>();
        try
        {
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null)
                {
                    list.add(lineTxt);
                    //list.add("\n");
                }
                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("Cannot find file.");
            }
        }
        catch (Exception e)
        {
            System.out.println("Error reading.");
            e.printStackTrace();
        }
        return list;
    }
    
    public static long Convert (String str)
    {
        long time = 0;
        str = str.replace('.', ':');
        str = str.replace('[','-');
        str = str.replace(']','-');
        String[] divide = str.split("-");
        String[] timeString = divide[1].split(":");
        
        time = Integer.valueOf(timeString[0])*60*100+
                Integer.valueOf(timeString[1])*100+
                Integer.valueOf(timeString[2]);
        return time;
    }
    
    public static String Content(String str)
    {
        String content;
        String[] divide = str.split("]");
        content = divide[1];
        return content;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        List<String> stringList;
        stringList = readTxtFileIntoStringArrList("C:\\Users\\Sleepy\\Desktop\\lyrics\\test\\七里香.lrc");
        /*for(String str : stringList)
        {
            long time = Convert(str);
            System.out.print(time+" ");
            String content = Content(str);
            System.out.println(content);
            System.out.println(str);
            
        }*/
        List<AnotherList> newList = AnotherList.useList(stringList);
        for(AnotherList str:newList)
        {
            System.out.print(str.strTime+" ");
            System.out.print(str.endTime+" ");
            System.out.println(str.content);
        }
        String play = SynchronousLyrics.playSyn(33.55, newList);
        System.out.println(play);
    }
}
