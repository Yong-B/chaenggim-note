package org.example.chaenggim.domain.visit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.visit.dto.VisitCreateRequest;
import org.example.chaenggim.domain.visit.dto.VisitResponse;
import org.example.chaenggim.domain.visit.dto.VisitUpdateRequest;
import org.example.chaenggim.domain.visit.service.VisitService;
import org.example.chaenggim.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api")
public class VisitController {

    private final VisitService visitService;

    @PostMapping("/doctors/{doctorId}/visits")
    public ResponseEntity<ApiResponse<VisitResponse>> create(@PathVariable Long doctorId,
                                                             @Valid @RequestBody VisitCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(visitService.create(doctorId, request)));
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<ApiResponse<VisitResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(visitService.get(id)));
    }

    @PutMapping("/visits/{id}")
    public ResponseEntity<ApiResponse<VisitResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody VisitUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(visitService.update(id, request)));
    }

    /** D16: 질문·약속 함께 삭제. */
    @DeleteMapping("/visits/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        visitService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
