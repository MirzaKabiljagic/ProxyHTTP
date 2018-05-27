package rkn2018;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.Arrays;
import java.util.HashMap;


public class Parser {



    private enum situationOfChunked {
        CHUNK_PARSED,
        TRY_NEXT_CHUNK
    }

    private String method;
    private String helper_response;

    public static final int HOST = 1;
    public static final int CONNECTION = 2;
    public static final int CONTENT_ENCODING = 3;
    public static final int CONTENT_TYPE = 4;
    public static final int CONTENT_LENGTH = 5;
    public static final int TRANSFER_ENCODING = 6;
    public static int lengthOfChunk = 0;
    public static int indexOfLF = -1;


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
    //******************************************************************************************************************
    public boolean contentLenCheck(byte[] input){
        System.out.println("We are checking length of the content");

        if(valuesFromField().get(CONTENT_LENGTH) != null)
        {
            int content_len = Integer.parseInt(valuesFromField().get(CONTENT_LENGTH));
            if(content_len == input.length - headerSize())
            {

                System.out.println("We are finished with checking the header.");
                return true;
            }
        }
        return false;
    }
    //******************************************************************************************************************
    public boolean chunkedCheck(byte[] inputClient_){
        System.out.println("We are checking chunking here");

        if(valuesFromField().get(TRANSFER_ENCODING) != null && valuesFromField().get(TRANSFER_ENCODING).equals("chunked")){

            if(inputClient_[inputClient_.length-5] == 48 && inputClient_[inputClient_.length -4] == 13
                    && inputClient_[inputClient_.length-3] == 10 && inputClient_[inputClient_.length-2] == 13
                    && inputClient_[inputClient_.length-1] == 10)
                System.out.println("Finish with checking chunked");
            return true;
        }
        return false;
    }

    //******************************************************************************************************************
    public boolean checkEnd(byte[] inputClient_)
    {
        System.out.println("We are checking end of header");

        if(inputClient_[inputClient_.length - 1] == 10 &&
                inputClient_[inputClient_.length - 2] == 13 &&
                inputClient_[inputClient_.length - 3] == 10 &&
                inputClient_[inputClient_.length - 4] == 13)
        {
            System.out.println("We are finished with checking header.");
            return true;
        }
        return false;
    }
    //******************************************************************************************************************
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
                FileWriter.caller.inputData(input);

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

            FileWriter.caller.inputData("--------------------------------------------------------------------------------\n");

            if(contentLenCheck(inputClient)){
                parsed = true;
            }
            else if(chunkedCheck(inputClient)){
                parsed = true;
            }
            else if(checkEnd(inputClient)){
                parsed = true;
            }

            else
                parsed = false;
            System.out.println("Finishing with parsing!");
        }
    }
    //******************************************************************************************************************
    public HashMap<Integer, String>valuesFromField()
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
        hash_map.put(CONTENT_ENCODING, content_encoding);

        String content_type = getResponseValues("Content-Type");
        if(content_type != null)
            content_type.replaceAll("\\s+", "");
        hash_map.put(CONTENT_TYPE, content_type);

        String content_length = getResponseValues("Content-Length");
        if(content_length != null)
            content_length.replaceAll("\\s+", "");
        hash_map.put(CONTENT_LENGTH, content_length);


        String transfer_encoding = getResponseValues("Transfer-Encoding");
        if(transfer_encoding != null)
            transfer_encoding.replaceAll("\\s+", "");
        hash_map.put(TRANSFER_ENCODING, transfer_encoding);

        return hash_map;
    }
    //******************************************************************************************************************
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

    //******************************************************************************************************************
    public static byte[] dataFromChunk(byte[] chunks) throws IOException, NumberFormatException{

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        situationOfChunked situation;
        situation = situationOfChunked.CHUNK_PARSED;

        for(int i = 0; i < chunks.length; ++i) {
            if (situation == situationOfChunked.CHUNK_PARSED) {
                if (chunks[i] == 13 && chunks[i + 1] == 10) {
                    String chunk_string = new String(Arrays.copyOfRange(chunks, indexOfLF + 1, i));
                    lengthOfChunk = Integer.parseInt(chunk_string, 16);

                    if (lengthOfChunk == 0)
                        break;

                    outputStream.write(Arrays.copyOfRange(chunks, i + 2, i + 2 + lengthOfChunk));

                    indexOfLF = i + 1;
                    i += lengthOfChunk;

                    situation = situationOfChunked.TRY_NEXT_CHUNK;

                }
            }else if (situation == situationOfChunked.TRY_NEXT_CHUNK ) {
                if (chunks[i] == 13 && chunks[i + 1] == 10) {
                    indexOfLF = i + 1;
                    situation = situationOfChunked.CHUNK_PARSED;
                }
            }
        }

        return outputStream.toByteArray();
    }
    //******************************************************************************************************************
    public byte[] headerOrBodyReturn(byte[] input, boolean headerOrBody)
    {
        byte[] headerBody;
        int i;
        for(i = 0; i < input.length - 3; i++)
        {
            if(input[i + 3] == 10 &&
                    input[i + 2] == 13 &&
                    input[i + 1] == 10 &&
                    input[i] == 13)
            {
                //i+=3;
                i = input.length -3;
                break;
            }
        }

        //When headerOrBody = true then header return otherwise return body
        if(headerOrBody)
        {
            headerBody = Arrays.copyOfRange(input, 0, i + 1);
        }
        else
        {
            headerBody = Arrays.copyOfRange(input, i + 1, input.length);
        }

        return  headerBody;
    }
    //******************************************************************************************************************

    public  byte[] mergeHB(byte[] h, byte[] b) {
        byte[] holder = new byte[h.length + b.length];
        for (int i = 0; i < holder.length; ++i)
        {
            holder[i] = i < h.length ? h[i] : b[i - h.length];
        }
        return holder;
    }
    //******************************************************************************************************************
    public String GZIPDecompression(byte[] compressed, String encoding) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(compressed);
        GZIPInputStream gzip = new GZIPInputStream(stream);
        InputStreamReader inputRead = new InputStreamReader(gzip, encoding);
        BufferedReader bufferRead = new BufferedReader(inputRead);
        StringBuilder output = new StringBuilder();
        String line;
        try {
            while((line = bufferRead.readLine()) != null)
            {
                output.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stream.close();
        bufferRead.close();
        gzip.close();
        String decompressed = output.toString();
        return decompressed;
    }
    //******************************************************************************************************************
    public byte[] GZIPCompression(String webPage, String encoding) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        GZIPOutputStream GZIPOut = new GZIPOutputStream(stream);
        try {
            GZIPOut.write(webPage.getBytes(encoding));
        } catch (IOException e) {
            e.printStackTrace();
        }
        GZIPOut.close();
        byte[] compressed = stream.toByteArray();
        return compressed;
    }
    //******************************************************************************************************************
    public String getEncoding()
    {
        String encoding = valuesFromField().get(CONTENT_TYPE);
        if(encoding != null)
        {
            encoding = encoding.replaceAll("\\s+", "");
            String[] parsed = encoding.split(";");

            //find value of char-set
            for(int i = 0; i < parsed.length; i++)
            {
               String[] parsed_parsed = parsed[i].split("=");

               if(parsed_parsed.length == 2 && parsed_parsed[0].equalsIgnoreCase("charset"))
               {
                   return parsed_parsed[1];
               }
            }
        }


        return  "UTF-8";
    }

    //******************************************************************************************************************
    public boolean checkGZIP()
    {
        if(valuesFromField().get(CONTENT_ENCODING) != null &&
                valuesFromField().get(CONTENT_ENCODING).equalsIgnoreCase("gzip"))
        {
            return true;
        }
        return  false;
    }
};





