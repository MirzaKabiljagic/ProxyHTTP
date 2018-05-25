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
        byte[] replyBuffer = new byte[ProxyClient.SIZE_OF_BYTE];

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

                //if whole data is parsed break
                if (parse.getParsed() == true)
                {
                    if(ProxyClient.statusCheck(parse))
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
        byte[] BodyReturn = parse.headerOrBodyReturn(outputStream.toByteArray(), false);
        byte[] HeaderReturn = parse.headerOrBodyReturn(outputStream.toByteArray(), true);
        if (!Proxy.jsInjectPath.isEmpty()) {
            ScriptInjector Injector = new ScriptInjector(Proxy.jsInjectPath);

            try {
                BodyReturn = Injector.toInject(BodyReturn, parse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] page = parse.mergeHB(HeaderReturn, BodyReturn);
        //write to client
        try
        {
            //toClient.write(outputStream.toByteArray(), 0, outputStream.size());
            toClient.write(page, 0, page.length);
            toClient.flush();
        }
        catch (IOException e)
        {
            System.out.println("It is not possible to transfer data to client");
        }
    }
}