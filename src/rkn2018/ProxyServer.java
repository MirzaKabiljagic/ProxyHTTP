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

    //empty constructor
    ProxyServer(){}

    //constructor for input and output stream
    ProxyServer(InputStream fromServer_, OutputStream toClient_)
    {
        System.out.println("Calling constructor of server proxy. Input stream from server: " + fromServer_ + " Output stream to client: " + toClient_);
        this.fromServer = fromServer_;
        this.toClient = toClient_;
    }

    @Override
    public void run() {


        Parser parse = new Parser();
        byte[] replyBuffer = new byte[4096];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int readBytes;

        //read data from server and parse
        try {
            //System.out.println("from server" + fromServer.read(replyBuffer));
            while((readBytes = fromServer.read(replyBuffer)) != -1)
            {
                byte[] readByte = Arrays.copyOfRange(replyBuffer, 0, readBytes);
                outputStream.write(readByte);


                byte[] parseArray = outputStream.toByteArray();
                //start parsing
                parse.startParse(parseArray);

                //if whole data is parsed break
                if (parse.getParsed() == true){
                    String konekcija = parse.valuesFromField().get(2);
                    if(konekcija != null && !konekcija.equals("keep-alive"))
                        break;
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

        //write to client
        try {
            toClient.write(outputStream.toByteArray(), 0, outputStream.size());
            toClient.flush();
        }
        catch (IOException e)
        {
            System.out.println("It is not possible to transfer data to client");
        }



    }
}
