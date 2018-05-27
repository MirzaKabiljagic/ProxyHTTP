package rkn2018;
//package test;


import java.io.IOException;
import java.net.ServerSocket;




public class ProxyConnection{

    private int portNumber = 8080;
    private ServerSocket serverS;
    private Proxy proxy_instance;

    //constructor which call startServer method
    ProxyConnection(Proxy proxy_)
    {
        this.proxy_instance = proxy_;

    }


    public void startServer()
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
