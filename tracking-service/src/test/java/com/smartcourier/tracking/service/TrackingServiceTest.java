package com.smartcourier.tracking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.smartcourier.tracking.config.StorageProperties;
import com.smartcourier.tracking.dto.DeliveryProofRequest;
import com.smartcourier.tracking.dto.TrackingEventRequest;
import com.smartcourier.tracking.entity.DeliveryProof;
import com.smartcourier.tracking.entity.TrackingEvent;
import com.smartcourier.tracking.exception.ResourceNotFoundException;
import com.smartcourier.tracking.integration.DeliveryEventMessage;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRecordRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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

    @Test
    void saveProofShouldPersistDeliveryProof() {
        DeliveryProofRequest request = new DeliveryProofRequest("SC123", "Priya", "Delivered", "proof.jpg");
        when(deliveryProofRepository.findByTrackingNumber("SC123")).thenReturn(Optional.empty());
        when(deliveryProofRepository.save(org.mockito.ArgumentMatchers.any(DeliveryProof.class))).thenAnswer(invocation -> {
            DeliveryProof proof = invocation.getArgument(0);
            proof.setId(1L);
            return proof;
        });

        var response = trackingService.saveProof(request);

        assertEquals("Priya", response.recipientName());
        assertEquals("SC123", response.trackingNumber());
    }

    @Test
    void getProofShouldThrowWhenMissing() {
        when(deliveryProofRepository.findByTrackingNumber("SC404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> trackingService.getProof("SC404"));
    }

    @Test
    void syncFromDeliveryEventShouldCreateTimelineEntry() {
        DeliveryEventMessage message = new DeliveryEventMessage();
        message.setTrackingNumber("SC123");
        message.setStatus("IN_TRANSIT");
        message.setEventType("DELIVERY_STATUS_UPDATED");
        message.setOccurredAt(java.time.LocalDateTime.now());

        when(trackingEventRepository.save(org.mockito.ArgumentMatchers.any(TrackingEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trackingService.syncFromDeliveryEvent(message);

        org.mockito.Mockito.verify(trackingEventRepository).save(org.mockito.ArgumentMatchers.any(TrackingEvent.class));
    }

    @Test
    void uploadDocumentShouldStoreFileMetadata() throws IOException {
        Path tempDir = Files.createTempDirectory("tracking-test");
        TrackingService localTrackingService = new TrackingService(
                trackingEventRepository,
                documentRecordRepository,
                deliveryProofRepository,
                new StorageProperties(tempDir.toString())
        );
        MockMultipartFile multipartFile = new MockMultipartFile("file", "invoice.txt", "text/plain", "hello".getBytes());
        when(documentRecordRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            com.smartcourier.tracking.entity.DocumentRecord record = invocation.getArgument(0);
            record.setId(9L);
            return record;
        });

        var response = localTrackingService.uploadDocument("SC123", multipartFile);

        assertEquals(9L, response.id());
        assertEquals("SC123", response.trackingNumber());
    }
}
