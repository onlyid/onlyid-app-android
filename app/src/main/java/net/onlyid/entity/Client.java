package net.onlyid.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public String id;
    public String iconUrl;
    public String name;
    public Type type;
    public String tenant;
    public List<String> redirectUris;
    public LocalDateTime createDate;
    public String description;
    public String packageName;
    public String bundleId;
}