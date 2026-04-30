package com.green.core.application.controller;

import com.green.common.model.ResultResponse;
import com.green.core.application.model.major.MajorCreateReq;
import com.green.core.application.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/majors")
public class MajorController {
    private final MajorService majorService;
//
//    @PostMapping
//    public ResultResponse<?> test(@RequestBody MajorCreateReq req ) {
//        log.info("req: {}", req);
//        majorService.test( req );
//        return ResultResponse.builder()
//                .message( "테스트 성공" )
//                .data( 1 )
//                .build();
//    }
}
