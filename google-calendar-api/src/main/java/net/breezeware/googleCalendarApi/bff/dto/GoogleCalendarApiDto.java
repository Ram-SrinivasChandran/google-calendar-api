package net.breezeware.googleCalendarApi.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleCalendarApiDto {

    private String summary;

    private String Location;

    private String description;

    private String eventStartDateTime;

    private String eventEndDateTime;

    private String eventTimeZone;

    private List<String> eventAttendees;

    private boolean meeting;

}
