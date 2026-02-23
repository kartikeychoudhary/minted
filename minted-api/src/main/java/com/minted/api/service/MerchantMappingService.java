package com.minted.api.service;

import com.minted.api.dto.MerchantMappingRequest;
import com.minted.api.dto.MerchantMappingResponse;
import com.minted.api.entity.MerchantCategoryMapping;

import java.util.List;

public interface MerchantMappingService {

    List<MerchantMappingResponse> getMappings(Long userId);

    MerchantMappingResponse createMapping(MerchantMappingRequest request, Long userId);

    MerchantMappingResponse updateMapping(Long id, MerchantMappingRequest request, Long userId);

    void deleteMapping(Long id, Long userId);

    List<MerchantCategoryMapping> getRawMappings(Long userId);
}
