package com.bubbaTech.api.data;


import lombok.Data;

@Data
public class storeStatDTO {
    private String name;
    private Long maleCount;
    private Long femaleCount;
    private Long boyCount;
    private Long girlCount;
    private Long kidCount;
    private Long unisexCount;
    private Long otherCount;
    private Long totalCount;

    public storeStatDTO(String name, Long maleCount, Long femaleCount, Long boyCount, Long girlCount, Long kidCount, Long unisexCount, Long otherCount) {
        this.name = name;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.boyCount = boyCount;
        this.girlCount = girlCount;
        this.kidCount = kidCount;
        this.unisexCount = unisexCount;
        this.totalCount = maleCount + femaleCount + boyCount + girlCount + kidCount + unisexCount;
        this.otherCount = otherCount;
    }
}
