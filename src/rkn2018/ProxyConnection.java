package rkn2018;
//package test;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;




public class ProxyConnection extends Thread {

    private int portNumber = 8080;
    private String host = "127.0.0.1";
    private ServerSocket serverS;

    ProxyConnection()
    {
        try{
            startServer();
        }
        catch(IOException e)
        {
            System.out.print("Error occurred while trying to run the server \n");
            e.printStackTrace();
        }
    }


    public void startServer()
    throws IOException{
        System.out.println("Server is started");

        serverS = new ServerSocket(portNumber);
        final byte[] requestBuffer = new byte[1024];
        byte[] responseBuffer = new byte[4096];

        while(true)
        {
            Socket client = null;
            Socket server = null;

            try {

                client = serverS.accept();

                /*TODO part http parser*/
                //initialise connection


                Parser parse = new Parser();
                try{

                    parse.startParse(client);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                final InputStream fromClient = client.getInputStream();
                final OutputStream toClient = client.getOutputStream();

                try {
                    //host from http parser

                    server = new Socket(parse.getHost(), 80);
                }
                catch (IOException e)
                {
                    PrintWriter output = new PrintWriter(new OutputStreamWriter(toClient));
                    output.println("Connection can not be established");
                    output.flush();
                    client.close();
                    continue;
                }
                final InputStream fromServer = server.getInputStream();
                final OutputStream toServer = server.getOutputStream();

                /* http header parsed*/
                String headerHTTP = parse.getMethod() + " " + parse.getUrl() + " " +  parse.getVersion() + "\r\n"
                        + "Host: " + parse.getHost() + "\r\n"
                        + "Connection: keep-alive " + "\r\n"
                        + "\r\n";


                Thread thread = new Thread(){
                    public void run() {
                        int readBytes;
                        try{
                            while((readBytes = fromClient.read(requestBuffer)) != -1) {
                                toServer.write(requestBuffer, 0, readBytes);
                                System.out.println(readBytes + "na server prika --->" + new String(requestBuffer,"UTF-8") + "<----");
                                toServer.flush();
                            }
                        }
                        catch (IOException e) {}
                        try{toServer.close();} catch(IOException e){}
                    }
                };

                toServer.write(headerHTTP.getBytes());

                thread.start();
                int read_Bytes;
                try{
                        while((read_Bytes = fromServer.read(responseBuffer)) != -1){
                        try{
                            Thread.sleep(1);
                            System.out.println(read_Bytes+"To client ---->" + new String(requestBuffer,"UTF-8") + "<---");
                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                     toClient.write(responseBuffer, 0, read_Bytes);
                     toClient.flush();
                    }
                }
                catch(IOException e) {}
                toClient.close();

            }
            catch(IOException e) {
                System.err.println(e);
            }
            finally {
                try{
                    if(server != null) server.close();
                    if(client != null) client.close();
                }
                catch(IOException e){}
            }
        }


    }



}
