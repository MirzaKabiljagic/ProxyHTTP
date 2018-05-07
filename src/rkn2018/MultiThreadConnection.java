package rkn2018;

import java.io.*;
import java.net.*;

public class MultiThreadConnection extends Thread {
    public Socket serverSocket;
    public int portNumber = 8080;

    MultiThreadConnection(Socket serverSocket_)
    {
        this.serverSocket = serverSocket_;
    }

    @Override
    public void run() {
        try{
            final byte[] requestBuffer = new byte[1024];
            byte[] responseBuffer = new byte[4096];

            final InputStream fromClient = serverSocket.getInputStream();
            final OutputStream toClient = serverSocket.getOutputStream();

            Socket serverS = null;

            try {
                serverS = new Socket("127.0.0.1", portNumber);
            }
            catch (IOException e)
            {
                PrintWriter output = new PrintWriter(new OutputStreamWriter(toClient));
                output.println("Connection can not be established");
                output.flush();
                throw new RuntimeException(e);

            }

            final InputStream fromServer = serverS.getInputStream();
            final OutputStream toServer = serverS.getOutputStream();

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
                    try{
                        toServer.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }.start();

            int read_Bytes;
            try{
                while((read_Bytes = fromServer.read(responseBuffer)) != -1){

                    toClient.write(responseBuffer, 0, read_Bytes);
                    toClient.flush();
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            finally {
                try{
                    if(serverS != null)
                        serverS.close();

                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
            toClient.close();
            serverSocket.close();


        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
