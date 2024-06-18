package net.breezeware.googleCalendarApi.bff.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.calendar.model.Event;

import net.breezeware.googleCalendarApi.bff.dto.GoogleCalendarApiDto;
import net.breezeware.googleCalendarApi.bff.service.GoogleCalendarApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/google-calendar-api")
@Slf4j
@RequiredArgsConstructor
public class GoogleCalendarApiController {

    private final GoogleCalendarApiService googleCalendarApiService;

    @GetMapping
    public List<Event> getCalendarEvents() throws GeneralSecurityException, IOException {
        log.info("Entering getCalendarEvents()");
        List<Event> calendarEvents = googleCalendarApiService.getCalendarEvents();
        log.info("Leaving getCalendarEvents()");
        return calendarEvents;
    }

    @PostMapping
    public String createCalendarEvent(@RequestBody GoogleCalendarApiDto googleCalendarApiDto)
            throws GeneralSecurityException, IOException {
        log.info("Entering createCalendarEvent()");
        String calendarEvent = googleCalendarApiService.createCalendarEvent(googleCalendarApiDto);
        log.info("Leaving createCalendarEvent()");
        return calendarEvent;
    }

    @DeleteMapping("/{event-id}")
    public void deleteCalendarEvent(@PathVariable("event-id") String eventId)
            throws GeneralSecurityException, IOException {
        log.info("Entering deleteCalendarEvent()");
        googleCalendarApiService.deleteCalendarEvent(eventId);
        log.info("Leaving deleteCalendarEvent()");
    }

    @PutMapping("/{event-id}")
    public void updateCalendarEvent(@PathVariable("event-id") String eventId,
            @RequestBody GoogleCalendarApiDto googleCalendarApiDto) throws GeneralSecurityException, IOException {
        log.info("Entering updateCalendarEvent()");
        googleCalendarApiService.updateCalendarEvent(eventId,googleCalendarApiDto);
        log.info("Leaving updateCalendarEvent()");
    }
}
