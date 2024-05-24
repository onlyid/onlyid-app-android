package net.onlyid.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Otp implements Serializable {
    public Integer id;
    public String code;
    public LocalDateTime expireDate;
    public Boolean sso;
    public String clientId;
    public String clientIconUrl;
    public String clientName;
}
