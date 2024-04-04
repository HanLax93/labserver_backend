package site.handglove.labserver.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(20000, "success"),
    FAIL(20001, "fail"),
    LOGIN_AUTH(20008, "not login"),
    PERMISSION(20009, "no permission");

    private Integer code;
    private String msg;

    private ResultCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
