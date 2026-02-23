package com.minted.api.admin.service;

import com.minted.api.admin.dto.DefaultAccountTypeRequest;
import com.minted.api.admin.dto.DefaultAccountTypeResponse;
import com.minted.api.admin.dto.DefaultCategoryRequest;
import com.minted.api.admin.dto.DefaultCategoryResponse;

import java.util.List;

public interface DefaultListsService {

    List<DefaultCategoryResponse> getAllCategories();

    DefaultCategoryResponse createCategory(DefaultCategoryRequest request);

    void deleteCategory(Long id);

    List<DefaultAccountTypeResponse> getAllAccountTypes();

    DefaultAccountTypeResponse createAccountType(DefaultAccountTypeRequest request);

    void deleteAccountType(Long id);
}
