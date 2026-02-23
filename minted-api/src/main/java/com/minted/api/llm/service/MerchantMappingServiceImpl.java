package com.minted.api.llm.service;

import com.minted.api.llm.dto.MerchantMappingRequest;
import com.minted.api.llm.dto.MerchantMappingResponse;
import com.minted.api.llm.entity.MerchantCategoryMapping;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.llm.repository.MerchantCategoryMappingRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.llm.service.MerchantMappingService;
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
