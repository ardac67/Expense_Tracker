package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateTimeBuilder {
    private DateTimeFormatterBuilder dateTimeFormatterBuilder;
    private DateTimeFormatter dateTimeFormatter;

    // Constructor for the DateTimeBuilder class
    public DateTimeBuilder() {
        // Create a DateTimeFormatterBuilder and add a pattern for parsing the date in the format "[yyyy-MM-dd]"
        dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern("[yyyy-MM-dd]"));
        // Create a DateTimeFormatter from the DateTimeFormatterBuilder
        dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
    }

    // Method to parse a date string into a LocalDate object using the defined DateTimeFormatter
    public LocalDate returnFormattedDate(String date) {
        // Parse the date string and return the corresponding LocalDate object
        LocalDate dateStart = LocalDate.parse(date, dateTimeFormatter);
        return dateStart;
    }
}
