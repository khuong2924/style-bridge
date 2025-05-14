package khuong.com.postingservice.controller;

import khuong.com.postingservice.dto.BookingDTO;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody Booking booking,
            Authentication authentication) {
        // Check if bookingDate is null
        if (booking.getBookingDate() == null) {
            return new ResponseEntity<>("Booking date (ngay_gio_hen) cannot be null", HttpStatus.BAD_REQUEST);
        }
        
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Booking createdBooking = bookingService.createBooking(booking, userId);
        BookingDTO bookingDTO = BookingDTO.fromEntity(createdBooking);
        return new ResponseEntity<>(bookingDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody Booking booking,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Booking updatedBooking = bookingService.updateBooking(bookingId, booking, userId);
        BookingDTO bookingDTO = BookingDTO.fromEntity(updatedBooking);
        return ResponseEntity.ok(bookingDTO);
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
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId)
                .map(booking -> ResponseEntity.ok(BookingDTO.fromEntity(booking)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByPostId(@PathVariable Long postId) {
        List<Booking> bookings = bookingService.getBookingsByPostId(postId);
        List<BookingDTO> bookingDTOs = bookings.stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTOs);
    }

    @GetMapping("/user")
    public ResponseEntity<List<BookingDTO>> getBookingsByClientUserId(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        List<Booking> bookings = bookingService.getBookingsByClientUserId(userId);
        List<BookingDTO> bookingDTOs = bookings.stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTOs);
    }

    @GetMapping("/poster")
    public ResponseEntity<List<BookingDTO>> getBookingsForPosterUser(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        List<Booking> bookings = bookingService.getBookingsForPosterUser(userId);
        List<BookingDTO> bookingDTOs = bookings.stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTOs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<BookingDTO>> getBookingsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Booking> bookings = bookingService.getBookingsBetweenDates(startDate, endDate);
        List<BookingDTO> bookingDTOs = bookings.stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTOs);
    }

    @GetMapping("/poster/paged")
    public ResponseEntity<Page<BookingDTO>> getBookingsByPosterUserId(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookings = bookingService.getBookingsByPosterUserId(userId, pageable);
        Page<BookingDTO> bookingDTOs = bookings.map(BookingDTO::fromEntity);
        return ResponseEntity.ok(bookingDTOs);
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