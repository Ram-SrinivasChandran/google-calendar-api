# Google Calendar API Integration with FLBC using Java

## Introduction
Integrating the Google Calendar API into FLBC allows you to automate and manage calendar-related tasks programmatically. This guide will walk you through the steps to set up and authenticate with Google's services using a service account, and interact with the API to create, update, and retrieve calendar events.

**Key Benefits:**
- **Automation:** Streamline scheduling and event management processes.
- **Integration:** Seamlessly integrate calendar functionalities into FLBC.

Now, let's explore how to set up and utilize the Google Calendar API in FLBC.

## Prerequisites
- A Google Cloud Project
- Access to the Google Admin Console (for domain-wide delegation)
- Java Development Kit (JDK)
- Maven (for dependency management)

## Step 1: Set Up Google Cloud Project and Enable Calendar API

**Create a Google Cloud Project:**
1. Open the Google Cloud Console.
2. Click on the project dropdown and select "New Project".
3. Enter a project name and click "Create".

**Enable Google Calendar API:**
1. With your project selected, go to APIs & Services > Library.
2. Search for "Google Calendar API" and click on it.
3. Click "Enable".

## Step 2: Create an Application

1. **Navigate to Google Cloud Console:** Access the Google Cloud Console.
2. **Select Your Project:** Choose the project where you want to configure the OAuth consent screen or create a new project.
3. **Open OAuth Consent Screen Settings:** Go to APIs & Services > OAuth consent screen.
4. **Choose User Type:** Specify whether your application is for internal use only or will be published publicly.
5. **Configure Consent Screen Details:** Provide basic information such as user support email.
6. **Add OAuth Scopes:** Define the OAuth scopes your application will request to access Google APIs.
7. **Save and Continue:** Review details and save your configuration.

## Step 3: Create a Service Account

**Create Service Account:**
1. Go to IAM & Admin > Service Accounts.
2. Click `+ CREATE SERVICE ACCOUNT`.
3. Provide a name, ID, and description for the service account, then click `CREATE AND CONTINUE`.

**Grant Roles to the Service Account:**
1. Assign Role as Compute Admin.
2. Click `DONE`.

**Create and Download the Service Account Key:**
1. Go to the Keys tab of your service account.
2. Click `Add Key > Create New Key`.
3. Choose JSON and download the key file. Store it securely.

## Step 4: Enable Domain-Wide Delegation 

1. **Access the Google Admin Console.**
2. **Navigate to Security Settings:**
   - Go to Security > API controls.
   - Click Manage domain-wide delegation.
3. **Add New API Client:**
   - Click `Add new` and enter the Client ID from your service account JSON file.
   - In the OAuth Scopes field, enter the following scopes:
     ```
     https://www.googleapis.com/auth/calendar
     https://www.googleapis.com/auth/calendar.events
     ```

## Step 5: Implement the Service Account Code in Java

### Project Setup

**Set Up Your Development Environment:**
Create a new Maven project or update your existing `pom.xml` with the following dependencies:

```xml
<!-- Google API Client Library -->
<dependency>
   <groupId>com.google.api-client</groupId>
   <artifactId>google-api-client</artifactId>
   <version>1.35.0</version>
</dependency>

<!-- Google Calendar API Services -->
<dependency>
   <groupId>com.google.apis</groupId>
   <artifactId>google-api-services-calendar</artifactId>
   <version>v3-rev411-1.25.0</version>
</dependency>

<!-- Google Auth Library for OAuth2 -->
<dependency>
   <groupId>com.google.auth</groupId>
   <artifactId>google-auth-library-oauth2-http</artifactId>
   <version>1.23.0</version>
</dependency>
```
1.google-api-client: Provides core functionality for making API requests and handling responses.
2.google-api-services-calendar: Official Google Calendar API client library.
3.google-auth-library-oauth2-http: Google Auth Library for OAuth2, used for authentication with Google services.
### Code Implementation
Authenticate and Make API Requests:
- Use the following Java code to authenticate and interact with the Google Calendar API:

```java

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
```

# Limitation

## Service Account as Organizer:

When using a service account to create events through the Google Calendar API, the service account itself acts as the organizer of those events. This means:

- Events created through the service account will have the service account's email address (or its associated Google Workspace domain) as the organizer.
- Other users invited to these events will be considered guests rather than organizers.

## Guest Status for Other Users:

Users invited to events created by a service account will be treated as guests, regardless of their access level or permissions within the organization. Guests typically have limited capabilities compared to organizers, such as restricted editing rights or access to event details.

## Event Ownership and Retrieval:

When events are created using a service account, the service account becomes the organizer of all events. This means that events created by the service account cannot be retrieved or managed by individual users as their own events. Users invited to these events will only be considered guests, which limits their ability to view or modify the event details. As a result, it is not possible to retrieve events for specific users if every event is created under the service account's email address.

## Conclusion:

Integrating the Google Calendar API into FLBC empowers you to leverage powerful scheduling and event management functionalities. By following the steps outlined in this guide, you can effectively harness the capabilities of the API to automate tasks and manage events.

## Key Takeaways:

- **Setup and Authentication:** Create a Google Cloud Project, set up a service account, and enable necessary API access.
- **Dependency Management:** Include essential Maven dependencies for seamless integration.
- **API Usage:** Write Java code to authenticate with Google's services and interact with the Google Calendar API to perform operations like event creation, retrieval, and updates.

By leveraging the Google Calendar API, you can enhance the functionality of FLBC, streamline workflows, and provide users with powerful scheduling capabilities directly within your software.

