package com.github.xincao9.archetype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xincao9.archetype.entity.SysUser;
import com.github.xincao9.archetype.mapper.SysUserMapper;
import com.github.xincao9.archetype.service.SysUserService;
import fun.golinks.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 演示，（读取缓存，判断缓存，读取数据库，回写缓存）常见业务流程
     *
     * @param name
     * @return
     */
    @Override
    public SysUser findByName(String name) {
        String value = stringRedisTemplate.opsForValue().get(name);
        if (StringUtils.isNotBlank(value)) {
            return JsonUtils.parseObject(value, SysUser.class);
        }
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getName, name);
        SysUser sysUser = getOne(lambdaQueryWrapper);
        if (sysUser == null) {
            return null;
        }
        stringRedisTemplate.opsForValue().set(name, JsonUtils.toJsonString(sysUser), 1, TimeUnit.MINUTES);
        return sysUser;
    }
}
