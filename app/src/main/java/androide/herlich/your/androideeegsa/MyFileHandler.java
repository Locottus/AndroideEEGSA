package androide.herlich.your.androideeegsa;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFileHandler {

    private static final String FILENAME = "Plot";
    private static final String DNAME = "Geografia";
    FileOutputStream fos;

    public static void readFile() {
        try {
            System.out.println("entering readfile function");
            RandomAccessFile file = new RandomAccessFile("c:\\android\\data.txt", "r");
            FileChannel channel = file.getChannel();
            System.out.println("File size is: " + channel.size());
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();//Restore buffer to position 0 to read it
            System.out.println("Reading content and printing ... ");
            for (int i = 0; i < channel.size(); i++) {
                System.out.print((char) buffer.get());
            }
            channel.close();
            file.close();
        } catch (Exception ex) {
        }
    }

    public String currentDateFileName() {
        DateFormat df = new SimpleDateFormat("dd MMM, yyyy");
        String now = df.format(new Date());
        now = FILENAME + now.replace(" ", "") + ".csv";
        return now;
    }

    public File creaDirectorio() {
        File rootPath = new File(Environment.getExternalStorageDirectory(), DNAME);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        return rootPath;
    }

    public void readFileLineByLine() {
        try {
            File f = new File("c:\\android\\data.txt");
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            System.out.println("Reading file using Buffered Reader");
            while ((readLine = b.readLine()) != null) {
                System.out.println(readLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void escribeArchivo(String data) {
        File rootPath = creaDirectorio();
        File dataFile = new File(rootPath, currentDateFileName());
        try {
            FileOutputStream mOutput = new FileOutputStream(dataFile, true);
            mOutput.write(data.getBytes());
            mOutput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
