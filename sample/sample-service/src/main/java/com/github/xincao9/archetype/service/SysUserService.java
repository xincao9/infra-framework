package com.github.xincao9.archetype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.xincao9.archetype.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser findByName(String name);
}
