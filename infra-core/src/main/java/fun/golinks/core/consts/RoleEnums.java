package fun.golinks.core.consts;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Getter
public enum RoleEnums {
    TOURIST(1, "tourist", "游客"), NORMAL((1 << 1) + 1, "normal", "普通用户"), VIP((1 << 2) + (1 << 1) + 1, "vip", "VIP用户"),
    ADMIN((1 << 3) + (1 << 2) + (1 << 1) + 1, "admin", "管理员");

    /**
     * 编号
     */
    private final Integer no;

    /**
     * 标志符
     */
    private final String flag;

    /**
     * 名称
     */
    private final String name;

    RoleEnums(Integer no, String flag, String name) {
        this.no = no;
        this.flag = flag;
        this.name = name;
    }

    public static RoleEnums fromFlag(String flag) {
        if (StringUtils.isBlank(flag)) {
            return TOURIST;
        }
        for (RoleEnums roleEnums : RoleEnums.values()) {
            if (Objects.equals(flag, roleEnums.getFlag())) {
                return roleEnums;
            }
        }
        return TOURIST;
    }

    /**
     * 判断角色枚举a是否包含角色枚举b的所有权限。
     * <p>
     * 该函数通过比较两个角色枚举的编号（no）的位运算结果，来判断a是否包含b的所有权限。 具体来说，如果a的编号与b的编号进行按位与运算的结果等于b的编号，则说明a包含b的所有权限。
     *
     * @param a
     *            第一个角色枚举，表示待检查的角色
     * @param b
     *            第二个角色枚举，表示需要包含的角色
     * 
     * @return 如果a包含b的所有权限，则返回true；否则返回false
     */
    public static Boolean contain(RoleEnums a, RoleEnums b) {
        return (a.getNo() & b.getNo()) == b.getNo();
    }
}
