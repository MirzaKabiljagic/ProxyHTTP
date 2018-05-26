package rkn2018;

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
    String SetContentLength(String input, int length)
    {
        String[] parsed = input.split("\r\n");
        String returnString = "";

        for(int i = 0; i != parsed.length; i++)
        {

            //set every time
            if(parsed[0].equals("Content-Length"))
                continue;

            returnString += parsed[i] + "\r\n";


        }
        returnString += "Content-Length: " + length + "\r\n";
        returnString += "\r\n";

        return returnString;
    }

    //******************************************************************************************************************
    String ChangeContentLength(String input, int length)
    {
        String[] parsed = input.split("\r\n");
        String returnString = "";

        for(int i = 0; i != parsed.length; i++)
        {
            //change content_length if it exists
            if(parsed[0].equals("Content-Length"))
            {
                returnString += "Content-Length: " + length + "\r\n";
            }

            returnString += parsed[i] + "\r\n";

        }

        returnString += "\r\n";

        return returnString;
    }
    //******************************************************************************************************************
    String removeCunks(String input)
    {
        String returnString = "";
        String[] parsed = input.split("\r\n");
        for(int i = 0; i != parsed.length; i++)
        {
            if(!parsed[0].equals("Transfer-Encoding") ||
                    !parsed[0].equals("Content-Length"))
            {
                returnString += parsed[i] + "\r\n";
            }
        }
        returnString += "\r\n";
        return returnString;
    }
}
