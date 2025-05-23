package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import common.*;

public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        initReservationData(); // 🔹 초기화 호출
    }

    private void initReservationData() {
        File storageFile = new File("storage/reservation_data.txt");
        if (storageFile.exists()) return;

        try (InputStream is = getClass().getResourceAsStream("/reservation_data.txt")) {
            if (is == null) {
                System.err.println("❌ resources에서 reservation_data.txt 파일을 찾을 수 없습니다.");
                return;
            }
            storageFile.getParentFile().mkdirs();
            Files.copy(is, storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ 예약 데이터 초기화 완료 (storage/reservation_data.txt)");
        } catch (IOException e) {
            System.err.println("❌ 예약 데이터 초기화 중 오류 발생:");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
    try {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        System.out.println("🔵 클라이언트 스트림 연결됨: " + socket.getInetAddress());

        while (true) {
            try {
                Message msg = (Message) in.readObject();
                System.out.println("✅ 수신된 메시지: " + msg.getType());

                Message response = new Message();
                response.setDomain(msg.getDomain());
                response.setType(msg.getType());

                if (msg.getType() == RequestType.LOGIN) {
                    User requestUser = (User) msg.getPayload();
                    User found = findUser(requestUser.getUsername(), requestUser.getPassword());

                    if (found != null) {
                        response.setPayload(found);
                        System.out.println("🔐 로그인 성공: " + found.getUsername());
                    } else {
                        response.setError("아이디 또는 비밀번호가 일치하지 않습니다.");
                        System.out.println("❌ 로그인 실패");
                    }

                } else if (msg.getType() == RequestType.REGISTER) {
                    User newUser = (User) msg.getPayload();

                    if (checkUserExists(newUser.getUsername())) {
                        response.setError("이미 존재하는 ID입니다.");
                    } else {
                        if (saveUser(newUser)) {
                            response.setPayload("회원가입 완료");
                            System.out.println("✅ 신규 회원 등록됨: " + newUser.getUsername());
                        } else {
                            response.setError("회원가입 저장 중 오류 발생");
                        }
                    }

                } else if (msg.getType() == RequestType.RESERVE) {
                    Reservation r = (Reservation) msg.getPayload();

                    if (isTimeSlotTaken(r)) {
                        response.setPayload("중복");
                    } else {
                        saveReservation(r);
                        response.setPayload("성공");
                        System.out.println("✅ 예약 저장됨: " + r.getUserName() + " - " + r.getDate() + " " + r.getTime());
                    }

                } else if (msg.getType() == RequestType.LOAD_TIMETABLE) {
                    Map<String, String> info = (Map<String, String>) msg.getPayload();
                    String day = info.get("day");
                    String room = info.get("room");

                    List<RoomStatus> statusList = loadTimeTable(day, room);

                    response.setDomain("timetable");
                    response.setType(RequestType.LOAD_TIMETABLE);
                    response.setPayload(statusList);

                } else if (msg.getType() == RequestType.LOAD_SCHEDULE_FILE) {
                    String roomNumber = (String) msg.getPayload();
                    List<String> scheduleLines = loadRoomSchedule(roomNumber);

                    response.setDomain("schedule");
                    response.setType(RequestType.LOAD_SCHEDULE_FILE);
                    response.setPayload(scheduleLines);

                } else if (msg.getType() == RequestType.LOAD_MY_RESERVATIONS) {
                    String username = (String) msg.getPayload(); // ✅ null 여부 로그 찍기
                    System.out.println("📥 예약 목록 요청 (ID): " + username);

                    List<Reservation> list = loadReservationsByUserId(username);
                    System.out.println("📤 예약 수: " + list.size());

                    response.setDomain("reservation");
                    response.setType(RequestType.LOAD_MY_RESERVATIONS);
                    response.setPayload(list);
                } else {
                    response.setError("지원하지 않는 요청입니다.");
                }

                out.writeObject(response);
                out.flush();
            } catch (EOFException | SocketException e) {
                System.out.println("⚠️ 클라이언트 연결 종료됨: " + socket.getInetAddress());
                break;
            } catch (Exception e) {
                System.err.println("❌ 클라이언트 처리 중 예외 발생: " + e.getMessage());
                e.printStackTrace();
                // 클라이언트 연결은 유지
            }
        }
    } catch (IOException e) {
        System.err.println("❌ 소켓 설정 중 오류: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("🔒 소켓 닫힘: " + socket.getInetAddress());
            }
        } catch (IOException ignored) {}
    }
}


    private User findUser(String id, String pw) {
        File file = new File("storage/user.txt");
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String fileId = parts[0].trim();
                    String filePw = parts[1].trim();
                    String roleCode = parts[2].trim();
                    String name = parts[3].trim();

                    if (fileId.equals(id) && filePw.equals(pw)) {
                        String role = switch (roleCode) {
                            case "s" -> "학생";
                            case "p" -> "교수";
                            case "a" -> "조교";
                            default -> "알 수 없음";
                        };
                        return new User(fileId, filePw, role, name);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean checkUserExists(String username) {
        File file = new File("storage/user.txt");
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].trim().equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean saveUser(User user) {
        File file = new File("storage/user.txt");
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.newLine();
                String line = String.format("%s,%s,%s,%s",
                        user.getUsername(),
                        user.getPassword(),
                        user.getRole(),
                        user.getName());
                writer.write(line);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTimeSlotTaken(Reservation r) {
        File file = new File("storage/reservation_data.txt");
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String date = parts[0];
                    String time = parts[1];
                    String room = parts[2];

                    if (date.equals(r.getDate()) && time.equals(r.getTime()) && room.equals(r.getRoomNumber())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void saveReservation(Reservation r) {
        File file = new File("storage/reservation_data.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            String line = String.format("%s,%s,%s,%s,%s",
                    r.getDate(), r.getTime(), r.getRoomNumber(), r.getUserName(), r.getStatus());
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<RoomStatus> loadTimeTable(String day, String room) {
        List<RoomStatus> result = new ArrayList<>();

        String[] timeSlots = {
            "09:00~09:50", "10:00~10:50", "11:00~11:50",
            "12:00~12:50", "13:00~13:50", "14:00~14:50",
            "15:00~15:50", "16:00~16:50"
        };

        Set<String> reservedSlots = new HashSet<>();
        File file = new File("storage/reservation_data.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5 && parts[0].equals(day) && parts[2].equals(room)) {
                        reservedSlots.add(parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String slot : timeSlots) {
            String status = reservedSlots.contains(slot) ? "예약됨" : "비어 있음";
            result.add(new RoomStatus(slot, status));
        }

        return result;
    }

    private List<String> loadRoomSchedule(String roomNumber) {
        List<String> scheduleList = new ArrayList<>();
        String fileName = "/schedule_" + roomNumber + ".txt";

        try (InputStream is = getClass().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                scheduleList.add(line);
            }

        } catch (IOException | NullPointerException e) {
            System.err.println("📛 시간표 파일 읽기 실패: " + fileName);
            e.printStackTrace();
        }

        return scheduleList;
    }
    private List<Reservation> loadReservationsByUserId(String userId) {
        List<Reservation> list = new ArrayList<>();
        String userName = getUserNameById(userId);  // 🔸 ID로 이름 조회
        if (userName == null) {
            System.err.println("❌ ID에 해당하는 이름을 찾을 수 없습니다: " + userId);
            return list;
        }

        File file = new File("storage/reservation_data.txt");

        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int id = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[3].trim().equals(userName.trim())) {
                    list.add(new Reservation(String.valueOf(id++), parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private String getUserNameById(String userId) {
        File file = new File("storage/user.txt");
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].trim().equals(userId.trim())) {
                    return parts[3].trim();  // ✅ 이름 반환
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}