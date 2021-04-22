package net.onlyid.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Client1 extends Client {
    public LocalDateTime firstDate;
    public String lastIp;
    public String lastLocation;
    public LocalDateTime lastDate;
}