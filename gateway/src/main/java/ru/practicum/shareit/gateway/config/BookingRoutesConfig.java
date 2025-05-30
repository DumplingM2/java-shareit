package ru.practicum.shareit.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.enums.BookingState;
import ru.practicum.shareit.gateway.validation.DtoValidator;
import ru.practicum.shareit.gateway.validation.HeaderValidationFilter;
import ru.practicum.shareit.gateway.validation.QueryParamValidationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BookingRoutesConfig {

    private final DtoValidator dtoValidator;
    private final HeaderValidationFilter headerValidationFilter;
    private final QueryParamValidationFilter queryParamValidationFilter;

    @Value("${shareit-server.url}")
    private String serverUri;

    private static final String BOOKINGS_PATH = "/bookings";
    private static final String BOOKINGS_ID_PATH = BOOKINGS_PATH + "/{bookingId}";
    private static final String BOOKINGS_OWNER_PATH = BOOKINGS_PATH + "/owner";

    @Bean
    public RouteLocator bookingRoutes(RouteLocatorBuilder builder) {
        log.info("Configuring routes for Booking service at URI: {}", serverUri);

        return builder.routes()
                // Route: POST /bookings -> Create Booking
                .route("create_booking", r -> r
                        .path(BOOKINGS_PATH)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .modifyRequestBody(
                                        NewBookingDto.class, NewBookingDto.class,
                                        (exchange, dto) -> {
                                            log.debug("Validating NewBookingDto for POST {}", BOOKINGS_PATH);
                                            dtoValidator.validate(dto);
                                            return Mono.just(dto);
                                        }
                                ))
                        .uri(serverUri))

                // Route: PATCH /bookings/{bookingId}?approved={approved} -> Approve/Reject Booking
                .route("approve_booking", r -> r
                        .path(BOOKINGS_ID_PATH)
                        .and()
                        .method(HttpMethod.PATCH)
                        .and()
                        .query("approved", "true|false")
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                        )
                        .uri(serverUri))

                // Route: GET /bookings/{bookingId} -> Get Booking By ID
                .route("get_booking_by_id", r -> r
                        .path(BOOKINGS_ID_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                        )
                        .uri(serverUri))

                // Route: GET /bookings?state={state}&from={from}&size={size} -> Get Bookings by Booker
                .route("get_bookings_by_booker", r -> r
                        .path(BOOKINGS_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .filter(queryParamValidationFilter.validateOptionalEnumQueryParam(
                                        "state", BookingState.class))
                        )
                        .uri(serverUri))

                // Route: GET /bookings/owner?state={state}&from={from}&size={size} -> Get Bookings by Owner
                .route("get_bookings_by_owner", r -> r
                        .path(BOOKINGS_OWNER_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .filter(queryParamValidationFilter.validateOptionalEnumQueryParam(
                                        "state", BookingState.class))
                        )
                        .uri(serverUri))

                .build();
    }
}