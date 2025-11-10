package career.exploration.controller;


import career.exploration.domain.Member;
import career.exploration.dto.Request.ReqCreateLecture;
import career.exploration.dto.Request.ReqUpdateLecture;
import career.exploration.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/lecture")
public class LectureAdminController {
    private final LectureService lectureService;

    @PostMapping("/add")
    public ResponseEntity<String> uploadFiles(@AuthenticationPrincipal Member member,
                                              @RequestBody ReqCreateLecture request) throws IOException {
        lectureService.createLecture(member, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("강의자료 생성 성공");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateLecture(@AuthenticationPrincipal Member member,
                                                @RequestBody ReqUpdateLecture request) throws IOException {
        lectureService.updateLecture(member, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("강의자료 수정 성공");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLecture(@PathVariable("id") Long lectureId) {
        lectureService.deleteLecture(lectureId);
        return ResponseEntity.status(HttpStatus.OK).body("강의자료 삭제 성공");
    }

}
