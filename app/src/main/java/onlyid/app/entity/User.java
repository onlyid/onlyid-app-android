package onlyid.app.entity;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

import onlyid.app.Constants;

public class User implements Serializable {
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public Integer id;
    public String uid;
    public String avatarUrl;
    public String nickname;
    public String mobile;
    public String email;
    public Gender gender;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime createDate;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    public LocalDateTime passwordUpdateDate;
    public String description;
    public Integer creator;
}
