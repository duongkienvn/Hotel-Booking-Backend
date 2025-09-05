package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.dto.request.TestimonialRequest;
import com.dev.hotelbooking.dto.response.TestimonialResponse;
import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.model.Testimonial;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.repository.TestimonialRepository;
import com.dev.hotelbooking.repository.UserRepository;
import com.dev.hotelbooking.service.ITestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestimonialService implements ITestimonialService {
    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;

    @Override
    public TestimonialResponse addTestimonial(TestimonialRequest testimonialRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = "";
        if (authentication != null && authentication.isAuthenticated()) {
            email = authentication.getName();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Testimonial testimonial = Testimonial.builder()
                .content(testimonialRequest.getContent())
                .rating(testimonialRequest.getRating())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        Testimonial savedTest = testimonialRepository.save(testimonial);
        return getTestimonialResponse(savedTest);
    }

    @Override
    public void rejectTestimonial(Long id) {
        Testimonial testimonial = getTesTimonialEntityById(id);
        testimonialRepository.delete(testimonial);
    }

    @Override
    public TestimonialResponse updateTestimonial(Long id, TestimonialRequest testimonialRequest) {
        Testimonial testimonial = getTesTimonialEntityById(id);
        testimonial.setContent(testimonialRequest.getContent());
        testimonial.setRating(testimonialRequest.getRating());
        testimonial.setUpdatedAt(LocalDateTime.now());
        testimonialRepository.save(testimonial);
        return getTestimonialResponse(testimonial);
    }

    @Override
    public TestimonialResponse getTestimonialById(Long id) {
        Testimonial testimonial = getTesTimonialEntityById(id);
        return getTestimonialResponse(testimonial);
    }

    @Override
    public List<TestimonialResponse> getAllApprovedTestimonials() {
        List<Testimonial> testimonials = testimonialRepository.findAllByIsApproved(true);
        return testimonials.stream().map(this::getTestimonialResponse).toList();
    }

    @Override
    public List<TestimonialResponse> getAllTestimonials() {
        List<Testimonial> testimonials = testimonialRepository.findAll();
        return testimonials.stream().map(this::getTestimonialResponse).toList();
    }

    @Override
    public TestimonialResponse approveTestimonial(Long id) {
        Testimonial testimonial = getTesTimonialEntityById(id);
        testimonial.setApproved(true);
        testimonial.setUpdatedAt(LocalDateTime.now());
        testimonialRepository.save(testimonial);
        return getTestimonialResponse(testimonial);
    }

    private Testimonial getTesTimonialEntityById(Long id) {
        return testimonialRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TESTIMONIAL_NOT_FOUND));
    }

    private TestimonialResponse getTestimonialResponse(Testimonial testimonial) {
        return TestimonialResponse.builder()
                .id(testimonial.getId())
                .content(testimonial.getContent())
                .rating(testimonial.getRating())
                .user(testimonial.getUser())
                .isApproved(testimonial.isApproved())
                .createdAt(testimonial.getCreatedAt())
                .updatedAt(testimonial.getUpdatedAt())
                .build();
    }
}
