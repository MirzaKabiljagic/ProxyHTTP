package rkn2018;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Parser {
    private String url;
    private String method;
    private String version;
    private String host;

    Parser()
    {

    }

    public String getUrl()
    {
        return  url;
    }

    public String getMethod()
    {
        return method;

    }

    public String getVersion()
    {
        return version;
    }

    public String getHost() {
        return host;
    }

    public void startParse(Socket ws)
            throws IOException
    {

        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(ws.getInputStream()));
        String input;
        String newline = "\r\n";
        String helper = "";
        boolean findCertainParameter = true;
        int count = 0;

        while(findCertainParameter)
        {
            input = inputBuffer.readLine();
            helper = input + newline;

            if(count == 0)
            {
                String [] arrayParser = input.split(" ");

                if(arrayParser[0].equals("GET"))
                    method = "GET";

                if(arrayParser[0].equals("CONNECT"))
                {
                    method = "CONNECT";
                    host = arrayParser[1];
                    findCertainParameter = false;
                }


                if(arrayParser[2].equals("HTTP/1.1"))
                    version = "HTTP/1.1";

                url = arrayParser[1];

                //System.out.println(method);
                //System.out.println(version);
                //System.out.println(url);
            }
            else
            {
                String []helperString = input.split("Host: ");
                //System.out.println("jedan" + helperString[0]);
                //System.out.println("dva" + helperString[1]);
                host = helperString[1];
                findCertainParameter = false;
                System.out.println(host);
            }

            count ++;
        }








    }
};





