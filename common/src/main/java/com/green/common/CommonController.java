package com.green.common;

import com.green.common.enumcode.EnumMapper;
import com.green.common.enumcode.EnumMapperValue;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommonController {
    private final EnumMapper enumMapper;

    @GetMapping("code")
    public ResultResponse getCodeList(@RequestParam("code_type") String codeType) {
        List<EnumMapperValue> enumCodeList = enumMapper.get(codeType);
        return ResultResponse.builder()
                .message( String.format("%d rows", enumCodeList.size()) )
                .data( enumCodeList )
                .build();
    }
}