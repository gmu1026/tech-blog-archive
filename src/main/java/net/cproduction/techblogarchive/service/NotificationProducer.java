package net.cproduction.techblogarchive.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.model.NotificationEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationProducer {
    private final Map<String, SseEmitter> connectClients = new ConcurrentHashMap<>();
    private static final String TOPIC = "blog-posts";

    public SseEmitter subscribe(String clientId) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitter.onCompletion(() -> connectClients.remove(clientId));
        sseEmitter.onTimeout(() -> connectClients.remove(clientId));
        sseEmitter.onError(e -> connectClients.remove(clientId));

        connectClients.put(clientId, sseEmitter);

        return sseEmitter;
    }

    public void broadcastNotification(NotificationEvent event) {
        List<String> deadEmitters = new ArrayList<>();

        connectClients.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(event)
                        .id(event.getId())
                );
            } catch (IOException e) {
                deadEmitters.add(clientId);
            }
        });

        deadEmitters.forEach(connectClients::remove);
    }
}
