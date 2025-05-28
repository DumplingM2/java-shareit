package ru.practicum.shareit.server.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.common.exception.ErrorMessage;

import java.io.IOException;

@JsonTest
@DisplayName("ErrorMessage JSON Serialization Tests")
class ErrorMessageJsonTest {

    @Autowired
    private JacksonTester<ErrorMessage> json;

    @Test
    @DisplayName("should serialize with message and code")
    void testSerialize_WithMessageAndCode() throws IOException {
        String errorMessage = "User not found";
        int responseCode = HttpStatus.NOT_FOUND.value();
        ErrorMessage dto = new ErrorMessage(errorMessage, responseCode);

        JsonContent<ErrorMessage> result = json.write(dto);

        assertThat(result).hasJsonPath("$.error");
        assertThat(result).extractingJsonPathStringValue("$.error").isEqualTo(errorMessage);
        assertThat(result).hasJsonPath("$.responseCode");
        assertThat(result).extractingJsonPathNumberValue("$.responseCode").isEqualTo(responseCode);
    }

    @Test
    @DisplayName("should serialize with empty message")
    void testSerialize_EmptyMessage() throws IOException {
        String errorMessage = "";
        int responseCode = HttpStatus.BAD_REQUEST.value();
        ErrorMessage dto = new ErrorMessage(errorMessage, responseCode);

        JsonContent<ErrorMessage> result = json.write(dto);

        assertThat(result).hasJsonPath("$.error");
        assertThat(result).extractingJsonPathStringValue("$.error").isEqualTo(errorMessage);
        assertThat(result).hasJsonPath("$.responseCode");
        assertThat(result).extractingJsonPathNumberValue("$.responseCode").isEqualTo(responseCode);
    }

    @Test
    @DisplayName("should serialize with null message")
    void testSerialize_NullMessage() throws IOException {
        int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        ErrorMessage dto = new ErrorMessage(null, responseCode);

        JsonContent<ErrorMessage> result = json.write(dto);

        assertThat(result).hasJsonPath("$.error");
        assertThat(result).extractingJsonPathStringValue("$.error").isNull();
        assertThat(result).hasJsonPath("$.responseCode");
        assertThat(result).extractingJsonPathNumberValue("$.responseCode").isEqualTo(responseCode);
    }
}