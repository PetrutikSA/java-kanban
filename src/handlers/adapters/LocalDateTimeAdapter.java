package handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        String s = (localDateTime != null) ? localDateTime.format(formatter) : null;
        jsonWriter.value(s);
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String s = jsonReader.nextString();
        return  (s.isBlank() || s.isEmpty()) ? null : LocalDateTime.parse(s, formatter);
    }
}