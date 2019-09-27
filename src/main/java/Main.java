import mathBot.MathBotServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);
        //create socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket socket = serverSocket.accept();
            new MathBotServer(socket).run();
        }
        catch (IOException ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
