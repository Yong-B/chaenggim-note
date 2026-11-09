package org.example.chaenggim.domain.home.controller;

import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.home.dto.HomeResponse;
import org.example.chaenggim.domain.home.service.HomeService;
import org.example.chaenggim.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomeResponse>> home() {
        return ResponseEntity.ok(ApiResponse.ok(homeService.load()));
    }
}
