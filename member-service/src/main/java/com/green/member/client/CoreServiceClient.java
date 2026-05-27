package com.green.member.client;

import com.green.common.client.GpaResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "core-service", url = "${core-service.url}")
public interface CoreServiceClient {

    @GetMapping("/api/core/internal/grades/gpa/{studentCode}")
    GpaResult getGpa(@PathVariable("studentCode") Long studentCode);
}
