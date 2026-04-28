package com.green.core.major.application;
import com.green.core.major.application.model.MajorCreateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/major")
public class MajorController {

    private final MajorService majorService;
//
//    @PostMapping
//    public void createMajor(@RequestBody MajorCreateReq req) {
//        majorService.createMajor(req);
//    }
}