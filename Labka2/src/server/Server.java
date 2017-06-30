package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by stasbar on 10.03.2017.
 */
class Server {
    Logger logger = Logger.getLogger(Server.class.getCanonicalName());
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    public void listenOn(int port) throws IOException {

        try (ServerSocket serverSocker = new ServerSocket(port)) {
            logger.info(String.format("Listening on port %d", port));

            while (true) {
                Socket socket = serverSocker.accept();
                executorService.submit(() -> checkItOut(socket));
            }
        }

    }

    public void checkItOut(Socket socket) {
        logger.info("Handling socket: " + socket.getInetAddress().getHostAddress());

        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            String fileName = in.readUTF();
            logger.info("FileName: " + fileName);
            Path filePath = Paths.get(System.getProperty("user.home"), "outputLab2", fileName);
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            logger.info("File " + fileName + " is being processing !");
            try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(filePath))) {
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int readSize;

                while ((readSize = in.read(buffer)) != -1) { //Read from input stream (network socket)
                    out.write(buffer, 0, readSize); //Write to output stream (to file)
                }
                logger.info("File " + fileName + " successfully received !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
