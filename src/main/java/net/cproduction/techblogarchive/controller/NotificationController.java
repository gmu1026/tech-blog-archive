package net.cproduction.techblogarchive.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.service.NotificationProducer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@RestController
public class NotificationController {
    private final NotificationProducer notificationProducer;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(defaultValue = "anonymous") String clientId) {
        log.info("SSE Subscribe requested clientId={}", clientId);
        return notificationProducer.subscribe(clientId + "-" + UUID.randomUUID().toString());
    }
}
