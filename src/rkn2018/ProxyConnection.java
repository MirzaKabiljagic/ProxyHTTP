package rkn2018;
//package test;


import java.io.IOException;
import java.net.ServerSocket;




public class ProxyConnection{

    private int portNumber = 8080;
    private String host = "127.0.0.1";
    private ServerSocket serverS;
    private Proxy proxy_instance;

    //constructor which call startServer method
    ProxyConnection(Proxy proxy_)
    {
        this.proxy_instance = proxy_;
        try{
            startServer();
        }
        catch(IOException e)
        {
            System.out.print("It is not possible to run server \n");
            e.printStackTrace();
        }
    }


    private void startServer()
    throws IOException{
        System.out.println("Server is started");
        serverS = new ServerSocket(portNumber);

        while(true)
        {
            ProxyClient clientConnection = new ProxyClient(serverS.accept(), proxy_instance);
            clientConnection.start();
        }

    }
}
