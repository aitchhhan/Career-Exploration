package career.exploration.controller;


import career.exploration.dto.Response.LectureIncludeFileKeyRes;
import career.exploration.dto.Response.LectureRes;
import career.exploration.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lecture")
public class LectureController {
    private final LectureService lectureService;

    @GetMapping("/{id}")
    public ResponseEntity<LectureRes> findLectureById(@PathVariable("id") Long lectureId) {
        LectureRes lectureRes = lectureService.findLectureById(lectureId);
        return ResponseEntity.status(HttpStatus.OK).body(lectureRes);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LectureIncludeFileKeyRes>> getAllLecture() {
        List<LectureIncludeFileKeyRes> lectureIncludeFileKeyRes = lectureService.findAllLecture();
        return ResponseEntity.status(HttpStatus.OK).body(lectureIncludeFileKeyRes);
    }
}
