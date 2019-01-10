package gov.cdc.ncezid.daas.serdes;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;

public class MessageSerdes<T extends MessageSerdeCompatiple> implements Serializer<T>, Deserializer<T>, Serde<T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void configure(final Map<String, ?> configs, final boolean isKey) {}

    @SuppressWarnings("unchecked")
    public T deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return (T) OBJECT_MAPPER.readValue(data, MessageSerdeCompatiple.class);
        } catch (final IOException e) {
            throw new SerializationException(e);
        }
    }

    public byte[] serialize(final String topic, final T data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (final Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    public void close() {}

    public Serializer<T> serializer() {
        return this;
    }

    public Deserializer<T> deserializer() {
        return this;
    }
}

