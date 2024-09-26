package com.tanhua.sso.controller;

import com.tanhua.sso.service.SmsService;
import com.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@Slf4j
public class SmsController {
    @Autowired
    private SmsService smsService;

    @GetMapping("/sendSmsCode")
    public ResponseEntity<ErrorResult> sendSmsCode(@RequestParam String phone){
        ErrorResult errorResult = null;
        try{
            errorResult = smsService.sendSmsCode(phone);
            if (null == errorResult){
                return ResponseEntity.ok(errorResult);
            }
        } catch (Exception e) {
            log.error("发送短信验证码失败");
            errorResult = ErrorResult.builder().errCode("000002").errMessage("短信验证码发送失败").build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }


}
