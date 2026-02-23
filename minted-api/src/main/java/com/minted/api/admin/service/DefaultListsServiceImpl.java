package com.minted.api.admin.service;

import com.minted.api.admin.dto.DefaultAccountTypeRequest;
import com.minted.api.admin.dto.DefaultAccountTypeResponse;
import com.minted.api.admin.dto.DefaultCategoryRequest;
import com.minted.api.admin.dto.DefaultCategoryResponse;
import com.minted.api.admin.entity.DefaultAccountType;
import com.minted.api.admin.entity.DefaultCategory;
import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.admin.service.DefaultListsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultListsServiceImpl implements DefaultListsService {

    private final DefaultCategoryRepository categoryRepository;
    private final DefaultAccountTypeRepository accountTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DefaultCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> new DefaultCategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        cat.getIcon(),
                        cat.getType()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DefaultCategoryResponse createCategory(DefaultCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("A default category with this name already exists");
        }

        DefaultCategory category = new DefaultCategory();
        category.setName(request.name());
        category.setIcon(request.icon());
        category.setType(request.type().toUpperCase());

        DefaultCategory saved = categoryRepository.save(category);

        log.info("Default category created: id={}, name={}", saved.getId(), saved.getName());
        return new DefaultCategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getIcon(),
                saved.getType()
        );
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Default category not found");
        }
        categoryRepository.deleteById(id);
        log.info("Default category deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefaultAccountTypeResponse> getAllAccountTypes() {
        return accountTypeRepository.findAll().stream()
                .map(type -> new DefaultAccountTypeResponse(
                        type.getId(),
                        type.getName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DefaultAccountTypeResponse createAccountType(DefaultAccountTypeRequest request) {
        if (accountTypeRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("A default account type with this name already exists");
        }

        DefaultAccountType type = new DefaultAccountType();
        type.setName(request.name());

        DefaultAccountType saved = accountTypeRepository.save(type);

        log.info("Default account type created: id={}, name={}", saved.getId(), saved.getName());
        return new DefaultAccountTypeResponse(
                saved.getId(),
                saved.getName()
        );
    }

    @Override
    @Transactional
    public void deleteAccountType(Long id) {
        if (!accountTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Default account type not found");
        }
        accountTypeRepository.deleteById(id);
        log.info("Default account type deleted: id={}", id);
    }
}
