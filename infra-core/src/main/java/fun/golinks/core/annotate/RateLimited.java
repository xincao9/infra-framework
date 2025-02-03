package fun.golinks.core.annotate;

/**
 * 限流
 */
public @interface RateLimited {

    /**
     * 流速
     *
     * @return 流速
     */
    int permitsPerSecond() default 0;
}
