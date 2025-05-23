package client;

import view.LoginView;

import java.io.*;
import java.net.Socket;

public class ClientMain {
    public static Socket socket;
    public static ObjectOutputStream out;
    public static ObjectInputStream in;

    public static void main(String[] args) {
        try {
            socket = new Socket("192.168.0.12", 8080); // 서버 IP
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // 중요!
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("✅ 서버 연결 성공");

            javax.swing.SwingUtilities.invokeLater(() -> {
                new LoginView().setVisible(true);
            });

        } catch (Exception e) {
            System.err.println("❌ 서버 연결 실패:");
            e.printStackTrace();
        }
    }
}
