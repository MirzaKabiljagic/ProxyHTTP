package rkn2018;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ProxyServer extends Thread{

    private InputStream fromServer;
    private OutputStream toClient;
    private boolean connected;
    private Proxy proxy_instance;

    public boolean transferPlugin = false;
    //empty constructor
    ProxyServer(){}

    //constructor for input and output stream
    ProxyServer(InputStream fromServer_, OutputStream toClient_, boolean connected_, Proxy proxy_instance_)
    {
        System.out.println("Calling constructor of server proxy. Input stream from server: " + fromServer_ + " Output stream to client: " + toClient_);
        this.fromServer = fromServer_;
        this.toClient = toClient_;
        this.connected = connected_;
        this.proxy_instance = proxy_instance_;
    }

    @Override
    public void run() {
        Parser parse = new Parser();
        byte[] replyBuffer = new byte[ProxyClient.SIZE_OF_BYTE];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int readBytes;

        //read data from server and parse
        try {
            while ((readBytes = fromServer.read(replyBuffer)) != -1) {
                outputStream.write(Arrays.copyOfRange(replyBuffer, 0, readBytes));

                if (connected) {
                    toClient.write(replyBuffer, 0, readBytes);
                    toClient.flush();
                    continue;
                }

                byte[] parseArray = outputStream.toByteArray();
                //start parsing
                parse.startParse(parseArray);

                //if whole data is parsed break
                if (parse.getParsed() == true) {


                    byte[] BodyReturn = parse.headerOrBodyReturn(outputStream.toByteArray(), false);
                    byte[] HeaderReturn = parse.headerOrBodyReturn(outputStream.toByteArray(), true);
                    //plugins
                    plugins(parse, BodyReturn, HeaderReturn);
                    if(!transferPlugin)
                       return;


                    if (ProxyClient.statusCheck(parse))
                        break;


                    parse = new Parser();
                    outputStream.close();
                    outputStream = new ByteArrayOutputStream();

                }

                //return if thread is interrupted
                if (isInterrupted())
                    return;
            }
        } catch (IOException e) {
            System.out.println("It is not possible to read data from server");
            e.printStackTrace();
        }

         try
        {
            toClient.write(outputStream.toByteArray(), 0, outputStream.size());
            toClient.flush();
        }

        catch (IOException e) {
            System.out.println("It is not possible to transfer data to client");
        }





    }
    //******************************************************************************************************************
    public void plugins(Parser parse_, byte[] BodyReturn_, byte[]HeaderReturn_)
    {
        //Plugin3
        PluginHelper helper = new PluginHelper(proxy_instance);

        //add cookies
        HeaderReturn_ = helper.addCookies(new String(HeaderReturn_)).getBytes();

        String contentTypeValue = parse_.valuesFromField().get(parse_.CONTENT_TYPE);
        if(contentTypeValue != null) {
            String[] parsedValues = contentTypeValue.split(";");

            //remove whitespaces
            int index = 0;
            for (String i : parsedValues) {
                parsedValues[index] = i.replaceAll("\\s+", "");
                index++;
            }

            boolean htmlExists = false;
            //check if exists text/html
            for (String i : parsedValues) {
                if (i.equals("text/html"))
                    htmlExists = true;
            }

            if (htmlExists) {

                try {
                    BodyReturn_ = replaceContent(parse_, BodyReturn_);
                    if (checkChunk(parse_)) {
                        String encodingValue = parse_.getEncoding();
                        HeaderReturn_ = helper.removeChunks(new String(HeaderReturn_)).getBytes(encodingValue);
                        HeaderReturn_ = helper.contentLengthSetting(new String(HeaderReturn_), BodyReturn_.length).getBytes();
                    } else {
                        HeaderReturn_ = helper.editContentLength(new String(HeaderReturn_), BodyReturn_.length).getBytes();
                    }

                } catch (IOException e) {
                    System.out.println("Can not replace content");
                    e.printStackTrace();
                }

            }
        }

        byte[] mergeHB = parse_.mergeHB(HeaderReturn_, BodyReturn_);
        try
        {
            toClient.write(mergeHB, 0, mergeHB.length);
            toClient.flush();

        }
        catch (IOException e) {

            System.out.println("Can not transfer data");
            e.printStackTrace();
            transferPlugin = false;

        }

        transferPlugin = true;

    }
    //******************************************************************************************************************
    public byte[] replaceContent(Parser parse__, byte[] replaceBody_)
    {

        String replacedContent = "";
        String encoding = parse__.getEncoding();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] helper_buffer = new byte[1024];
        int byteRead;
        HashMap<String, String> getContent = new HashMap<>(proxy_instance.getReplacements());

        if(checkChunk(parse__)) {
            try {
                replaceBody_ = parse__.dataFromChunk(replaceBody_);
            } catch (IOException e) {
                System.out.println("Can not read chunks");
                e.printStackTrace();
            }

        }

        if(parse__.checkGZIP())
        {
            //decompress gzip to html
            try {
                ByteArrayInputStream inputToGZP = new ByteArrayInputStream(replaceBody_);
                GZIPInputStream inputGZIP = new GZIPInputStream(inputToGZP);

                while ((byteRead = inputGZIP.read(helper_buffer)) != -1)
                {
                    outputStream.write(helper_buffer, 0, byteRead);

                }
                replacedContent =  outputStream.toString(encoding);
                inputGZIP.close();
                outputStream.close();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            replacedContent = new String(replaceBody_);
        }

        //then replace content
        for(Map.Entry<String, String> it : getContent.entrySet())
        {
            String temp = getContent.get(it.getKey());
            replacedContent = replacedContent.replaceAll(it.getKey(), temp);
        }

        if(parse__.checkGZIP())
        {
            try {
                ByteArrayOutputStream outputStream_new = new ByteArrayOutputStream();
                GZIPOutputStream outputGZIP = new GZIPOutputStream(outputStream_new);
                outputGZIP.write(replacedContent.getBytes(encoding));
                outputGZIP.close();

                return outputStream_new.toByteArray();
            }
            catch (IOException e)
            {
                System.out.println("Can not compress html to gzip");
                e.printStackTrace();
            }

        }
        else
        {
            try {
                return replacedContent.getBytes(encoding);
            }
            catch (IOException e)
            {
                System.out.println("Can not return replaced content");
                e.printStackTrace();
            }

        }
        //it will be never returned
        return  new byte[0];
    }
    //******************************************************************************************************************
    public boolean checkChunk(Parser parse_)
    {
        if(parse_.valuesFromField().get(parse_.TRANSFER_ENCODING) != null &&
                parse_.valuesFromField().get(parse_.TRANSFER_ENCODING).equals("chunked"))
        {
            return  true;
        }
        return  false;
    }




}