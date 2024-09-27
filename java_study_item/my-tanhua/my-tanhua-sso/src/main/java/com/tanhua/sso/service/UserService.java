package com.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.sso.mapper.UserMapper;
import com.tanhua.sso.pojo.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public String login(String phone, String code) {
        String redisKey = "CHECK_CODE_" + phone;
        boolean isNew = false;
        String codeFromRedis = redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.equals(codeFromRedis,code)){
            return null;
        }
        this.redisTemplate.delete(redisKey);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("mobile", phone);
        User user = userMapper.selectOne(userQueryWrapper);
        if(user == null){
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));

            //注册新用户
            this.userMapper.insert(user);
            isNew = true;
        }
        //生成token
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", user.getId());
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256,secret)
                .setExpiration(new DateTime().plusHours(12).toDate())
                .compact();
        // 发送用户登录成功消息
        try{
            HashMap<String, Object> msg = new HashMap<>();
            msg.put("id", user.getId());
            msg.put("data", System.currentTimeMillis());
            rocketMQTemplate.convertAndSend("tanhua-sso-login-topic", msg);
        }catch (MessagingException e){
            log.error("发送消息失败！",e);
        }
        return token+"|"+isNew;
    }


}
