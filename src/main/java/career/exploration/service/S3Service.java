package career.exploration.service;


import career.exploration.dto.PresignedUrlRes;
import career.exploration.dto.ReqPresignedUrl;
import career.exploration.enums.AllowedFileType;
import career.exploration.exception.NotAllowedFileTypeException;
import career.exploration.exception.S3PresignedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final String bucketName;

    @Value("${cloud.aws.cloudfront}")
    private String cloudfrontDomain;

    public S3Service(
            @Value("${cloud.aws.credentials.access-key}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${cloud.aws.s3}") String s3Name
    ) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.bucketName = s3Name;
        this.s3Presigner = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.s3Client = S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public PresignedUrlRes issuePresignedAndCdnUrl(ReqPresignedUrl req) {
        if (!AllowedFileType.isAllowedMimeType(req.mimetype())) {
            throw new IllegalArgumentException("허용되지 않은 MIME 타입입니다.");
        }
        System.out.println("FileName: " + req.fileName());
        System.out.println("MimeType: " + req.mimetype());
        // 확장자 추출
        String ext = Optional.ofNullable(req.fileName())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse("");

        // UUID로 안전한 파일 키 생성
        String key = "uploads/" + UUID.randomUUID() + ext;

        URL presignedUrl = generatePresignedPutUrl(key, req.fileName());
        String cdnUrl = cloudfrontDomain + key;

        return new PresignedUrlRes(presignedUrl.toString(), cdnUrl, key);
    }

    private URL generatePresignedPutUrl(String key, String mimeType) {
        if (!AllowedFileType.isAllowedMimeType(mimeType)) {
            throw new NotAllowedFileTypeException();
        }

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(mimeType)
                .build();

        // 임시 Presigned URL 발급
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // 5분간만 유효
                .putObjectRequest(putRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url();
    }

    public void deleteFiles(List<String> keys) {
        for (String key : keys) {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);
            } catch (S3Exception e) {
                throw new S3PresignedException("파일 삭제 중 오류가 발생했습니다. key: " + key);
            } catch (Exception e) {
                throw new S3PresignedException("파일 삭제 실패. key: " + key);
            }
        }
    }

}

