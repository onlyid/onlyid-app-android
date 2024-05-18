package net.onlyid.entity;

import java.time.LocalDateTime;

public class Entity3 {
    public enum Type {
        android, apple
    }

    public String deviceId;
    public Type type;
    public String name;
    public String customName;
    public LocalDateTime lastDate;
    public String sessionId;
}
