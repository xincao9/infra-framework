package fun.golinks.archetype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.golinks.archetype.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser findByName(String name);
}
