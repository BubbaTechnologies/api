package com.bubbaTech.api.app.responseObjects.clothingListResponse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description: Provides a data structure for the /likes routes.
 */
@Data
@AllArgsConstructor
@JsonSerialize(using= ActivityListResponseSerializer.class)
public class ActivityListResponse {
    private List<ActivityLikeDTO> activityList;
    private Long totalPageCount;
}
