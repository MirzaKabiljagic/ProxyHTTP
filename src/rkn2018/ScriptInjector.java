package rkn2018;
import org.jsoup.*;
import java.io.*;
import org.jsoup.nodes.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ScriptInjector {
    String scriptPath;

    public ScriptInjector(String Path) {
        this.scriptPath = Path;
    }
    byte[] toInject(byte[] body, Parser parser) throws IOException {
        //System.out.println("*****************************************************888888888888888888888888888888888888888888888");
        if(parser.chunkedCheck(body)){
            try {
                body = parser.dataFromChunk(body);
            } catch (IOException e) {
                throw new IOException("Merging Error" + e);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Corrupt chunk" + e);
            }
        }
        File scriptFile = new File(scriptPath);
        FileInputStream scriptStream;
        try {
            scriptStream = new FileInputStream(scriptFile);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Script not found!" + e);
        }
        byte[] scriptContent = new byte[(int) scriptFile.length()];
        if (scriptStream.read(scriptContent) == -1) {
                System.out.println("File could not be read!");
                return body;
        }
        String webPage = new String(body);
        scriptStream.close();
        String script = new String(scriptContent);
        System.out.println("Script Opened and ready to be injected!");
        Document page = Jsoup.parse(webPage);
        Element header = page.head();
        header.append("<script type=\"text/javascript\">"+ script +"</script>");
        String encoding = "UTF-8";//parser.valuesFromField().get(3);

        try {
            body = page.toString().getBytes(encoding);

        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingException("1111111111111111111111111111111111111111111111111111Encoding <" + encoding + "> not supported. " + e);
        }
        return body;
    }
}
