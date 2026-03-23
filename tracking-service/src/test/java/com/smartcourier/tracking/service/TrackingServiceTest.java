package com.smartcourier.tracking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.smartcourier.tracking.config.StorageProperties;
import com.smartcourier.tracking.dto.TrackingEventRequest;
import com.smartcourier.tracking.entity.TrackingEvent;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRecordRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private TrackingEventRepository trackingEventRepository;
    @Mock
    private DocumentRecordRepository documentRecordRepository;
    @Mock
    private DeliveryProofRepository deliveryProofRepository;

    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new TrackingService(
                trackingEventRepository,
                documentRecordRepository,
                deliveryProofRepository,
                new StorageProperties("uploads/test")
        );
    }

    @Test
    void addEventShouldPersistStatusUpdate() {
        TrackingEventRequest request = new TrackingEventRequest("SC123", "IN_TRANSIT", "Delhi Hub", "Parcel moved to sort center");
        when(trackingEventRepository.save(org.mockito.ArgumentMatchers.any(TrackingEvent.class))).thenAnswer(invocation -> {
            TrackingEvent event = invocation.getArgument(0);
            event.setId(1L);
            return event;
        });

        var response = trackingService.addEvent(request);

        assertEquals("IN_TRANSIT", response.status());
        assertEquals("Delhi Hub", response.location());
    }
}

