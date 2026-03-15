package org.example.isc.main.dto.scholarship;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewScheduleForm {

    private List<NewDayForm> days = new ArrayList<>();

    public NewScheduleForm() {
    }

    public List<NewDayForm> getDays() {
        return days;
    }

    public void setDays(List<NewDayForm> days) {
        this.days = days;
    }


}
