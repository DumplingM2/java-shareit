package ru.practicum.shareit.server.item.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.server.item.Comment;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.user.User;

@DisplayName("Comment Mapper Implementation Tests")
class CommentMapperImplTest {

    private CommentMapperImpl commentMapper;

    @BeforeEach
    void setUp() {
        commentMapper = new CommentMapperImpl();
    }

    @Nested
    @DisplayName("mapToDto Tests")
    class MapToDtoTests {

        private Comment testComment;
        private LocalDateTime testTimestamp;

        @BeforeEach
        void setUpMapToDto() {
            User testAuthor = new User();
            testAuthor.setId(1L);
            testAuthor.setName("Author Name");
            testAuthor.setEmail("author@example.com");

            Item testItem = new Item();
            testItem.setId(10L);
            testItem.setName("Test Item");
            testItem.setDescription("Test Description");
            testItem.setAvailable(true);
            User owner = new User();
            owner.setId(2L);
            testItem.setOwner(owner);

            testTimestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

            testComment = new Comment();
            testComment.setId(100L);
            testComment.setText("This is a test comment.");
            testComment.setItem(testItem);
            testComment.setAuthor(testAuthor);
            testComment.setCreated(testTimestamp);
        }

        @Test
        @DisplayName("should map Comment to CommentDto correctly")
        void mapToDto_whenCommentIsValid_shouldReturnCorrectCommentDto() {
            CommentDto commentDto = commentMapper.mapToDto(testComment);

            assertThat("Mapped CommentDto should not be null", commentDto, is(notNullValue()));
            assertThat("Mapped CommentDto should have correct properties from Comment", commentDto,
                    allOf(
                            hasProperty("id", equalTo(100L)),
                            hasProperty("text", equalTo("This is a test comment.")),
                            hasProperty("authorName", equalTo("Author Name")),
                            hasProperty("created", equalTo(testTimestamp.toString()))
                    )
            );
        }
    }

    @Nested
    @DisplayName("mapToComment Tests")
    class MapToCommentTests {

        @Test
        @DisplayName("should map NewCommentDto to Comment correctly")
        void mapToComment_whenNewCommentDtoIsValid_shouldReturnCorrectComment() {
            NewCommentDto newCommentDto = new NewCommentDto();
            newCommentDto.setText("New comment text.");

            Comment comment = commentMapper.mapToComment(newCommentDto);

            assertThat("Mapped Comment entity should not be null", comment, is(notNullValue()));
            assertThat("Mapped Comment entity should have text from DTO and null other properties",
                    comment,
                    allOf(
                            hasProperty("text", equalTo("New comment text.")),
                            hasProperty("id", is(nullValue())),
                            hasProperty("item", is(nullValue())),
                            hasProperty("author", is(nullValue())),
                            hasProperty("created", is(nullValue()))
                    )
            );
        }

        @Test
        @DisplayName("should handle empty text in NewCommentDto")
        void mapToComment_whenNewCommentDtoHasEmptyText_shouldReturnCommentWithEmptyText() {
            NewCommentDto newCommentDto = new NewCommentDto();
            newCommentDto.setText("");

            Comment comment = commentMapper.mapToComment(newCommentDto);

            assertThat("Mapped Comment entity should not be null", comment, is(notNullValue()));
            assertThat("Mapped Comment entity should have empty text when DTO text is empty", comment,
                    hasProperty("text", equalTo("")));
        }

        @Test
        @DisplayName("should handle null text in NewCommentDto")
        void mapToComment_whenNewCommentDtoHasNullText_shouldReturnCommentWithNullText() {
            NewCommentDto newCommentDto = new NewCommentDto();
            newCommentDto.setText(null);

            Comment comment = commentMapper.mapToComment(newCommentDto);

            assertThat("Mapped Comment entity should not be null", comment, is(notNullValue()));
            assertThat("Mapped Comment entity should have null text when DTO text is null", comment,
                    hasProperty("text", is(nullValue())));
        }
    }
}