package org.example.chaenggim.domain.doctor.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.doctor.dto.DoctorDetailResponse;
import org.example.chaenggim.domain.doctor.dto.DoctorRequest;
import org.example.chaenggim.domain.doctor.dto.DoctorResponse;
import org.example.chaenggim.domain.doctor.dto.DoctorSummaryResponse;
import org.example.chaenggim.domain.doctor.service.DoctorService;
import org.example.chaenggim.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorSummaryResponse>>> search(
            @RequestParam(name = "q", required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(doctorService.search(q)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.create(request);
        return ResponseEntity.created(URI.create("/api/doctors/" + response.id()))
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(doctorService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(doctorService.update(id, request)));
    }

    /** D14: 숨김 처리. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
