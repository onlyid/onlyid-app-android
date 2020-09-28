package net.onlyid.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import net.onlyid.Constants;

import java.time.LocalDateTime;

public class Session {
    public enum Platform {
        ANDROID, IOS
    }

    public String sessionId;
    public Integer userId;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime expireDate;
    public String deviceName;
    public String deviceId;
    public Platform platform;
    public String userAgent;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime createDate;
    public String customName;
    public String lastActiveIp;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime lastActiveDate;
    public String lastActiveLocation;
}