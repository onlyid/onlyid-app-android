package net.onlyid.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import net.onlyid.Constants;

import java.time.LocalDateTime;
import java.util.List;

public class Client {
    public enum Type {
        APP, WEB, OTHER
    }

    public Integer id;
    public String uid;
    public String iconUrl;
    public String name;
    public Type type;
    public Integer tenant;
    public List<String> redirectUris;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime createDate;
    public String description;
    public String packageName;
    public String bundleId;
}