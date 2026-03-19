package com.virtbank.security;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Masks account numbers in API responses — shows only last 4 digits.
 * Usage: @JsonSerialize(using = AccountNumberSerializer.class)
 */
public class AccountNumberSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value.length() <= 4) {
            gen.writeString(value);
            return;
        }
        String masked = "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
        gen.writeString(masked);
    }
}
