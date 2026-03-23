package com.smartcourier.tracking.service;

import com.smartcourier.tracking.config.StorageProperties;
import com.smartcourier.tracking.dto.DeliveryProofRequest;
import com.smartcourier.tracking.dto.DeliveryProofResponse;
import com.smartcourier.tracking.dto.DocumentUploadResponse;
import com.smartcourier.tracking.dto.TrackingEventRequest;
import com.smartcourier.tracking.dto.TrackingEventResponse;
import com.smartcourier.tracking.entity.DeliveryProof;
import com.smartcourier.tracking.entity.DocumentRecord;
import com.smartcourier.tracking.entity.TrackingEvent;
import com.smartcourier.tracking.exception.ResourceNotFoundException;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRecordRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrackingService {

    private final TrackingEventRepository trackingEventRepository;
    private final DocumentRecordRepository documentRecordRepository;
    private final DeliveryProofRepository deliveryProofRepository;
    private final StorageProperties storageProperties;

    public TrackingService(
            TrackingEventRepository trackingEventRepository,
            DocumentRecordRepository documentRecordRepository,
            DeliveryProofRepository deliveryProofRepository,
            StorageProperties storageProperties
    ) {
        this.trackingEventRepository = trackingEventRepository;
        this.documentRecordRepository = documentRecordRepository;
        this.deliveryProofRepository = deliveryProofRepository;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public TrackingEventResponse addEvent(TrackingEventRequest request) {
        TrackingEvent event = new TrackingEvent();
        event.setTrackingNumber(request.trackingNumber());
        event.setStatus(request.status());
        event.setLocation(request.location());
        event.setDescription(request.description());
        return toResponse(trackingEventRepository.save(event));
    }

    public List<TrackingEventResponse> getTimeline(String trackingNumber) {
        return trackingEventRepository.findByTrackingNumberOrderByEventTimeDesc(trackingNumber).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DocumentUploadResponse uploadDocument(String trackingNumber, MultipartFile file) throws IOException {
        Path uploadRoot = Path.of(storageProperties.uploadDir());
        Files.createDirectories(uploadRoot);
        String storedName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path targetPath = uploadRoot.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        DocumentRecord documentRecord = new DocumentRecord();
        documentRecord.setTrackingNumber(trackingNumber);
        documentRecord.setFileName(file.getOriginalFilename());
        documentRecord.setStoredPath(targetPath.toString());
        documentRecord.setContentType(file.getContentType());
        DocumentRecord saved = documentRecordRepository.save(documentRecord);
        return new DocumentUploadResponse(saved.getId(), saved.getTrackingNumber(), saved.getFileName(), saved.getStoredPath(), saved.getUploadedAt());
    }

    @Transactional
    public DeliveryProofResponse saveProof(DeliveryProofRequest request) {
        DeliveryProof proof = deliveryProofRepository.findByTrackingNumber(request.trackingNumber()).orElseGet(DeliveryProof::new);
        proof.setTrackingNumber(request.trackingNumber());
        proof.setRecipientName(request.recipientName());
        proof.setProofNote(request.proofNote());
        proof.setProofImagePath(request.proofImagePath());
        return toResponse(deliveryProofRepository.save(proof));
    }

    public DeliveryProofResponse getProof(String trackingNumber) {
        return deliveryProofRepository.findByTrackingNumber(trackingNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery proof not found"));
    }

    private TrackingEventResponse toResponse(TrackingEvent event) {
        return new TrackingEventResponse(event.getId(), event.getTrackingNumber(), event.getStatus(), event.getLocation(), event.getDescription(), event.getEventTime());
    }

    private DeliveryProofResponse toResponse(DeliveryProof proof) {
        return new DeliveryProofResponse(proof.getId(), proof.getTrackingNumber(), proof.getRecipientName(), proof.getProofNote(), proof.getProofImagePath(), proof.getDeliveredAt());
    }
}
