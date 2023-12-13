package com.bubbaTech.api.app.responseObjects.clothingListResponse;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description: Provides a data structure for the /likes routes.
 */
@Data
@AllArgsConstructor
@JsonSerialize(using=ClothingListResponseSerializer.class)
public class ClothingListResponse {
    private List<ClothingDTO> clothingList;
    private Long totalPageCount;
}
