package com.minted.api.service;

import com.minted.api.dto.DefaultAccountTypeRequest;
import com.minted.api.dto.DefaultAccountTypeResponse;
import com.minted.api.dto.DefaultCategoryRequest;
import com.minted.api.dto.DefaultCategoryResponse;

import java.util.List;

public interface DefaultListsService {

    List<DefaultCategoryResponse> getAllCategories();

    DefaultCategoryResponse createCategory(DefaultCategoryRequest request);

    void deleteCategory(Long id);

    List<DefaultAccountTypeResponse> getAllAccountTypes();

    DefaultAccountTypeResponse createAccountType(DefaultAccountTypeRequest request);

    void deleteAccountType(Long id);
}
