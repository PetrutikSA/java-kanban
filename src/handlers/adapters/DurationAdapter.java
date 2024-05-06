package handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        String durationString = (duration == null) ? null : String.valueOf(duration.toMinutes());
        jsonWriter.value(durationString);
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String durationString = jsonReader.nextString();
        return (durationString.isEmpty() | durationString.isBlank()) ?
                null : Duration.ofMinutes(Long.parseLong(durationString));
    }
}