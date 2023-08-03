package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateTimeBuilder {
    private DateTimeFormatterBuilder dateTimeFormatterBuilder;
    private DateTimeFormatter dateTimeFormatter;
    public DateTimeBuilder(){
        dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern("[yyyy-MM-dd]"));
        dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
    }
    public LocalDate returnFormattedDate(String date){
        LocalDate dateStart = LocalDate.parse(date, dateTimeFormatter);
        return dateStart;
    }
}
