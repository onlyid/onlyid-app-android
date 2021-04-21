package net.onlyid.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public String id;
    public String avatarUrl;
    public String nickname;
    public String mobile;
    public String email;
    public Gender gender;
    public LocalDate birthDate;
    public String province;
    public String city;
    public String bio;
    public LocalDateTime createDate;
    public LocalDateTime updatePasswordDate;
}
