package rkn2018;
//package test;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;




public class ServerConnection {


    public void startServer()
    throws IOException{

        int portNumber = 8080;
        System.out.println("Creating server socket on port " + portNumber);
        try(
                ServerSocket webSocket = new ServerSocket(portNumber);
                Socket clientSocket = webSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

               
            ){
            /*String inputLine, outputLine;

             //Initiate conversation with client
             HttpParser kkp = new HttpParser();
             outputLine = kkp.processInput(null);
             out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Quit"))
                    break;
            }*/
            while(true)
            {

                 (new ClientConnection(clientSocket)).start();
            }
        }
        catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }




    }





}
