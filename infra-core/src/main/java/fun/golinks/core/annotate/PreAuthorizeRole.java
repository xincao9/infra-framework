package fun.golinks.core.annotate;

import fun.golinks.core.consts.RoleEnums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreAuthorizeRole {

    /**
     * 角色类型
     */
    RoleEnums value();
}