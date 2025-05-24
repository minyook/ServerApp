package model;

import common.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UserModel {
    private static final String DATA_FILE = "resources/user_data.txt";
    private final List<User> userList = new ArrayList<>();

    public UserModel() throws IOException {
        load();
    }

    private void load() throws IOException {
        userList.clear();
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) Files.createFile(path);

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] tokens = line.split(",");
            if (tokens.length >= 4) {  // ✅ 4개 항목 모두 불러옴
                userList.add(new User(tokens[0], tokens[1], tokens[2], tokens[3]));
            }
        }
    }

    public List<User> listAll() throws IOException {
        load();
        return new ArrayList<>(userList);
    }

    public void register(User user) throws IOException {
        userList.add(user);
        save();
    }

    public boolean login(String id, String pw) throws IOException {
        load();
        for (User u : userList) {
            if (u.getUsername().equals(id) && u.getPassword().equals(pw)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkDuplicateId(String id) throws IOException {
        load();
        for (User u : userList) {
            if (u.getUsername().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : userList) {
            lines.add(String.join(",", u.getUsername(), u.getPassword(), u.getRole(), u.getName()));
        }
        Files.write(Paths.get(DATA_FILE), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
