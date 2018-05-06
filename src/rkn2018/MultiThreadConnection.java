package rkn2018;

import java.io.*;
import java.net.*;

public class MultiThreadConnection extends Thread {
    public Socket websocket;


    MultiThreadConnection(Socket websocket_)
    {
        this.websocket = websocket_;
    }
}
