package khuong.com.postingservice.repository;

import khuong.com.postingservice.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByRecruitmentPostId(Long postId);
    
    List<Booking> findByClientUserId(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.recruitmentPost.posterUserId = :userId")
    List<Booking> findBookingsForPosterUser(@Param("userId") Long userId);
    
    Page<Booking> findByRecruitmentPostPosterUserId(Long userId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 