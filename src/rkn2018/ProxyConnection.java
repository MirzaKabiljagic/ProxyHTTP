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


    public void startServer()
    throws IOException{

        /*
        int portNumber = 8080;
        String host = "127.0.0.1";

        ServerSocket SocketServer = new ServerSocket(portNumber);
        final byte[] requestBuffer = new byte[1024];
        byte[] responseBuffer = new byte[4096];

        while(true)
        {
            Socket client = null;
            Socket server = null;

            try {
                client = SocketServer.accept();

                final InputStream fromClient = client.getInputStream();
                final OutputStream toClient = client.getOutputStream();

                try {
                    server = new Socket(host,portNumber);
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

                new Thread() {
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
                }
                .start();

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
        }*/

        int portNumber = 8080;
        ServerSocket SocketServer = new ServerSocket(portNumber);
        System.out.println("Server started :)");
        try
        {
            while (true){
                new MultiThreadConnection(SocketServer.accept()).start();
            }

        }
        catch (IOException e)
        {
            System.err.println(e);
        }









    }



}
