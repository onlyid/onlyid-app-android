package net.onlyid.entity;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import net.onlyid.Constants;

public class User {
    public enum Gender {
        MALE, FEMALE, OTHER;

        public String toLocalizedString() {
            switch (this) {
                case MALE:
                    return "男";
                case FEMALE:
                    return "女";
                default:
                    return "其他";
            }
        }
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
    public LocalDateTime updatePasswordDate;
    public String description;
    public Integer creator;
}
