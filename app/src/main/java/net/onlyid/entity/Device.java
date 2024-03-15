package net.onlyid.entity;

import java.time.LocalDateTime;

public class Device {
    public enum Type {
        android, apple
    }

    public String sessionId;
    public String deviceId;
    public String name;
    public Type type;
    public LocalDateTime createDate;
    public LocalDateTime lastDate;
    public String lastIp;
    public String lastLocation;
    public String customName;
    public LocalDateTime expireDate;
}
