package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCP/IP Server
 * - 0.0.0.0 에 바인딩하여 모든 네트워크 인터페이스에서 수신
 * - 클라이언트 연결 시마다 ClientHandler 스레드 생성
 */
public class Server {
    public static final int PORT = 8080;  // 원하는 포트 번호

    public static void main(String[] args) {
        System.out.println(">>> 서버 시작: 포트 " + PORT);
        // 0.0.0.0에 바인딩하면 로컬, LAN, WAN 모든 인터페이스에서 수신 가능
        try (ServerSocket serverSocket = new ServerSocket(
                PORT,            // 포트
                50,              // backlog 큐 크기
                InetAddress.getByName("192.168.0.12")  // 모든 인터페이스
        )) {
            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 접속: " + clientSocket.getInetAddress());

                // 연결된 클라이언트마다 별도 스레드로 처리
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
