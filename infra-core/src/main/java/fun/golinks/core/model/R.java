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
    private final Object message;

    public R(int code, T body, Object message) {
        this.code = code;
        this.body = body;
        this.message = message;
    }

    public static <T> R<T> ok(T body) {
        return new R<T>(StatusEnums.SUCCESS.getCode(), body, StatusEnums.SUCCESS.getMessage());
    }

    public static <T> R<T> failed(StatusEnums statusEnums) {
        return new R<T>(statusEnums.getCode(), null, statusEnums.getMessage());
    }
}
