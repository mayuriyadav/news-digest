package com.news.digest.app.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NotificationSummaryDTO {

    private long unreadCount;
    private long totalCount;
    private List<NotificationDTO> recent; // latest 5 for bell dropdown
}
