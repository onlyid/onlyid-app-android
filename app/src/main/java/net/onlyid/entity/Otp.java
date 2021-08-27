package net.onlyid.entity;

import java.time.LocalDateTime;

public class Otp {
    public enum Channel {
        OTP, SSO, PRIVATE
    }

    public Integer id;
    public String recipient;
    public String code;
    public LocalDateTime createDate;
    public LocalDateTime expireDate;
    public Integer failCount;
    public Integer maxFailCount;
    public Boolean sendSuccess;
    public Boolean verifySuccess;
    public Channel channel;
    public String clientId;
    public String tenant;

    public String clientIconUrl;
}
