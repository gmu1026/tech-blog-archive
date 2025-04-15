package net.cproduction.techblogarchive.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String id;
    private String type;
    private String title;
    private String content;
    private int postCount;
    private Map<String, Object> data;
    private Instant timestamp;
}
