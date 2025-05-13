package khuong.com.postingservice.service;

import khuong.com.postingservice.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    
    Booking createBooking(Booking booking, Long userId);
    
    Booking updateBooking(Long bookingId, Booking updatedBooking, Long userId);
    
    void deleteBooking(Long bookingId, Long userId);
    
    Optional<Booking> getBookingById(Long bookingId);
    
    List<Booking> getBookingsByPostId(Long postId);
    
    List<Booking> getBookingsByClientUserId(Long userId);
    
    List<Booking> getBookingsForPosterUser(Long userId);
    
    Page<Booking> getBookingsByPosterUserId(Long userId, Pageable pageable);
    
    void confirmBooking(Long bookingId, Long userId);
    
    void cancelBooking(Long bookingId, Long userId, String reason);
    
    void completeBooking(Long bookingId, Long userId);
    
    List<Booking> getBookingsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
} 