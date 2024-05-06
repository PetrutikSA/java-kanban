package handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        String localDateTimeString = (localDateTime != null) ? localDateTime.format(DATE_TIME_FORMATTER) : null;
        jsonWriter.value(localDateTimeString);
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String localDateTimeString = jsonReader.nextString();
        return  (localDateTimeString.isBlank() || localDateTimeString.isEmpty()) ? null : LocalDateTime.parse(localDateTimeString, DATE_TIME_FORMATTER);
    }
}