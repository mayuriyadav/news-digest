package com.news.digest.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ReadingActivityDTO {

    private Map<String,Long> dailyActivity;   // date -> read count
    private List<CategoryStatDTO> categoryBreakdown;

    @Data
    @AllArgsConstructor
    public static class CategoryStatDTO {
        private String category;
        private Long count;
    }
}
