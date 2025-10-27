package career.exploration.controller;


import career.exploration.dto.PresignedUrlRes;
import career.exploration.dto.ReqPresignedUrl;
import career.exploration.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3PresignedService;

    @PostMapping("/s3/presigned-urls")
    public ResponseEntity<?> getPresignedUrls(@RequestBody List<ReqPresignedUrl> requests) {
        List<PresignedUrlRes> response = requests.stream()
                .map(s3PresignedService::issuePresignedAndCdnUrl)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/s3")
    public ResponseEntity<Void> deleteFile(@RequestBody List<String> keys) {
        s3PresignedService.deleteFiles(keys);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

