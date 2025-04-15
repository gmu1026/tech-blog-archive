package net.cproduction.techblogarchive.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.cproduction.techblogarchive.model.NotificationEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BlogPostEventConsumer {
    private final NotificationProducer notificationProducer;

    @KafkaListener(topics = "blog-posts", groupId = "blogpost-group")
    public void consumeBlogEvents(ConsumerRecord<String, Object> record) {
        String key = record.key();

        if ("new-posts".equals(key) && record.value() instanceof NotificationEvent event) {
            processNewPostsEvent(event);
        }
    }

    private void processNewPostsEvent(NotificationEvent event) {
        NotificationEvent notification = new NotificationEvent();
        notification.setId(UUID.randomUUID().toString());
        notification.setType("NEW_POSTS");
        notification.setTitle("새로운 기술 블로그 글이 등록되었습니다.");
        notification.setTimestamp(event.getTimestamp());

        notificationProducer.broadcastNotification(notification);
    }
}
