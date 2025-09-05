package com.dev.hotelbooking.event.listener;

import com.dev.hotelbooking.event.UserEvent;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final IEmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent userEvent) {
        User user = userEvent.getUser();

        switch (userEvent.getEventType()) {
            case REGISTRATION -> emailService.sendNewAccountEmail(user.getFirstName(), user.getEmail(),
                    userEvent.getEventDetails().get("key").toString());
            case RESETPASSWORD -> emailService.sendResetPasswordEmail(user.getFirstName(), user.getEmail(),
                    userEvent.getEventDetails().get("key").toString());
            default -> {}
        }
    }
}
