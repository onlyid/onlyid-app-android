package net.onlyid.entity;

import java.time.LocalDateTime;

public class Session {
    public String token;
    public User user;
    public LocalDateTime expireDate;
}
