package fun.golinks.core.model;

import fun.golinks.core.consts.StatusEnums;
import lombok.Getter;

/**
 * 统一mvc返回接口
 *
 * @param <T>
 */
@Getter
public class R<T> {

    private final int code;
    private final T body;
    private final String message;

    public R(int code, T body, String message) {
        this.code = code;
        this.body = body;
        this.message = message;
    }

    public static <T> R<T> ok(T body) {
        return new R(StatusEnums.SUCCESS.getCode(), body, StatusEnums.SUCCESS.getMessage());
    }

    public static <T> R<T> failed(StatusEnums statusEnums) {
        return new R(statusEnums.getCode(), null, statusEnums.getMessage());
    }
}
