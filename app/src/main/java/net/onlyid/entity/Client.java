package net.onlyid.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Client implements Serializable {
    public enum Type {
        APP, WEB, OTHER;

        public String toString() {
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
    public String tenant;
    public LocalDateTime createDate;
    public String description;
}
