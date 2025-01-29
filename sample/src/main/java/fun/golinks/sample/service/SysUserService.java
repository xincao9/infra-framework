package fun.golinks.sample.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.golinks.sample.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser findByName(String name);
}
