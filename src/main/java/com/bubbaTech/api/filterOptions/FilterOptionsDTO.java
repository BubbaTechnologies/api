package com.bubbaTech.api.filterOptions;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingTag;
import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.user.Gender;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FilterOptionsDTO implements DTO<FilterOptionsDTO> {
    private List<Gender> genders;
    private List<List<ClothType>> types;
    private Map<ClothType, List<ClothingTag>> tags;

    public FilterOptionsDTO(List<Gender> genders, List<List<ClothType>> types, Map<ClothType, List<ClothingTag>> tags) {
        this.genders = genders;
        this.types = types;
        this.tags = tags;
    }
}
