package com.minted.api.controller;

import com.minted.api.dto.LlmModelRequest;
import com.minted.api.dto.LlmModelResponse;
import com.minted.api.entity.LlmModel;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.LlmModelRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/llm-models")
@RequiredArgsConstructor
public class AdminLlmModelController {

    private final LlmModelRepository modelRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllModels() {
        List<LlmModelResponse> models = modelRepository.findAllByOrderByIsDefaultDescNameAsc().stream()
                .map(LlmModelResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", models));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createModel(@Valid @RequestBody LlmModelRequest request) {
        LlmModel model = new LlmModel();
        model.setName(request.name());
        model.setProvider(request.provider() != null ? request.provider() : "GEMINI");
        model.setModelKey(request.modelKey());
        model.setDescription(request.description());
        model.setIsActive(request.isActive() != null ? request.isActive() : true);
        model.setIsDefault(request.isDefault() != null ? request.isDefault() : false);

        model = modelRepository.save(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", LlmModelResponse.from(model)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody LlmModelRequest request
    ) {
        LlmModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));

        if (request.name() != null) model.setName(request.name());
        if (request.provider() != null) model.setProvider(request.provider());
        if (request.modelKey() != null) model.setModelKey(request.modelKey());
        if (request.description() != null) model.setDescription(request.description());
        if (request.isActive() != null) model.setIsActive(request.isActive());
        if (request.isDefault() != null) model.setIsDefault(request.isDefault());

        model = modelRepository.save(model);
        return ResponseEntity.ok(Map.of("success", true, "data", LlmModelResponse.from(model)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        LlmModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));
        modelRepository.delete(model);
        return ResponseEntity.noContent().build();
    }
}
