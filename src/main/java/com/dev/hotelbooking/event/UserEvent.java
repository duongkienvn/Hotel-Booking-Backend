package com.dev.hotelbooking.event;

import com.dev.hotelbooking.enums.EventType;
import com.dev.hotelbooking.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@AllArgsConstructor
@Getter
@Setter
public class UserEvent {
    private User user;
    private EventType eventType;
    private Map<?, ?> eventDetails;
}
