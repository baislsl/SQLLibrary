package util;

/**
 * Created by baislsl on 17-5-6.
 */
public enum Language {
    CHINESE("res/property/sqllib_zh_CN.properties"),
    ENGLISH("res/property/sqllib.properties");

    final public String path;

    Language(String path) {
        this.path = path;
    }
}
