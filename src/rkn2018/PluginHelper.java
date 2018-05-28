package rkn2018;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PluginHelper {

    private HashMap<String, String> helperMap = new HashMap<>();
    //******************************************************************************************************************
    PluginHelper(Proxy proxy_instance_)
    {
        this.helperMap = new HashMap<>(proxy_instance_.getReplacements());
    }
    //******************************************************************************************************************
    String addCookies(String input)
    {
        String[] parsed = input.split("\r\n");
        String returnCookies = "";
        for(int i = 0; i != parsed.length; i++)
        {
            returnCookies = returnCookies + parsed[i] + "\r\n";
        }

        for(Map.Entry<String,String> it : helperMap.entrySet())
        {
            if(it.getKey().contains("Set-Cookie"))
            {
                returnCookies = returnCookies + it.getKey() + ": " + it.getValue();
            }

        }
        returnCookies += "\r\n";
        return returnCookies;
    }
    //******************************************************************************************************************
    String contentLengthSetting(String input, int length, String encodingValue_)
    {
        //first we need to remove chunks in order to set content length
        byte[] stringByteWithoutChunks = new byte[1000000];
        String returnString = "";
        String[] parsed = input.split("\r\n");
        for(int i = 0; i < parsed.length; i++)
        {
            if(!parsed[i].contains("Transfer-Encoding") ||
                    !parsed[i].contains("Content-Length"))
            {
                returnString += parsed[i] + "\r\n";
            }
        }
        returnString += "\r\n";

        try {
            stringByteWithoutChunks = returnString.getBytes(encodingValue_);
        }
        catch (IOException e)
        {
            System.out.println("Can not convert string to byte array");
            e.printStackTrace();
        }

        String[] parsed_withoutChunks = new String(stringByteWithoutChunks).split("\r\n");
        String stringWithoutChunks = "";

        //then add content-length
        for(int i = 0; i < parsed_withoutChunks.length; i++)
        {
            //set every time
            if(parsed_withoutChunks[i].contains("Content-Length"))
                continue;
            stringWithoutChunks += parsed[i] + "\r\n";

        }
        stringWithoutChunks += "Content-Length: " + length + "\r\n";
        stringWithoutChunks += "\r\n";

        return stringWithoutChunks;
    }

    //******************************************************************************************************************
    String editContentLength(String input, int length)
    {
        String[] parsed = input.split("\r\n");
        String returnString = "";

        for(int i = 0; i < parsed.length; i++)
        {
            //change content_length if it exists
            if(parsed[i].contains("Content-Length"))
            {
                //System.out.println("Changed length from " + parsed[i] +  " to " + length);
                returnString += "Content-Length: " + length + "\r\n";
            }
            else
                returnString += parsed[i] + "\r\n";
        }

        returnString += "\r\n";

        return returnString;
    }

}
