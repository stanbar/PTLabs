package client;

import javafx.concurrent.Task;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by stasbar on 13.03.2017.
 */
public class SendFileTask extends Task<Void> {
    Controller controller;
    private File file;

    public SendFileTask(Controller controller, File file) {
        this.controller = controller;
        updateTitle(file.getName());
        updateMessage("Waiting...");
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        updateTitle(file.getName());
        updateMessage("Processing...");


        try (Socket socket = new Socket(InetAddress.getLocalHost(), 1234);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {

            out.writeUTF(file.getName());

            int bufferSize = 1024 * 4;
            byte[] byteArray = new byte[bufferSize];
            int readLength = in.read(byteArray);
            long counter = 0;
            while (readLength > 0) {
                updateProgress(counter, file.length());
                counter += readLength;
                out.write(byteArray, 0, readLength);
                readLength = in.read(byteArray);

            }
        }

        updateProgress(file.length(), file.length());
        updateMessage("Done");
        return null;
    }

    public void setStatus(String status) {
        updateMessage(status);
    }
}
