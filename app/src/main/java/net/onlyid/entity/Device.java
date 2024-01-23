package net.onlyid.entity;

import java.time.LocalDateTime;

public class Device {
    public enum Type {
        ANDROID, IOS, BROWSER
    }

    public String sessionId;
    public String deviceId;
    public String deviceName;
    public Type type;
    public LocalDateTime firstDate;
    public String firstIp;
    public String firstLocation;
    public LocalDateTime lastDate;
    public String lastIp;
    public String lastLocation;
    public String userDeviceName;
    public LocalDateTime expireDate;
}
