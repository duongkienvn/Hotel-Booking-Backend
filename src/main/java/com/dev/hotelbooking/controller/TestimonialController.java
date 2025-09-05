package com.dev.hotelbooking.controller;

import com.dev.hotelbooking.dto.request.TestimonialRequest;
import com.dev.hotelbooking.dto.response.TestimonialResponse;
import com.dev.hotelbooking.service.ITestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/testimonials")
@RequiredArgsConstructor
public class TestimonialController {
    private final ITestimonialService testimonialService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<TestimonialResponse> addNewTestimonial(@Valid @RequestBody TestimonialRequest testimonialRequest) {
        return ResponseEntity.ok(testimonialService.addTestimonial(testimonialRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TestimonialResponse> updateTestimonial(@PathVariable Long id,
                                                                 @Valid @RequestBody TestimonialRequest testimonialRequest) {
        return ResponseEntity.ok(testimonialService.updateTestimonial(id, testimonialRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectTestimonial(@PathVariable Long id) {
        testimonialService.rejectTestimonial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestimonialResponse> getTestimonialById(@PathVariable Long id) {
        return ResponseEntity.ok(testimonialService.getTestimonialById(id));
    }

    @GetMapping("/approved")
    public ResponseEntity<List<TestimonialResponse>> getAllApprovedTestimonials() {
        return ResponseEntity.ok(testimonialService.getAllApprovedTestimonials());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TestimonialResponse>> getAllTestimonials() {
        return ResponseEntity.ok(testimonialService.getAllTestimonials());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestimonialResponse> approveTestimonial(@PathVariable Long id) {
        TestimonialResponse testimonialResponse = testimonialService.approveTestimonial(id);
        return ResponseEntity.ok(testimonialResponse);
    }
}
