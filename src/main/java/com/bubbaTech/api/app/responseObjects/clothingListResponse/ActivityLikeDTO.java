package com.bubbaTech.api.app.responseObjects.clothingListResponse;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.user.ProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivityLikeDTO {
    private ProfileDTO userProfile;
    private ClothingDTO clothingDTO;
}
