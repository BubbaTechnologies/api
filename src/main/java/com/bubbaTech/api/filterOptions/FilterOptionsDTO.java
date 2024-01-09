package com.bubbaTech.api.filterOptions;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingTag;
import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.user.Gender;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
//@JsonSerialize(using = FilterOptionsSerializer.class)
public class FilterOptionsDTO implements DTO<FilterOptionsDTO> {
    private Map<Gender, Map<ClothType, List<ClothingTag>>> genders;

    public FilterOptionsDTO(Map<Gender, Map<ClothType, List<ClothingTag>>> information) {
        this.genders = information;
    }
}
