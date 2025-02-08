package fun.golinks.core.consts;

import lombok.Getter;

@Getter
public enum StatusEnums {
    SUCCESS(200, "success."), SYSTEM_EXCEPTION(500, "system exception."), BAD_REQUEST(400, "bad BAD_REQUEST."),
    RATE_LIMIT_EXCEEDED(403, "Rate limit exceeded, please try again later."),;

    private final int code;
    private final String message;

    StatusEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
