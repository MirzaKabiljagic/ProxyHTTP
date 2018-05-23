package rkn2018;

import java.io.*;
import java.util.HashMap;


public class Parser {
    private String method;
    private String helper_response;

    public static final int HOST = 1;
    public static final int CONNECTION = 2;
    public static final int CONTENT_ENCODING = 3;
    public static final int CONTENT_TYPE = 4;
    public static final int CONTENT_LENGHT = 5;

    private boolean parsed = false;
    private boolean isParsed;

    private HashMap<String, String> header_response;

    Parser()
    {
        System.out.println("Constructor of Parser.");
        header_response = new HashMap<>();
        helper_response = "";
        isParsed = false;
    }

    public String getMethod() { return method; }

    public boolean getParsed() { return parsed; }

    public boolean isParsed(){ return isParsed; }

    public String getResponseValues(String name)
    {
        return header_response.get(name);
    }

    public void checkEnd(byte[] inputClient_)
    {
        System.out.println("We are checking end of header");
        if(valuesFromField().get(CONTENT_LENGHT) != null)
        {
            int content_len = Integer.parseInt(valuesFromField().get(CONTENT_LENGHT));
            if(content_len == inputClient_.length - headerSize())
            {
                parsed = true;
                System.out.println("We are finish with checking header.");
            }
        }
        else if(inputClient_[inputClient_.length - 1] == 10 && inputClient_[inputClient_.length - 2] == 13 && inputClient_[inputClient_.length - 3] == 10 &&
                inputClient_[inputClient_.length - 4] == 13)
        {
            parsed = true;
            System.out.println("We are finish with checking header.");
        }
        else
        {
            parsed = false;
            System.out.println("We are finish with checking header. Header is not correct!");
        }
    }

    public void startParse(byte[] inputClient)
            throws IOException
    {
        if(!isParsed())
        {
            System.out.println("Starting with parsing!");
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inputClient)));
            String input;
            String newline = "\r\n";
            int count = 0;
            String[] arrayParser;

            while((input = inputBuffer.readLine()) != null && !input.isEmpty())
            {
                helper_response += input + newline;

                if(count == 0)
                {
                    arrayParser = input.split(" ");

                    if(arrayParser[0].equals("GET"))
                        method = "GET";

                    if(arrayParser[0].equals("CONNECT"))
                        method = "CONNECT";
                }
                else
                {
                    arrayParser = input.split(": ");
                    if(arrayParser.length == 2)
                        header_response.put(arrayParser[0], arrayParser[1]);
                    else
                        System.out.println("Size of arrayParser bigger than 2 and is: " + arrayParser.length);
                }
                count++;
            }
            helper_response += newline;
            isParsed = true;

            checkEnd(inputClient);
            System.out.println("Finishing with parsing!");
        }
    }

    public  HashMap<Integer, String>valuesFromField()
    {
        HashMap<Integer, String> hash_map = new HashMap<>();
        String host = getResponseValues("Host"); 
        if(host != null)
            host.replaceAll("\\s+", "");
        hash_map.put(HOST, host);

        String connection = getResponseValues("Connection");
        if(connection != null)
             connection.replaceAll("\\s+", "");
        hash_map.put(CONNECTION, connection);

        String content_encoding = getResponseValues("Content-Encoding");
        if(content_encoding != null)
            content_encoding.replaceAll("\\s+", "");
        hash_map.put(CONTENT_ENCODING, connection);

        String content_type = getResponseValues("Content-Type");
        if(content_type != null)
            content_type.replaceAll("\\s+", "");
        hash_map.put(CONTENT_TYPE, content_type);

        String content_length = getResponseValues("Content-Length");
        if(content_length != null)
            content_length.replaceAll("\\s+", "");
        hash_map.put(CONTENT_LENGHT, content_length);

        return hash_map;
    }

    int headerSize()
    {
        try
        {
            return helper_response.getBytes("US-ASCII").length;
        }
        catch(UnsupportedEncodingException e)
        {
            return -1;
        }
    }
};





