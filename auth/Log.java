package auth;

import java.io.File;

//public abstract class Log {
//
////    private File output;
//
////    public Log(File outputFile) {
////        output = outputFile;
////    }
//
//    public abstract void addToLog(String data);
//
//}



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private SimpleDateFormat formatter;
    private Date date;

    private BufferedWriter bufferedWriter;

    public Log(String filename) {
        try {
            File file = new File(filename);
            FileWriter output = new FileWriter(file);
            bufferedWriter = new BufferedWriter(output);
            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            date = new Date();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToLog(String data) {
        try {
            String content = formatter.format(date) + " " + data;
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}