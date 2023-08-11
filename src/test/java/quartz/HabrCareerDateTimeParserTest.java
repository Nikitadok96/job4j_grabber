package quartz;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HabrCareerDateTimeParserTest {
    @Test
    public void whenCurrentTimeFormat() {
        String date = "2023-08-10T18:07:38+03:00";
        HabrCareerDateTimeParser timeParser = new HabrCareerDateTimeParser();
        LocalDateTime rsl = timeParser.parse(date);
        assertThat(rsl).isEqualTo("2023-08-10T18:07:38");
    }

    @Test
    public void whenExceptionTimeFormat() {
        String date = "2023-08-10";
        HabrCareerDateTimeParser timeParser = new HabrCareerDateTimeParser();
        DateTimeParseException exception = assertThrows(DateTimeParseException.class, () ->
                timeParser.parse(date));
        assertThat(exception).hasMessageContaining("Text '2023-08-10' could not be parsed");
    }
}