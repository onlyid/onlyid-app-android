package net.onlyid.entity;

import java.time.LocalDate;

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
    public String avatar;
    public String nickname;
    public String mobile;
    public String email;
    public Gender gender;
    public LocalDate birthDate;
    public String province;
    public String city;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", avatar='" + avatar + '\'' +
                ", nickname='" + nickname + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
