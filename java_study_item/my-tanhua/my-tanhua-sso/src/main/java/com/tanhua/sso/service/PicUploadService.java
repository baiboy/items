package com.tanhua.sso.service;

import com.tanhua.sso.config.MinioConfig;
import com.tanhua.sso.vo.PicUploadResult;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@Service
public class PicUploadService {
    private static final String[] IMAGE_TYPE = {"jpg","jpeg","png","gif"};
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;
    public PicUploadResult upload(MultipartFile file) {
        PicUploadResult picUploadResult = new PicUploadResult();
        boolean isLegal = false;
        for (String type : IMAGE_TYPE) {
            if (StringUtils.endsWithIgnoreCase(file.getOriginalFilename(), type)) {
                isLegal = true;
                break;
            }
        }
        if (!isLegal) {
            picUploadResult.setStatus("error");
            return picUploadResult;
        }
        String fileName = file.getOriginalFilename();
        String filePath = getFilePath(fileName);

        try{
            minioClient.putObject(PutObjectArgs.builder().bucket(minioConfig.getBucket())
            .object(filePath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        }catch (Exception e){
            e.printStackTrace();
            picUploadResult.setStatus("error");
            return picUploadResult;
        }
        picUploadResult.setStatus("done");
        picUploadResult.setName(minioConfig.getEndpoint()+"/"+minioConfig.getBucket()+filePath);
        picUploadResult.setUid(String.valueOf(System.currentTimeMillis()));
        return picUploadResult;
    }

    private String getFilePath(String fileName) {
        DateTime dateTime = new DateTime();
        return  "/images/" + dateTime.toString("yyyy")
                + "/" + dateTime.toString("MM")
                + "/" + dateTime.toString("dd")
                + "/" + System.currentTimeMillis()
                + RandomUtils.nextInt(100,9999)
                + "."
                + StringUtils.substringAfterLast(fileName, ".");
    }
}
