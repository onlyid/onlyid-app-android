package net.onlyid.entity;

import java.time.LocalDateTime;

public class MySession {
    public enum Platform {
        ANDROID, IOS
    }

    public String sessionId;
    public String userId;
    public LocalDateTime expireDate;
    public String deviceName;
    public String deviceId;
    public Platform platform;
    public String userAgent;
    public LocalDateTime createDate;
    public String customName;
    public String lastActiveIp;
    public LocalDateTime lastActiveDate;
    public String lastActiveLocation;
}