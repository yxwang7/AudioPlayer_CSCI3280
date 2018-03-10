/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyrics;
import java.util.ArrayList;
import java.util.List;
import lyrics.Lyrics;
/**
 *
 * @author Sleepy
 */
public class AnotherList {
    public String content;
    public long strTime;
    public long endTime;
    
    public AnotherList(){}
    public AnotherList(String content,long strTime,long endTime){
        this.strTime = strTime;
        this.content = content;
        this.endTime = endTime;
    }
    
    public static List<AnotherList> useList(List<String> list)
    {
        List<AnotherList> newList = new ArrayList<AnotherList>();
        long[] timeArr = new long[1000];
        String[] contentArr = new String[1000];
        int i = 1;
        try{
            for(String line:list)
            {
                long time = Lyrics.Convert(line);
                String content = Lyrics.Content(line);
                timeArr[i] = time;
                contentArr[i] = content;
                i++;
            }
            timeArr[i+1] = 30000;
            for(i = 1;contentArr[i]!=null;i++)
            {
                long strTime = timeArr[i];
                long endTime = timeArr[i+1]-1;
                String content = contentArr[i];
                AnotherList row = new AnotherList(content,strTime,endTime);
                newList.add(row);
            }
            return newList;
        }catch(Exception e)
        {
            return null;
        }
    }
}
