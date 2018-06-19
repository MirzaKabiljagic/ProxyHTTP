package rkn2018;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public class ProxyServer extends Thread{

    private InputStream fromServer;
    private OutputStream toClient;
    private boolean connected;
    private Proxy proxy_instance;

    public boolean transferPlugin = false;
    //empty constructor
    ProxyServer(){}
    private PluginHelper helper;

    //constructor for input and output stream
    ProxyServer(InputStream fromServer_, OutputStream toClient_, boolean connected_, Proxy proxy_instance_)
    {
        System.out.println("Calling constructor of server proxy. Input stream from server: " + fromServer_ + " Output stream to client: " + toClient_);
        this.fromServer = fromServer_;
        this.toClient = toClient_;
        this.connected = connected_;
        this.proxy_instance = proxy_instance_;
    }
    //******************************************************************************************************************
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
                parse.startParse(parseArray, "response");
                //if whole data is parsed break
                if (parse.getParsed() == true) {

                    byte[] BodyReturn = parse.headerOrBodyReturn(outputStream.toByteArray(), false);
                    byte[] HeaderReturn = parse.headerOrBodyReturn(outputStream.toByteArray(), true);
                    //plugins
                    if(!proxy_instance.getContentReplacements().isEmpty() ||
                            !proxy_instance.getJsInjectPath().isEmpty())
                    {
                        plugins(HeaderReturn , BodyReturn, parse);
                        if(!transferPlugin)
                            return;
                    }

                    if(!proxy_instance.headerReplacements.isEmpty() || proxy_instance.sopSwitch)
                    {
                        plugins(HeaderReturn,BodyReturn,parse);
                        if(!transferPlugin)
                            return;
                    }

                    else
                    {
                        try
                        {
                            toClient.write(outputStream.toByteArray(), 0, outputStream.toByteArray().length);
                            toClient.flush();
                        }

                        catch (IOException e) {
                            System.out.println("It is not possible to transfer data to client");
                        }
                    }

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


    }
    //******************************************************************************************************************

    public void plugins(byte[] HeaderReturn,byte[] BodyReturn, Parser parser) {
        try {
            PluginHelper helper = new PluginHelper(proxy_instance);
            String encodingValue = parser.getEncoding();

            HeaderReturn = helper.addCookies(new String(HeaderReturn)).getBytes(encodingValue);
            //System.out.println("-----------------------------------"+parser.getHeader_response()+"-----------------------------------------------");
            String content_type = parser.getResponseValues("Content-Type");
            if(content_type.contains("text/html"))
            {
                content_type.replaceAll("\\s+", "");

                String[] parsedValues = content_type.split(";");

                //remove whitespaces
                int index = 0;
                for (String i : parsedValues) {
                    parsedValues[index] = i.replaceAll("\\s+", "");
                    index++;
                }

                boolean htmlExists = true;
                //check if exists text/html
                for (String i : parsedValues) {
                    if (i.contains("text/html"))
                        htmlExists = true;
                }

                if (htmlExists) {
                    try {
                        if(!proxy_instance.getJsInjectPath().isEmpty()) {
                            BodyReturn = injectJS(parser, BodyReturn);
                        }
                        if(!proxy_instance.getContentReplacements().isEmpty()) {
                            BodyReturn = replaceContent(parser, BodyReturn);
                        }


                        if(checkChunk(parser)) {
                            HeaderReturn = helper.contentLengthSetting(new String(HeaderReturn), BodyReturn.length, encodingValue).getBytes(encodingValue);
                            if(!proxy_instance.getHeaderReplacements().isEmpty()){
                                HeaderReturn = helper.headerModifier(HeaderReturn,proxy_instance.getHeaderReplacements(), 0);
                            }
                            if(proxy_instance.sopSwitch){
                                HeaderReturn = helper.sop(HeaderReturn);
                            }
                        }
                        else {
                            HeaderReturn = helper.editContentLength(new String(HeaderReturn), BodyReturn.length).getBytes(encodingValue);
                            if(!proxy_instance.getHeaderReplacements().isEmpty()){
                                HeaderReturn = helper.headerModifier(HeaderReturn,proxy_instance.getHeaderReplacements(), 0);
                            }
                            if(proxy_instance.sopSwitch){
                                HeaderReturn = helper.sop(HeaderReturn);
                            }
                        }

                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }

            byte[] mergeHB = parser.mergeHB(HeaderReturn, BodyReturn);
            toClient.write(mergeHB, 0, mergeHB.length);
            toClient.flush();

        } catch (IOException e) {
            System.out.println("Can not transfer data");
            e.printStackTrace();

            transferPlugin = false;
        }

        transferPlugin = true;
    }
    //******************************************************************************************************************

    public byte[] injectJS(Parser parse__, byte[] replaceBody_) {

       String content_type = parse__.getResponseValues("Content-Type");
        if (content_type.contains("text/html")){
            //System.out.println("-----------------------------------" + proxy_instance.getJsInjectPath() + "--------------------------" + parse__.getHeader_response() + "---------------------");

        String encoding = parse__.getEncoding();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] helper_buffer = new byte[1024];
        int byteRead;
        String toInjectTo = new String();
        if (checkChunk(parse__)) {
            try {
                replaceBody_ = parse__.dataFromChunk(replaceBody_);
            } catch (IOException e) {
                System.out.println("Can not read chunks");
                e.printStackTrace();
            }

        }

        if (parse__.checkGZIP()) {

            try {

                ByteArrayInputStream inputToGZP = new ByteArrayInputStream(replaceBody_);
                GZIPInputStream inputGZIP = new GZIPInputStream(inputToGZP);
                while ((byteRead = inputGZIP.read(helper_buffer)) != -1) {
                    outputStream.write(helper_buffer, 0, byteRead);

                }

                toInjectTo = outputStream.toString(encoding);
                inputGZIP.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //System.out.println("=================================================================================");
            toInjectTo = new String(replaceBody_);
            //return null;
        }
        //System.out.println("55555555555555555555555555555555555"+toInjectTo+"-----------------------------------------------");
        String jsPath = proxy_instance.getJsInjectPath();
        File file = new File(jsPath);
        FileInputStream jsFileInputStream;
        byte[] fileBytes = new byte[(int) file.length()];
        ;
        try {
            jsFileInputStream = new FileInputStream(file);
            if (jsFileInputStream.read(fileBytes) == -1) {
                System.out.println("Invalid JavaScript file!");
                //System.out.println("444444444444444444444444444444444444444444444444444444444444444444444444444444");
                return toInjectTo.getBytes();
            }
            jsFileInputStream.close();
        } catch (Exception e) {
            System.out.println("Error while opening the JavaScript file!" + e);
        }
        String script = new String(fileBytes);
        Document doc = Jsoup.parse(toInjectTo);
        Element head = doc.head();
        head.append("<script type=\"text/javascript\">" + script + "</script>");
        if (parse__.checkGZIP()) {
            try {

                ByteArrayOutputStream output_arr = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutStream_ = new GZIPOutputStream(output_arr);
                gzipOutStream_.write(doc.toString().getBytes(encoding));
                gzipOutStream_.close();

                return output_arr.toByteArray();
            } catch (IOException e) {
                System.out.println("Can not compress html to gzip");
                e.printStackTrace();
            }

        } else {
            try {
                //System.out.println("111111111111111111111111111111111111111111111111111111111111111");
                return toInjectTo.getBytes(encoding);
            } catch (IOException e) {
                System.out.println("Can not return replaced content");
                e.printStackTrace();
            }


        }
    }
        //System.out.println("33333333333333333333333333333333333333333333333333333333333333333333333");
            return new byte[0];

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
        //System.out.println("1111111111111111111111111111111111111111"+getContent+"111111111111111111111111111111111111111111111111");
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
                GZIPInputStream inputGZIP = new GZIPInputStream(new ByteArrayInputStream(replaceBody_));

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
