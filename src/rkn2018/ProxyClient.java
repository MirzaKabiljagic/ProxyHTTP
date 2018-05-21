package rkn2018;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;


class ProxyClient extends Thread {

    private ProxyServer server;

    private InputStream fromClient;
    private OutputStream toClient;
    private  InputStream fromServer;
    private OutputStream toServer;

    private Socket SocketClient;
    private Socket SocketServer;

   // private boolean serverConnected = false;
    private boolean connected = false;

    //empty constructor
    ProxyClient(){}

    //constructor with client Socket
    ProxyClient(Socket SocketClient_)
    {

        SocketClient = SocketClient_;
    }

    //getter
 //   public boolean getConnected(){ return connected; }

    //timeout for sockets
    public void setTimeOut(Socket timeout_)
    {
        try {
            timeout_.setSoTimeout(3000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    //close client and server sockets
    public void closeConnection()
    {
        try{
           /* if(server != null)
            {
                server.join();

            }*/
            if(SocketClient != null)
            {
                SocketClient.close();
                //SocketClient = null;
            }
            if(SocketServer != null)
            {
                SocketServer.close();
                //SocketServer = null;
            }

        }catch (IOException e)
        {
            System.out.println("It is not possible to close client and server sockets");
            e.printStackTrace();
        }

    }

    //client thread
    @Override
    public void run() {

        setTimeOut(SocketClient);

        // get the streams
        try {
            fromClient = SocketClient.getInputStream();
            toClient = SocketClient.getOutputStream();
        } catch (IOException e) {
            System.out.println("It is not possible to get input and outputstream from the client");
            closeConnection();
            return;
        }

        Parser parse = new Parser();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] requestBuffer = new byte[1024];
        int readBytes;

        //read request from client and parse
        try
        {
            while ((readBytes = fromClient.read(requestBuffer)) != -1) {
                outputStream.write(Arrays.copyOfRange(requestBuffer, 0, readBytes));

                //check if we already connected to the server
                if(connected)
                {
                    toServer.write(requestBuffer, 0, readBytes);
                    toServer.flush();
                    continue;
                }

                byte[] parseArray = outputStream.toByteArray();
                //Start parsing
                parse.startParse(parseArray);

                //break when whole request is parsed
                if (parse.getParsed() == true)
                    break;
            }
        }
        catch (IOException e)
        {
            System.out.println("It is not possible to read data from the client");
            //closeConnection();
        }


        try
        {
            //host for server socket
            HashMap<Integer, String> map = new HashMap<>();
            map = parse.valuesFromField();

            String host = map.get(1);
            if(host == null)
            {
                System.out.println("Host is null");
                closeConnection();
                return;
            }

            //HTTP
            if(parse.getMethod().equals("GET"))
            {

                //create socket server with host and default port 80
                System.out.println("Host http is: " +  host.split(":")[0]);
                SocketServer = new Socket(host, 80);
                setTimeOut(SocketServer);
                connected = true;
          //      serverConnected = true;
            }

            //HTTPS
            if(parse.getMethod().equals("CONNECT"))
            {
                System.out.println(host);
                String new_host = host.split(":")[0];
                System.out.println("Host https is: " +  new_host);

                SocketServer = new Socket(new_host, 443);
                DataOutputStream out = new DataOutputStream(toClient);
                out.writeBytes("HTTP/1.1 200 OK\r\n\r\n");
                out.flush();
                connected = true;
           //     serverConnected = true;
            }
            //TODO: i am not sure but stop or something else???
            else
            {
                //System.out.println("Making default server port 80");
                //TODO: maybe
                //SocketServer = new Socket( host.split(":")[0], 80);

            }

        }
        catch (IOException e)
        {
            System.out.println("Can not connect to certain host");
            closeConnection();
       //     serverConnected = false;
        }


        //input and output stream of server created with server socket
        try
        {
            System.out.println("Input/output stream! Output: " + SocketServer.getOutputStream() + " Input: " + SocketServer.getInputStream());
            toServer = SocketServer.getOutputStream();
            fromServer = SocketServer.getInputStream();

        }
        catch (IOException e)
        {
            System.out.println("It is not possible to get data from server");
            closeConnection();
            return;
        }

        try
        {
            System.out.println("Write and flush");
            toServer.write(outputStream.toByteArray(), 0, outputStream.toByteArray().length);
            toServer.flush();
        }
        catch (IOException e)
        {
            System.out.println("It is not possible to send data to server");
            server.interrupt();
            closeConnection();
            return;
        }

        //initialise server thread and start
        System.out.println("Initialise server thread and start");
        server = new ProxyServer(fromServer, toClient);
        server.start();


        //join thread
        try
        {
            if(server != null)
            {
                System.out.println("Start joining");
                server.join();
                System.out.println("Finish Joining!");
            }

            System.out.println("Closing connection!");
            closeConnection();
            return;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return;
        }


        /*if(connected)
            continue;*/



        //merge header and body
        /*byte[] mergedHeaderBody = createForServer(outputStream.toByteArray());

        try
        {
            toServer.write(mergedHeaderBody, 0, mergedHeaderBody.length);
            toServer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            server.interrupt();
            closeConnection();
            return;
        }*/
        //write to server


       /* parse = new Parser();
        outputStream.close();
        outputStream = new ByteArrayOutputStream();*/

        //closeConnection();
    }

   /* public byte[] createForServer(byte[] fromClientRequest)
    {
        byte[] forServerMerged = new byte[fromClientRequest.length *2];

        int i;
        for(i = 0; i < fromClientRequest.length - 3; i++)
        {
            if(fromClientRequest[i + 3] == 10 && fromClientRequest[i + 2] == 13 &&
                    fromClientRequest[i + 1] == 10 && fromClientRequest[i] == 13)
            {
                i+=3;
                break;
            }
        }
        byte[] header = Arrays.copyOfRange(fromClientRequest, 0, i + 1);
        byte[] body = Arrays.copyOfRange(fromClientRequest, i + 1, fromClientRequest.length);


        //for (int j = 0; j < forServerMerged.length; ++j)
        //{
          //  forServerMerged[j] = j < header.length ? header[j] : body[j - header.length];
        //}

        System.arraycopy(header, 0, forServerMerged, 0, header.length);
        System.arraycopy(body, 0, forServerMerged, header.length, body.length);
        return forServerMerged;
    }*/


}

