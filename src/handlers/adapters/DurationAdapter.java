package handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        String s = (duration == null) ? null : String.valueOf(duration.toMinutes());
        jsonWriter.value(s);
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String s = jsonReader.nextString();
        return (s.isEmpty() | s.isBlank()) ? null : Duration.ofMinutes(Long.parseLong(s));
    }
}