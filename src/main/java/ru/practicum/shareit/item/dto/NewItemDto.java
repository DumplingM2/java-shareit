package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.ItemStatus;

@Data
public class NewItemDto {

    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    @NotNull(message = "Item status must be set")
    private ItemStatus status;

    @JsonCreator
    public NewItemDto(@JsonProperty("name") String name,
                      @JsonProperty("description") String description,
                      @JsonProperty("available") Boolean status) {
        this.name = name;
        this.description = description;
        this.status =
                status == null ? null : (status ? ItemStatus.AVAILABLE : ItemStatus.UNAVAILABLE);
    }
}