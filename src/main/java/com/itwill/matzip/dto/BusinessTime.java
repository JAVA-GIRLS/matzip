package com.itwill.matzip.dto;


import com.itwill.matzip.domain.BusinessHour;
import com.itwill.matzip.domain.Restaurant;
import com.itwill.matzip.domain.enums.BusinessDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessTime {
    Boolean isHoliday;
    Integer startHour;
    Integer startMinute;
    Integer endHour;
    Integer endMinute;
    String day;

    private String getStartTime() {
        return startHour + ":" + String.format("%02d", startMinute);}

    private String getEndTime() {
        return endHour + ":" + String.format("%02d", endMinute);
    }

    public BusinessHour toEntity(Restaurant restaurant) {

        if (!isHoliday) {
            return BusinessHour.builder()
                    .restaurant(restaurant)
                    .isHoliday(isHoliday)
                    .openTime(getStartTime())
                    .closeTime(getEndTime())
                    .days(BusinessDay.valueOf(day.trim()))
                    .build();
        } else {
            return BusinessHour.builder()
                    .isHoliday(isHoliday)
                    .restaurant(restaurant)
                    .days(BusinessDay.valueOf(day.trim()))
                    .build();
        }
    }

}
