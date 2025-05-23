package controller;

import client.SocketClient;
import common.Message;
import common.RequestType;
import common.Reservation;
import view.AdminReservationFrame;

import java.util.List;

public class AdminReservationController {
     private final AdminReservationFrame view;

    public AdminReservationController(AdminReservationFrame view) {
        this.view = view;
        // 초기화 작업 가능
    }
    // 1. 전체 예약 목록 조회
    public List<Reservation> getAllReservations() {
        try {
            Message req = new Message();
            req.setDomain("admin");
            req.setType(RequestType.LIST);

            Message res = SocketClient.send(req);
            if (res.getError() == null) {
                @SuppressWarnings("unchecked")
                List<Reservation> list = (List<Reservation>) res.getList();
                return list;
            } else {
                System.out.println("조회 실패: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 요청 오류: " + e.getMessage());
        }
        return List.of();  // 빈 목록 반환
    }

    // 2. 예약 상태 변경 (예: 승인/거절)
    public boolean updateStatus(Reservation r) {
        try {
            Message req = new Message();
            req.setDomain("admin");
            req.setType(RequestType.UPDATE);
            req.setPayload(r);

            Message res = SocketClient.send(req);
            if (res.getError() == null) return true;
            else System.out.println("상태 변경 실패: " + res.getError());

        } catch (Exception e) {
            System.out.println("서버 요청 오류: " + e.getMessage());
        }
        return false;
    }
}
