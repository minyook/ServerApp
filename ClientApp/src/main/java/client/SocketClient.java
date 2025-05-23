package client;

import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private static final String SERVER_IP = "192.168.0.12";
    private static final int SERVER_PORT = 8080;

    public static Message send(Message req) throws Exception {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(req);          // 요청 전송
            out.flush();

            return (Message) in.readObject();  // 응답 수신
        }
    }
}
