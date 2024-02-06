package com.itwill.matzip.dto;

import com.itwill.matzip.domain.Menu;
import com.itwill.matzip.domain.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuToCreate {
    private String name;
    private Long price;

    public Menu toEntity(Restaurant restaurant) {
        return Menu.builder()
                .name(name)
                .price(price)
                .restaurant(restaurant)
                .build();
    }
}
