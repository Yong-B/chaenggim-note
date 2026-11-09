package org.example.chaenggim.domain.commitment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.commitment.dto.CommitmentUpdateRequest;
import org.example.chaenggim.domain.commitment.service.CommitmentService;
import org.example.chaenggim.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/commitments")
public class CommitmentController {

    private final CommitmentService commitmentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommitmentResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommitmentResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody CommitmentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentService.update(id, request)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<CommitmentResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentService.complete(id)));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<CommitmentResponse>> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentService.reopen(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        commitmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
