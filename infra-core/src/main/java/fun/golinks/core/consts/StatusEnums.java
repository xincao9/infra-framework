package fun.golinks.core.consts;

import lombok.Getter;

@Getter
public enum StatusEnums {
    SUCCESS(1, "success."), SYSTEM_EXCEPTION(2, "system exception."),
    RATE_LIMIT_EXCEEDED(100, "Rate limit exceeded, please try again later.");

    private final int code;
    private final String message;

    StatusEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
