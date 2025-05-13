package khuong.com.postingservice.service.impl;

import khuong.com.postingservice.entity.Booking;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.BookingStatus;
import khuong.com.postingservice.repository.BookingRepository;
import khuong.com.postingservice.repository.RecruitmentPostRepository;
import khuong.com.postingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RecruitmentPostRepository recruitmentPostRepository;

    @Override
    @Transactional
    public Booking createBooking(Booking booking, Long userId) {
        RecruitmentPost post = recruitmentPostRepository.findById(booking.getRecruitmentPost().getId())
                .orElseThrow(() -> new IllegalArgumentException("Recruitment post not found"));
        
        booking.setRecruitmentPost(post);
        booking.setClientUserId(userId);
        booking.setStatus(BookingStatus.PENDING);
        
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBooking(Long bookingId, Booking updatedBooking, Long userId) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Only client can update their booking
        if (!existingBooking.getClientUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this booking");
        }
        
        // Can only update if status is PENDING
        if (existingBooking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot update a booking that is not in PENDING status");
        }
        
        existingBooking.setLocation(updatedBooking.getLocation());
        existingBooking.setNotes(updatedBooking.getNotes());
        
        return bookingRepository.save(existingBooking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Only client or post owner can delete the booking
        if (!booking.getClientUserId().equals(userId) && 
            !booking.getRecruitmentPost().getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this booking");
        }
        
        // Can only delete if status is PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot delete a booking that is not in PENDING status");
        }
        
        bookingRepository.delete(booking);
    }

    @Override
    public Optional<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    @Override
    public List<Booking> getBookingsByPostId(Long postId) {
        return bookingRepository.findByRecruitmentPostId(postId);
    }

    @Override
    public List<Booking> getBookingsByClientUserId(Long userId) {
        return bookingRepository.findByClientUserId(userId);
    }

    @Override
    public List<Booking> getBookingsForPosterUser(Long userId) {
        return bookingRepository.findBookingsForPosterUser(userId);
    }

    @Override
    public Page<Booking> getBookingsByPosterUserId(Long userId, Pageable pageable) {
        return bookingRepository.findByRecruitmentPostPosterUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Only post owner can confirm the booking
        if (!booking.getRecruitmentPost().getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to confirm this booking");
        }
        
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        
        log.info("Booking {} has been confirmed by user {}", bookingId, userId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Both client and post owner can cancel
        if (!booking.getClientUserId().equals(userId) && 
            !booking.getRecruitmentPost().getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to cancel this booking");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(reason);
        booking.setCancelledBy(userId);
        bookingRepository.save(booking);
        
        log.info("Booking {} has been cancelled by user {}", bookingId, userId);
    }

    @Override
    @Transactional
    public void completeBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Only post owner can mark as completed
        if (!booking.getRecruitmentPost().getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to complete this booking");
        }
        
        // Can only complete if status is CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot complete a booking that is not in CONFIRMED status");
        }
        
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        
        log.info("Booking {} has been marked as completed by user {}", bookingId, userId);
    }
    
    @Override
    public List<Booking> getBookingsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findBookingsBetweenDates(startDate, endDate);
    }
} 