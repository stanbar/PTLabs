package server;

import java.io.IOException;

public class ServerLauncher {

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.listenOn(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
