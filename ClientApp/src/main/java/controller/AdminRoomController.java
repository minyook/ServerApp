package controller;

import client.SocketClient;
import common.Message;
import common.RequestType;
import common.Room;
import view.AdminReservationFrame;

import java.util.List;

public class AdminRoomController {
    private final AdminReservationFrame view;

    public AdminRoomController(AdminReservationFrame view) {
        this.view = view;
        // 초기화 작업 가능
    }
    // 1. 전체 강의실 목록 요청
    public List<Room> getAllRooms() {
        try {
            Message req = new Message();
            req.setDomain("room");
            req.setType(RequestType.LIST);

            Message res = SocketClient.send(req);
            if (res.getError() == null) {
                @SuppressWarnings("unchecked")
                List<Room> list = (List<Room>) res.getList();
                return list;
            } else {
                System.out.println("강의실 조회 오류: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return List.of();  // 빈 목록 반환
    }

    // 2. 강의실 상태 업데이트 요청
    public boolean updateRoom(Room room) {
        try {
            Message req = new Message();
            req.setDomain("room");
            req.setType(RequestType.UPDATE);
            req.setPayload(room);  // Room 객체 전체 전송

            Message res = SocketClient.send(req);
            if (res.getError() == null) return true;
            else System.out.println("상태 변경 실패: " + res.getError());

        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return false;
    }
}
