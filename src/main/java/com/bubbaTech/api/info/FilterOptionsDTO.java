package com.bubbaTech.api.info;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.user.Gender;
import lombok.Data;

@Data
public class FilterOptionsDTO implements DTO<FilterOptionsDTO> {
    private String[] genders;
    private String[][] types;
    public FilterOptionsDTO() {
        genders = new String[]{
                Gender.FEMALE.name(), Gender.MALE.name(), Gender.UNISEX.name()
        };
        types = new String[][]{
                new String[]{
                        ClothType.TOP.name(), ClothType.BOTTOM.name(), ClothType.SHOES.name(), ClothType.UNDERCLOTHING.name(), ClothType.JACKET.name(), ClothType.SKIRT.name(), ClothType.ONE_PIECE.name(), ClothType.ACCESSORY.name(), ClothType.SWIMWEAR.name(), ClothType.DRESS.name()
                },new String[]{
                        ClothType.TOP.name(), ClothType.BOTTOM.name(), ClothType.SHOES.name(), ClothType.UNDERCLOTHING.name(), ClothType.JACKET.name(), ClothType.ACCESSORY.name(), ClothType.SLEEPWEAR.name()
                },new String[]{
                        ClothType.TOP.name(), ClothType.BOTTOM.name(), ClothType.SHOES.name(), ClothType.UNDERCLOTHING.name(), ClothType.JACKET.name(), ClothType.SKIRT.name(), ClothType.ONE_PIECE.name(), ClothType.ACCESSORY.name(), ClothType.SWIMWEAR.name(), ClothType.DRESS.name()
                }
        };
    }

}
