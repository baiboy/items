package com.tanhua.sso.service;

import com.tanhua.sso.config.AliyunSMSConfig;
import com.tanhua.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class SmsService {
    @Autowired
    private AliyunSMSConfig aliyunSMSConfig;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public String hookSmsCode(){
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000); // 生成范围在100000到999999之间
        return String.valueOf(randomNumber);
    }

    public ErrorResult sendSmsCode(String phone) {
        // 默认验证码发送成功 本地hook
        String redis_key = "CHECK_CODE_" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redis_key))) {
            String msg = "验证码获取失败!";
            return ErrorResult.builder().errCode("000001").errMessage(msg).build();
        }
        String code = hookSmsCode();
        this.redisTemplate.opsForValue().set(redis_key,code, Duration.ofMinutes(3));
        return null;
    }
}
