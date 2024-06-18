package net.breezeware.googleCalendarApi.bff.service;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.springframework.stereotype.Service;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;


import net.breezeware.googleCalendarApi.bff.dto.GoogleCalendarApiDto;


import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class GoogleCalendarApiService {


    private static final String APPLICATION_NAME = "APPLICATION_NAME";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACCOUNT_KEY_FILE =
            "path-of-downloaded-key-json-file";


    /**
     * Retrieves the list of calendar events from the primary calendar.
     *
     * @return List of events retrieved.
     * @throws GeneralSecurityException If security credentials cannot be established.
     * @throws IOException              If an error occurs while communicating with the Google Calendar API.
     */
    public List<Event> getCalendarEvents() throws GeneralSecurityException, IOException {
        log.info("Entering getCalendarEvents()");
        Calendar service = getCalendarService();


        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("calendar-id").setMaxResults(10).setTimeMin(now)
                .setOrderBy("startTime").setSingleEvents(true).execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events:");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }


                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }


        }


        log.info("Leaving getCalendarEvents()");
        return items;
    }


    /**
     * Creates a new event on the Google Calendar.
     *
     * @param googleCalendarApiDto Details of the event to be created.
     * @return The ID of the newly created event.
     * @throws GeneralSecurityException If security credentials cannot be established.
     * @throws IOException              If an error occurs while communicating with the Google Calendar API.
     */
    public String createCalendarEvent(GoogleCalendarApiDto googleCalendarApiDto)
            throws GeneralSecurityException, IOException {
        log.info("Entering createCalendarEvent()");
        Calendar service = getCalendarService();
        Event event = new Event().setSummary(googleCalendarApiDto.getSummary())
                .setLocation(googleCalendarApiDto.getLocation()).setDescription(googleCalendarApiDto.getDescription());


        DateTime startDateTime = new DateTime(googleCalendarApiDto.getEventStartDateTime());
        EventDateTime start =
                new EventDateTime().setDateTime(startDateTime).setTimeZone(googleCalendarApiDto.getEventTimeZone());
        event.setStart(start);


        DateTime endDateTime = new DateTime(googleCalendarApiDto.getEventEndDateTime());
        EventDateTime end =
                new EventDateTime().setDateTime(endDateTime).setTimeZone(googleCalendarApiDto.getEventTimeZone());
        event.setEnd(end);


        List<EventAttendee> eventAttendeeList = new ArrayList<>();


        List<String> eventAttendees = googleCalendarApiDto.getEventAttendees();
        eventAttendees.forEach(eventAttendee -> eventAttendeeList.add(new EventAttendee().setEmail(eventAttendee)));


        EventAttendee[] eventAttendeeArray = eventAttendeeList.toArray(new EventAttendee[0]);


        event.setAttendees(Arrays.asList(eventAttendeeArray));


        EventReminder[] reminderOverrides =
                new EventReminder[] { new EventReminder().setMethod("popup").setMinutes(2), };
        Event.Reminders reminders =
                new Event.Reminders().setUseDefault(false).setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);


        String calendarId = "calendar-id";


        if (googleCalendarApiDto.isMeeting()) {
            ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey().setType("hangoutsMeet");
            CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest().setRequestId("sample123")
                    .setConferenceSolutionKey(conferenceSolutionKey);
            ConferenceData conferenceData = new ConferenceData().setCreateRequest(createConferenceRequest);
            event.setConferenceData(conferenceData);
        }


        event = service.events().insert(calendarId, event).setSendNotifications(true).setConferenceDataVersion(1)
                .execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
        log.info("Leaving createCalendarEvent()");
        return event.getId();
    }


    /**
     * Retrieves a Calendar service instance initialised with the Google credentials.
     *
     * @return Initialized Calendar service instance.
     * @throws GeneralSecurityException If security credentials cannot be established.
     * @throws IOException              If an error occurs while initialising the Calendar service.
     */
    public void deleteCalendarEvent(String eventId) throws GeneralSecurityException, IOException {
        log.info("Entering deleteCalendarEvent()");
        Calendar service = getCalendarService();
        String calendarId = "calendar-id";
        Event event = service.events().get(calendarId, eventId).execute();


        if (event != null) {
            service.events().delete("calendar-id", eventId).setSendNotifications(true).execute();
        }


        log.info("Leaving deleteCalendarEvent()");
    }


    /**
     * Updates an existing event on the Google Calendar.
     *
     * @param eventId             ID of the event to be updated.
     * @param googleCalendarApiDto Updated details of the event.
     * @throws GeneralSecurityException If security credentials cannot be established.
     * @throws IOException              If an error occurs while communicating with the Google Calendar API.
     */
    public void updateCalendarEvent(String eventId, GoogleCalendarApiDto googleCalendarApiDto)
            throws GeneralSecurityException, IOException {
        log.info("Entering updateCalendarEvent()");
        Calendar service = getCalendarService();
        String calendarId = "calendar-id";
        Event event = service.events().get(calendarId, eventId).execute();


        if (event != null) {
            Event updatedEvent = new Event().setSummary(googleCalendarApiDto.getSummary())
                    .setLocation(googleCalendarApiDto.getLocation())
                    .setDescription(googleCalendarApiDto.getDescription());


            DateTime startDateTime = new DateTime(googleCalendarApiDto.getEventStartDateTime());
            EventDateTime start =
                    new EventDateTime().setDateTime(startDateTime).setTimeZone(googleCalendarApiDto.getEventTimeZone());
            updatedEvent.setStart(start);


            DateTime endDateTime = new DateTime(googleCalendarApiDto.getEventEndDateTime());
            EventDateTime end =
                    new EventDateTime().setDateTime(endDateTime).setTimeZone(googleCalendarApiDto.getEventTimeZone());
            updatedEvent.setEnd(end);


            List<EventAttendee> eventAttendeeList = new ArrayList<>();


            List<String> eventAttendees = googleCalendarApiDto.getEventAttendees();
            eventAttendees.forEach(eventAttendee -> eventAttendeeList.add(new EventAttendee().setEmail(eventAttendee)));


            EventAttendee[] eventAttendeeArray = eventAttendeeList.toArray(new EventAttendee[0]);


            updatedEvent.setAttendees(Arrays.asList(eventAttendeeArray));


            EventReminder[] reminderOverrides =
                    new EventReminder[] { new EventReminder().setMethod("popup").setMinutes(2), };
            Event.Reminders reminders =
                    new Event.Reminders().setUseDefault(false).setOverrides(Arrays.asList(reminderOverrides));
            updatedEvent.setReminders(reminders);


            if (googleCalendarApiDto.isMeeting()) {
                ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey().setType("hangoutsMeet");
                CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest()
                        .setRequestId("sample123").setConferenceSolutionKey(conferenceSolutionKey);
                ConferenceData conferenceData = new ConferenceData().setCreateRequest(createConferenceRequest);
                updatedEvent.setConferenceData(conferenceData);
            } else {
                updatedEvent.setConferenceData(null);
            }


            service.events().update(calendarId, eventId, updatedEvent).setSendNotifications(true).execute();
        }


        log.info("Leaving updateCalendarEvent()");
    }


    public static Calendar getCalendarService() throws GeneralSecurityException, IOException {
        // Load the service account key JSON file
        FileInputStream serviceAccountStream = new FileInputStream(SERVICE_ACCOUNT_KEY_FILE);


        // Build the service account credentials
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(List.of("https://www.googleapis.com/auth/calendar"))
                .createDelegated("workspace-user@example.com");


        // Construct the Calendar service object
        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)).setApplicationName(APPLICATION_NAME).build();
    }


}


/**Keywords
 calendar-id - retrieved from the google calendar where the service account is shared.
 workspace-user@example.com - the mail id with domain wide delegation.
 APPLICATION_NAME - created application name in oAuth consent screen.
 **/
