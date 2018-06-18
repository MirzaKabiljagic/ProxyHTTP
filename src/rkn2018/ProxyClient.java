package rkn2018;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


class ProxyClient extends Thread {

    private ProxyServer server;

    private InputStream fromClient;
    private OutputStream toClient;
    private InputStream fromServer;
    private OutputStream toServer;
    public static final int SIZE_OF_BYTE = 4096;
    private static final int TIMEOUT = 3000;
    private static final int OFF_TRIGGER = 0;
    private Socket SocketClient;
    private Socket SocketServer;

    private boolean connected = false;
    private boolean serverConnection = false;

    private Proxy proxy_instance;
    //constructor with socket of client
    ProxyClient(Socket SocketClient_,  Proxy proxy_instance_) {

        this.proxy_instance = proxy_instance_;
        this.SocketClient = SocketClient_;
    }

    //******************************************************************************************************************
    //timeout for sockets
    public void setTimeOut(Socket timeout_) {
        try {
            timeout_.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //******************************************************************************************************************
    //Client and server socket closing
    public void closeConnection() {
        try {
            if (server != null) {
                server.join();
            }
            if (SocketClient != null) {
                SocketClient.close();
            }
            if (SocketServer != null) {
                SocketServer.close();
            }

        } catch (IOException e) {
            System.out.println("Not possible to close client and socket connection! ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    //******************************************************************************************************************
    @Override
    public void run() {

        setTimeOut(SocketClient);

        try
        {
            fromClient = SocketClient.getInputStream();
            toClient = SocketClient.getOutputStream();
        }
        catch (IOException e)
        {
            System.out.println("Not possible to streams from the client");
            closeConnection();
            return;
        }

        Parser parse = new Parser();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] requestBuffer = new byte[SIZE_OF_BYTE];
        int readBytes;
        try {
            while ((readBytes = fromClient.read(requestBuffer)) != -1) {
                outputStream.write(Arrays.copyOfRange(requestBuffer, 0, readBytes));


                if (connected) {
                    connectionCheck(toServer,readBytes,requestBuffer);
                    continue;
                }
                byte[] parseArray;
                parseArray = outputStream.toByteArray();
                parse.startParse(parseArray, "request");

                //flag checking for parsing
                if (parse.getParsed() == true) {
                    if (serverConnection == false) {

                        try {
                            //host for server socket
                            HashMap<Integer, String> map = new HashMap<>();
                            map = parse.valuesFromField();

                            String host = map.get(Parser.HOST);
                            if (host == null)
                            {
                                System.out.println("Host equals null");
                                closeConnection();
                                return;
                            }

                            if (parse.getMethod().equals("CONNECT")) {
                                System.out.println(host);
                                //String new_host = host.split(":")[0];
                                System.out.println("Host https is: " + host);
                                SocketServer = new Socket(host, 443);
                                setTimeOut(SocketServer);
                                //output response
                                DataOutputStream out = new DataOutputStream(toClient);
                                out.writeBytes("HTTP/1.1 200 OK\r\n\r\n");
                                out.flush();
                                connected = true;

                            }
                            else
                            {
                                //String new_host = host.split(":")[0];
                                System.out.println("Host http is: " + host);
                                if(parse.isSetNewPort())
                                    SocketServer = new Socket(host, parse.getNew_port());

                                else

                                    SocketServer = new Socket(host, 80);


                                setTimeOut(SocketServer);

                            }

                        } catch (IOException e) {
                            System.out.println("Connection can't be estabilished to host");
                            closeConnection();
                            return;
                        }
                        try
                        {
                            System.out.println("Input/output stream! Output: " +
                                    SocketServer.getOutputStream() + " Input: " + SocketServer.getInputStream());

                            toServer = SocketServer.getOutputStream();
                            fromServer = SocketServer.getInputStream();

                        }
                        catch (IOException e)
                        {
                            System.out.println("Can't reach data from server");
                            closeConnection();
                            return;
                        }
                        System.out.println("Init thread and start");
                        server = new ProxyServer(fromServer, toClient, connected, proxy_instance);
                        server.start();
                    }
                    serverConnection = true;
                    if (connected) {
                        continue;
                    }

                    String encoding = parse.getEncoding();
                    encoding = encoding == null ? "UTF-8" : encoding;

                    HashMap<Integer, String> map = new HashMap<>();
                    map = parse.valuesFromField();

                    String host = map.get(Parser.HOST);

                    byte[] requestingHeader = parse.headerOrBodyReturn(outputStream.toByteArray(), true);

                    PluginHelper help  = new PluginHelper(proxy_instance);

                    /*if(!proxy_instance.headerReplacements.isEmpty())
                    {
                        requestingHeader = help.replaceHeader(requestingHeader, proxy_instance.getHeaderReplacements(), 1);
                    }*/

                    if(!proxy_instance.redirections.isEmpty())
                    {
                        for (Map.Entry<String, String> entry : proxy_instance.redirections.entrySet()) {

                            if(entry.getKey().contains(host))
                            {
                                requestingHeader = help.redirect(entry.getKey(), entry.getValue(),requestingHeader);
                            }
                        }
                    }


                    byte[] clientRequestBody = parse.headerOrBodyReturn(outputStream.toByteArray(), false);

                    byte[] fusedContent = parse.mergeHB(requestingHeader,clientRequestBody);

                    try {
                        System.out.println("Write and flush");
                        toServer.write(fusedContent, OFF_TRIGGER,fusedContent.length);
                        toServer.flush();
                    } catch (IOException e) {
                        System.out.println("Can't reach data from server!");
                        server.interrupt();
                        closeConnection();
                        return;
                    }
                    if (statusCheck(parse) == true )
                        break;

                    parse = new Parser();
                    outputStream.close();
                    outputStream = new ByteArrayOutputStream();
                }
             }

        }
        catch(IOException e)
        {
            System.out.println("Not possible to streams from the client");
            closeConnection();
            return;
        }
        closeConnection();
    }
    //******************************************************************************************************************
    public static void connectionCheck(OutputStream toServer_,int readBytes_, byte[] requestBuffer_  ){
        try
        {
            toServer_.write(requestBuffer_, 0, readBytes_);
            toServer_.flush();
        }
        catch(IOException e)
        {
            System.out.println("Can't write data to server");
            e.printStackTrace();
        }   
    }
    //******************************************************************************************************************
    public static boolean statusCheck(Parser parse_){
        String konekcija = parse_.valuesFromField().get(Parser.CONNECTION);
        if (konekcija != null && !konekcija.equals("keep-alive"))
            return true;
        else
            return false;
    }
}