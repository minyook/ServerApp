package client;

import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private static final String SERVER_IP = "192.168.45.12";
    private static final int SERVER_PORT = 8080;

    public static Message send(Message req) throws Exception {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(req);          // 요청 전송
            out.flush();
            System.out.println("📨 요청 전송 완료");
            
            Object raw = in.readObject();
            System.out.println("📩 응답 수신 완료");
            return (Message) raw;

        } catch (Exception e) {
        System.err.println("❌ SocketClient 예외 발생: " + e.getMessage());
        e.printStackTrace(); // 🔍 전체 예외 스택 트레이스
        throw e;  // 예외 다시 던짐
    }
    }
}
