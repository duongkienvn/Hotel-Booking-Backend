package com.dev.hotelbooking.service;

import com.dev.hotelbooking.dto.request.TestimonialRequest;
import com.dev.hotelbooking.dto.response.TestimonialResponse;

import java.util.List;

public interface ITestimonialService {
    TestimonialResponse addTestimonial(TestimonialRequest testimonialRequest);
    void rejectTestimonial(Long id);
    TestimonialResponse updateTestimonial(Long id, TestimonialRequest testimonialRequest);
    TestimonialResponse getTestimonialById(Long id);
    List<TestimonialResponse> getAllApprovedTestimonials();
    TestimonialResponse approveTestimonial(Long id);
    List<TestimonialResponse> getAllTestimonials();
}
