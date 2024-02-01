package com.itwill.matzip.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Utility {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    
    
    // 허용할 이미지 확장자
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    
    
    
    // 파일 업로드 및 URL 생성
    /**
     * 단일 이미지 파일을 S3 버킷에 업로드, 이미지의 URL을 생성.
     * 
     * @param file        업로드할 이미지 파일(MultipartFile)
     * @param s3FileName  S3에 저장될 파일 이름
     * @return 업로드된 이미지 URL
     */
    public String uploadImageToS3(MultipartFile file, String s3FileName) {
        // 파일 확장자 체크
        if (!isImageExtensionAllowed(file.getOriginalFilename())) {
            log.error("업로드 할 수 없는 파일 형식입니다.");
            return "";
        }

        try {
            ObjectMetadata objMeta = new ObjectMetadata();
            objMeta.setContentLength(file.getSize());
            objMeta.setContentType(file.getContentType());
            objMeta.setHeader("Content-Disposition", "inline");

            // S3에 파일 업로드
            amazonS3.putObject(bucket, s3FileName, file.getInputStream(), objMeta);

            // 업로드된 파일 URL 생성
            return generateUrl(s3FileName);

        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
            return "";
        }
    }
    
    
    

    // 파일 여러 개 업로드, URL 생성
    /**
     * 여러 이미지 파일을 S3 버킷에 업로드하고 이미지파일들의 URL 생성.
     * 
     * @param files 업로드할 이미지 파일 목록(List<MultipartFile>)
     * @return 업로드된 이미지의 URL 목록
     */
    public List<String> uploadImageListToS3(List<MultipartFile> files) {
        List<String> uploadedUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                // 파일 확장자 체크
                if (!isImageExtensionAllowed(file.getOriginalFilename())) {
                    log.error("업로드 할 수 없는 파일형식이 포함되어있습니다. 업로드 제외 파일명: {}", file.getOriginalFilename());
                    continue;
                }

                String s3FileName = generateFileName();

                ObjectMetadata objMeta = new ObjectMetadata();
                objMeta.setContentLength(file.getSize());
                objMeta.setContentType(file.getContentType());
                objMeta.setHeader("Content-Disposition", "inline");

                // S3에 파일 업로드
                amazonS3.putObject(bucket, s3FileName, file.getInputStream(), objMeta);

                // 업로드된 파일의 URL 생성, 리스트에 추가
                String fileUrl = generateUrl(s3FileName);
                uploadedUrls.add(fileUrl);
            }

            return uploadedUrls;

        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    
    
    /**
     * UUID를 사용하여 고유 파일명 생성.
     * 
     * @return  생성된 파일명(String)
     */
    public String generateFileName() {
        UUID uuid = UUID.randomUUID();
        return "upload_" + uuid.toString();
    }
    
    

    /**
     * S3 버킷에서 단일 이미지 파일 삭제.
     * 
     * @param s3FileName  삭제할 파일의 S3 이름
     */
    public void deleteImageFromS3(String s3FileName) {
        try {
            if (!amazonS3.doesObjectExist(bucket, s3FileName)) {
                log.warn("파일 삭제 실패: 파일이 존재하지 않습니다. 파일명: {}", s3FileName);
                return;
            }
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, s3FileName));
            log.info("파일 삭제 성공: {}", s3FileName);
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생. 파일명: {}, 오류: {}", s3FileName, e.getMessage());
        }
    }

    
    
    /**
     * S3 버킷에서 여러 이미지 파일을 삭제.
     * 
     * @param s3FileNames  삭제할 파일 이름 리스트(List<String>)
     */
    public void deleteImageListFromS3(List<String> s3FileNames) {
        try {
            for (String s3FileName : s3FileNames) {
                if (!amazonS3.doesObjectExist(bucket, s3FileName)) {
                    log.warn("파일 삭제 실패: 파일이 존재하지 않습니다. 파일명: {}", s3FileName);
                    continue;
                }

                amazonS3.deleteObject(new DeleteObjectRequest(bucket, s3FileName));
                log.info("파일 삭제 성공: {}", s3FileName);
            }
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생. 오류: {}", e.getMessage());
        }
    }

    
    
    /**
     * S3에 업로드된 파일의 URL을 생성. (만료 시간 설정x)
     * 
     * @param s3FileName  URL 생성할 파일의 이름
     * @return 생성된 URL(String)
     */
    private String generateUrl(String s3FileName) {
        try {
            URL url = amazonS3.getUrl(bucket, s3FileName);
            return URLDecoder.decode(url.toString(), "utf-8");
        } catch (Exception e) {
            log.error("이미지 URL을 생성하는 중 오류 발생. 파일명: {}, 오류: {}", s3FileName, e.getMessage());
            return "";
        }
    }

 
    
    /**
     * 파일 확장자가 허용되는지 확인
     * 
     * @param filename  확인할 파일명
     * @return 허용 여부(Boolean)
     */
    private boolean isImageExtensionAllowed(String filename) {
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(fileExtension);
    }
    
    
    /**
     * 현재 설정된 S3 버킷 이름을 반환.
     * 
     * @return  S3 버킷 이름(String)
     */
    public String getBucket() {
        return bucket;
    }
}
