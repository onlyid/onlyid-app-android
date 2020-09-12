package net.onlyid.entity;

import java.io.Serializable;

public class OAuthConfig implements Serializable {
    public String clientId; // 你的应用id
    public String view; // 显示界面，设置为zoomed 放大显示，否则默认正常显示
    public String theme; // 主题样式，设置为dark 夜间主题，否则默认日间主题
    public String state; // oauth安全相关，不懂可以忽略
}