package net.onlyid.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Client implements Serializable {
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

    public Integer id;
    public String uid;
    public String iconUrl;
    public String name;
    public Type type;
    public Integer tenant;
    public List<String> redirectUris;
    public LocalDateTime createDate;
    public String description;
    public String packageName;
    public String bundleId;
}