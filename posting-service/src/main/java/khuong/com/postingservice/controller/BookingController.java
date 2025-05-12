package khuong.com.postingservice.controller;

import khuong.com.postingservice.entity.Booking;
import khuong.com.postingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody Booking booking,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Booking createdBooking = bookingService.createBooking(booking, userId);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody Booking booking,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Booking updatedBooking = bookingService.updateBooking(bookingId, booking, userId);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        bookingService.deleteBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Booking>> getBookingsByPostId(@PathVariable Long postId) {
        List<Booking> bookings = bookingService.getBookingsByPostId(postId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getBookingsByClientUserId(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        List<Booking> bookings = bookingService.getBookingsByClientUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/poster")
    public ResponseEntity<List<Booking>> getBookingsForPosterUser(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        List<Booking> bookings = bookingService.getBookingsForPosterUser(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Booking>> getBookingsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Booking> bookings = bookingService.getBookingsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/poster/paged")
    public ResponseEntity<Page<Booking>> getBookingsByPosterUserId(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookings = bookingService.getBookingsByPosterUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{bookingId}/confirm")
    public ResponseEntity<Void> confirmBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        bookingService.confirmBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam String reason,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        bookingService.cancelBooking(bookingId, userId, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{bookingId}/complete")
    public ResponseEntity<Void> completeBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        bookingService.completeBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }
} 