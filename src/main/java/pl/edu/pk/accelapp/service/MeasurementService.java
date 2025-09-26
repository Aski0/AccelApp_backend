package pl.edu.pk.accelapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.repository.MeasurementRepository;

import java.util.List;

@Service
public class MeasurementService {
    private final MeasurementRepository measurementRepository;

    public MeasurementService(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    public Measurement saveMeasurement(Measurement m) {
        return measurementRepository.save(m);
    }

    public List<Measurement> getMeasurementsByFile(Long fileId) {
        return measurementRepository.findByUploadedFileId(fileId);
    }
    public Page<Measurement> getMeasurementsByFilePaged(Long fileId, Pageable pageable) {
        return measurementRepository.findByUploadedFileId(fileId, pageable);
    }

}