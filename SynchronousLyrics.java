/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyrics;
import java.util.List;
import java.util.ArrayList;
import lyrics.Lyrics;
import lyrics.AnotherList;
/**
 *
 * @author Sleepy
 */
public class SynchronousLyrics {
    public long strTime;
    public long endTime;
    public String content;
    public SynchronousLyrics(){}
    public SynchronousLyrics(long strTime,String content,long endTime){
        this.strTime = strTime;
        this.content = content;
        this.endTime = endTime;
    }
    
    public static String playSyn(double currentTime,List<AnotherList> newList)
    {
        long time = (int)(currentTime*100);
        String content = null;
        System.out.println("time = "+time);
        try{
            for(AnotherList str:newList)
            {
                if(time>=str.strTime&&time<=str.endTime)
                {
                    content = str.content;
                    break;
                }
                else
                {
                    continue;
                }
            }
        }
        catch(Exception e){}
        return content;
    }
}
