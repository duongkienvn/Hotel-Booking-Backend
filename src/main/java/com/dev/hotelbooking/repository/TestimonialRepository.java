package com.dev.hotelbooking.repository;

import com.dev.hotelbooking.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    List<Testimonial> findAllByIsApproved(boolean isApproved);
    List<Testimonial> findAllByUser_Email(String email);
}
