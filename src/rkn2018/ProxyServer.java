package rkn2018;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyServer extends Thread{

    private InputStream fromServer;
    private OutputStream toClient;
    private static final int CONNECTION = 2;
    private boolean connected;
    //empty constructor
    ProxyServer(){}

    //constructor for input and output stream
    ProxyServer(InputStream fromServer_, OutputStream toClient_, boolean connected_)
    {
        System.out.println("Calling constructor of server proxy. Input stream from server: " + fromServer_ + " Output stream to client: " + toClient_);
        this.fromServer = fromServer_;
        this.toClient = toClient_;
        this.connected = connected_;
    }

    @Override
    public void run() {


        Parser parse = new Parser();
        byte[] replyBuffer = new byte[4096];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int readBytes;

        //read data from server and parse
        try {
            while((readBytes = fromServer.read(replyBuffer)) != -1)
            {
                outputStream.write(Arrays.copyOfRange(replyBuffer, 0, readBytes));

                   if(connected){
                    toClient.write(replyBuffer,0, readBytes);
                    toClient.flush();
                    continue;
                }


                byte[] parseArray = outputStream.toByteArray();
                //start parsing
                parse.startParse(parseArray);

                //TODO

                //if whole data is parsed break
                if (parse.getParsed() == true){

                    //write to client
                    try {
                        toClient.write(outputStream.toByteArray(), 0, outputStream.size());
                        toClient.flush();
                    }
                    catch (IOException e)
                    {
                        System.out.println("It is not possible to transfer data to client");
                    }



                    String konekcija = parse.valuesFromField().get(CONNECTION);
                    if(konekcija != null && !konekcija.equals("keep-alive"))
                        break;


                   parse = new Parser();
                   outputStream.close();
                   outputStream = new ByteArrayOutputStream();
                }

                //return if thread is interrupted
                if(isInterrupted())
                    return;

            }
        }
        catch (IOException e)
        {
            System.out.println("It is not possible to read data from server");
            e.printStackTrace();
        }




    }
}
