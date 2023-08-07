//Matthew Groholski
//May 19th, 2023
//OUTDATED

package com.bubbaTech.api.info;

import com.bubbaTech.api.generic.DTO;
import lombok.Data;

@Data
public class FilterOptionsDTO implements DTO<FilterOptionsDTO> {
    private String[] genders;
    private String[][] types;
    public FilterOptionsDTO() {
        //TODO: Dynamic filters
    }

}
