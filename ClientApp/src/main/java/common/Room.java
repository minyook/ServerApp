package common;

public class Room {
    public enum Availability { OPEN, CLOSED }

    private String roomId;           // ex. "911"
    private Availability availability;
    private String closeReason;      // 막혔을 때 설명

    public Room(String roomId, Availability avail, String reason) {
        this.roomId = roomId;
        this.availability = avail;
        this.closeReason = reason;
    }

    // ✅ 추가된 단순 생성자
    public Room(String roomId) {
        this.roomId = roomId;
        this.availability = Availability.OPEN;
        this.closeReason = "";
    }

    // --- getters / setters ---
    public String getRoomId() { return roomId; }
    public Availability getAvailability() { return availability; }
    public void setAvailability(Availability a) { this.availability = a; }
    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String r) { this.closeReason = r; }
}
