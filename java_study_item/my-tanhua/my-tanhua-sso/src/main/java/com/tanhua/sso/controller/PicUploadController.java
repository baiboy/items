package com.tanhua.sso.controller;

import com.tanhua.sso.service.PicUploadService;
import com.tanhua.sso.vo.PicUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("pic")
@RestController
public class PicUploadController {
    @Autowired
    private PicUploadService picUploadService;
    @PostMapping("upload")
    public PicUploadResult upload(@RequestParam("file") MultipartFile file) {
        return picUploadService.upload(file);
    }
}
