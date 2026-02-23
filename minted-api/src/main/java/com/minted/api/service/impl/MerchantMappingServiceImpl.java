package com.minted.api.service.impl;

import com.minted.api.dto.MerchantMappingRequest;
import com.minted.api.dto.MerchantMappingResponse;
import com.minted.api.entity.MerchantCategoryMapping;
import com.minted.api.entity.TransactionCategory;
import com.minted.api.entity.User;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.MerchantCategoryMappingRepository;
import com.minted.api.repository.TransactionCategoryRepository;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.MerchantMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantMappingServiceImpl implements MerchantMappingService {

    private final MerchantCategoryMappingRepository mappingRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MerchantMappingResponse> getMappings(Long userId) {
        return mappingRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(MerchantMappingResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MerchantMappingResponse createMapping(MerchantMappingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TransactionCategory category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        MerchantCategoryMapping mapping = new MerchantCategoryMapping();
        mapping.setUser(user);
        mapping.setSnippets(request.snippets());
        mapping.setCategory(category);

        mapping = mappingRepository.save(mapping);
        return MerchantMappingResponse.from(mapping);
    }

    @Override
    @Transactional
    public MerchantMappingResponse updateMapping(Long id, MerchantMappingRequest request, Long userId) {
        MerchantCategoryMapping mapping = mappingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));
        TransactionCategory category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        mapping.setSnippets(request.snippets());
        mapping.setCategory(category);

        mapping = mappingRepository.save(mapping);
        return MerchantMappingResponse.from(mapping);
    }

    @Override
    @Transactional
    public void deleteMapping(Long id, Long userId) {
        MerchantCategoryMapping mapping = mappingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));
        mappingRepository.delete(mapping);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantCategoryMapping> getRawMappings(Long userId) {
        return mappingRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
