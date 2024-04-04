package site.handglove.labserver.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    private Result() {

    }

    public static <T> Result<T> wrap(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> r = new Result<>();

        if (body != null) {
            r.setData(body);
        }
        r.setMsg(resultCodeEnum.getMsg());
        r.setCode(resultCodeEnum.getCode());

        return r;
    }

    public static <T> Result<T> OK() {
        return Result.wrap(null, ResultCodeEnum.SUCCESS);
    }

    public static <T> Result<T> OK(T data) {
        return Result.wrap(data, ResultCodeEnum.SUCCESS);
    }

    public static <T> Result<T> FAIL() {
        return Result.wrap(null, ResultCodeEnum.FAIL);
    }

    public static <T> Result<T> FAIL(T data) {
        return Result.wrap(data, ResultCodeEnum.FAIL);
    }

    public Result<T> message(String msg) {
        this.setMsg(msg);
        return this;
    }

    public Result<T> code(int code) {
        this.setCode(code);
        return this;
    }
}
