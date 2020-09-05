package net.onlyid.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import net.onlyid.Constants;

import java.time.LocalDateTime;

public class UserClientLink {
    public Integer id;
    public Integer userId;
    public Integer clientId;
    public Integer tenant;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime createDate;
    public Client client;
    public LocalDateTime lastLoginDate;
}
