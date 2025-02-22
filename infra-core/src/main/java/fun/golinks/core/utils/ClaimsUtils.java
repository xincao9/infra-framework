package fun.golinks.core.utils;

import io.jsonwebtoken.Claims;

public class ClaimsUtils {

    private static final ThreadLocal<Claims> CLAIMS_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取当前线程的Claims对象。 该方法从线程本地变量中获取与当前线程关联的Claims对象。Claims通常用于存储和传递与用户身份验证相关的信息，
     * 如用户ID、角色等。通过线程本地变量，可以确保每个线程都有自己独立的Claims对象，避免多线程环境下的数据竞争问题。
     *
     * @return 当前线程的Claims对象，如果当前线程没有关联的Claims对象，则返回null。
     */
    public static Claims getClaims() {
        return CLAIMS_THREAD_LOCAL.get();
    }

    /**
     * 设置当前线程的Claims对象。 该方法将传入的Claims对象存储到线程本地变量中，以便在当前线程的后续操作中使用。
     *
     * @param claims
     *            要设置的Claims对象，表示当前线程的声明信息。
     */
    public static void setClaims(Claims claims) {
        CLAIMS_THREAD_LOCAL.set(claims);
    }

    /**
     * 移除当前线程中存储的Claims对象。 该函数通过调用`CLAIMS_THREAD_LOCAL.remove()`方法，清除当前线程的ThreadLocal变量中存储的Claims对象。
     * 通常用于在请求处理完成后，清理线程局部变量，避免内存泄漏或数据污染。
     */
    public static void removeClaims() {
        CLAIMS_THREAD_LOCAL.remove();
    }

}
