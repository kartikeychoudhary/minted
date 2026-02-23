package com.minted.api.llm.service;

import com.minted.api.llm.dto.MerchantMappingRequest;
import com.minted.api.llm.dto.MerchantMappingResponse;
import com.minted.api.llm.entity.MerchantCategoryMapping;

import java.util.List;

public interface MerchantMappingService {

    List<MerchantMappingResponse> getMappings(Long userId);

    MerchantMappingResponse createMapping(MerchantMappingRequest request, Long userId);

    MerchantMappingResponse updateMapping(Long id, MerchantMappingRequest request, Long userId);

    void deleteMapping(Long id, Long userId);

    List<MerchantCategoryMapping> getRawMappings(Long userId);
}
