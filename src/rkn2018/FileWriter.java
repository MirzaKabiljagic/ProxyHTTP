package rkn2018;

import java.io.*;

public class FileWriter {
    public static FileWriter caller= new FileWriter();
    private PrintWriter sender;

    private FileWriter() {
        try{
            File cr_file = new File(Proxy.dumpPath);
            //File cr_file = new File("cookies.txt");
            sender = new PrintWriter(cr_file);

        }catch (IOException e) {
            System.out.println("Catched cookies exception");
            e.printStackTrace();
        }
    }

    public void inputData(String str) {
        sender.println(str);
        sender.flush();
    }


}
