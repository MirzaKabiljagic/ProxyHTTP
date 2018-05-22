package rkn2018;

import java.io.*;
import java.util.HashMap;


public class Parser {
    private String url;
    private String method;
    private String version;
    private String host;
    private String helper_response;
    private static final int CONTENT_LENGHT = 5;



    private boolean parsed = false;
    private boolean isParsed = false;

    private HashMap<String, String> header_response;

    //constructor initialise helper hashmap and string
    Parser() {

        header_response = new HashMap<>();
        helper_response = "";
    }


    //getters
    public String getUrl()
    {
        return  url;
    }

    public String getMethod() { return method; }

    public String getVersion()
    {
        return version;
    }

    public String getHost() {
        return host;
    }

    //check if data is parsed
    public boolean getParsed() { return parsed; }

    public boolean isParsed(){ return isParsed; }

    //get parsed values
    public String getResponseValues(String name)
    {
        return header_response.get(name);
    }


    public void checkEnd(byte[] inputClient_)
    {
        if(valuesFromField().get(CONTENT_LENGHT) != null){
            int content_len = Integer.parseInt(valuesFromField().get(CONTENT_LENGHT));
            if(content_len == inputClient_.length - headerSize()){
                parsed = true;
            }
        }
        else if(inputClient_[inputClient_.length - 1] == 10 &&
                inputClient_[inputClient_.length - 2] == 13 &&
                inputClient_[inputClient_.length - 3] == 10 &&
                inputClient_[inputClient_.length - 4] == 13)
        {
            parsed = true;
        }
        else
        {
            parsed = false;
        }
    }

    public void startParse(byte[] inputClient)
            throws IOException
    {
        //check whether data is already parsed
        if(!isParsed())
        {
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inputClient)));
            String input;
            String newline = "\r\n";
            int count = 0;
            String[] arrayParser;


            while((input = inputBuffer.readLine()) != null && !input.isEmpty()) {
                helper_response += input + newline;

                if(count == 0)
                {
                    arrayParser = input.split(" ");

                    if(arrayParser[0].equals("GET"))
                        method = "GET";

                    if(arrayParser[0].equals("CONNECT"))
                        method = "CONNECT";
                  //  System.out.println("Hocemo da isprintamo array" + arrayParser[0].toString());


                    //System.out.println(method);
                    //System.out.println(version);
                    //System.out.println(url);
                }




                else
                {
                    arrayParser = input.split(": ");
                    if(arrayParser.length == 2)
                        header_response.put(arrayParser[0], arrayParser[1]);
                    else
                        System.out.println("Size of arrayParser bigger than 2 and is: " + arrayParser.length);
                    //System.out.println(host);
                }


                count ++;

            }
            helper_response += newline;
            isParsed = true;

            checkEnd(inputClient);
        }
    }
    public  HashMap<Integer, String>valuesFromField()
    {
        HashMap<Integer, String> hash_map = new HashMap<>();
        String host = getResponseValues("Host");
        if(host != null)
            host.replaceAll("\\s+", "");
        hash_map.put(1, host);

        String connection = getResponseValues("Connection");
        if(connection != null)

             connection.replaceAll("\\s+", "");
        System.out.println("da znamo gdje je"+connection);
        hash_map.put(2, connection);

        String content_encoding = getResponseValues("Content-Encoding");
        if(content_encoding != null)
            content_encoding.replaceAll("\\s+", "");
        hash_map.put(3, connection);

        String content_type = getResponseValues("Content-Type");
        if(content_type != null)
            content_type.replaceAll("\\s+", "");
        hash_map.put(4, content_type);

        String content_length = getResponseValues("Content-Length");
        if(content_length != null)
            content_length.replaceAll("\\s+", "");
        hash_map.put(5, content_length);


        return hash_map;
    }

    int headerSize(){
        try{
            return helper_response.getBytes("US-ASCII").length;
        }catch(UnsupportedEncodingException e){
            return -1;
        }
    }



};





