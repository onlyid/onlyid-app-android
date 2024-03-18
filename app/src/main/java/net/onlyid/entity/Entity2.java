package net.onlyid.entity;

import java.time.LocalDateTime;

public class Entity2 {
    public enum Type {
        APP, WEB, OTHER;

        public String toLocalizedString() {
            switch (this) {
                case APP:
                    return "APP";
                case WEB:
                    return "网站";
                default:
                    return "其他";
            }
        }
    }

    public String id;
    public String iconUrl;
    public String name;
    public Type type;
    public LocalDateTime firstDate;
    public String lastIp;
    public String lastLocation;
    public LocalDateTime lastDate;
}
