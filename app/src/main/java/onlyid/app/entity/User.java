package onlyid.app.entity;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import onlyid.app.Constants;

public class User {
    public enum Gender {
        MALE, FEMALE, OTHER;

        @NonNull
        @Override
        public String toString() {
            switch (this) {
                case MALE:
                    return "男";
                case FEMALE:
                    return "女";
                case OTHER:
                    return "其他";
                default:
                    return null;
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
    public LocalDateTime passwordUpdateDate;
    public String description;
    public Integer creator;
}
