package com.github.xincao9.archetype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.xincao9.archetype.entity.SysUser;
import com.github.xincao9.archetype.mapper.SysUserMapper;
import com.github.xincao9.archetype.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public SysUser findByName(String name) {
        String value = stringRedisTemplate.opsForValue().get(name);
        if (StringUtils.isNotBlank(value)) {
            try {
                return jsonMapper.readValue(value, SysUser.class);
            } catch (JsonProcessingException e) {
                log.error("", e);
            }
        }
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getName, name);
        SysUser sysUser = getOne(lambdaQueryWrapper);
        if (sysUser == null) {
            return null;
        }
        try {
            stringRedisTemplate.opsForValue().set(name, jsonMapper.writeValueAsString(sysUser), 1, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("", e);
        }
        return sysUser;
    }
}
