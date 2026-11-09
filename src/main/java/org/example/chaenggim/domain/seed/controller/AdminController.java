package org.example.chaenggim.domain.seed.controller;

import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.seed.service.SeedService;
import org.example.chaenggim.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final SeedService seedService;

    /** 전체 삭제 후 시드 입력(한 트랜잭션). */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> reset() {
        seedService.reset();
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
