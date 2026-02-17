package com.minted.api.service.impl;

import com.minted.api.dto.TransactionCategoryRequest;
import com.minted.api.dto.TransactionCategoryResponse;
import com.minted.api.entity.TransactionCategory;
import com.minted.api.entity.User;
import com.minted.api.enums.TransactionType;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.TransactionCategoryRepository;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.TransactionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionCategoryServiceImpl implements TransactionCategoryService {

    private final TransactionCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCategoryResponse> getAllByUserId(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(TransactionCategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCategoryResponse> getAllActiveByUserId(Long userId) {
        return categoryRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(TransactionCategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCategoryResponse> getAllByUserIdAndType(Long userId, TransactionType type) {
        return categoryRepository.findByUserIdAndTypeAndIsActiveTrue(userId, type).stream()
                .map(TransactionCategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionCategoryResponse getById(Long id, Long userId) {
        TransactionCategory category = findCategoryByIdAndUserId(id, userId);
        return TransactionCategoryResponse.from(category);
    }

    @Override
    @Transactional
    public TransactionCategoryResponse create(TransactionCategoryRequest request, Long userId) {
        // Check if category with same name and type already exists for user
        if (categoryRepository.existsByNameAndTypeAndUserId(request.name(), request.type(), userId)) {
            throw new BadRequestException("Category with name '" + request.name() +
                    "' and type '" + request.type() + "' already exists");
        }

        User user = findUserById(userId);
        TransactionCategory parent = null;
        if (request.parentId() != null) {
            parent = findCategoryByIdAndUserId(request.parentId(), userId);
        }

        TransactionCategory category = new TransactionCategory();
        category.setName(request.name());
        category.setType(request.type());
        category.setIcon(request.icon());
        category.setColor(request.color());
        category.setParent(parent);
        category.setUser(user);
        category.setIsActive(true);

        TransactionCategory saved = categoryRepository.save(category);
        return TransactionCategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public TransactionCategoryResponse update(Long id, TransactionCategoryRequest request, Long userId) {
        TransactionCategory category = findCategoryByIdAndUserId(id, userId);

        // Check if name/type is changing and if new combination already exists
        if ((!category.getName().equals(request.name()) || !category.getType().equals(request.type())) &&
                categoryRepository.existsByNameAndTypeAndUserId(request.name(), request.type(), userId)) {
            throw new BadRequestException("Category with name '" + request.name() +
                    "' and type '" + request.type() + "' already exists");
        }

        TransactionCategory parent = null;
        if (request.parentId() != null) {
            parent = findCategoryByIdAndUserId(request.parentId(), userId);
            // Prevent circular reference
            if (parent.getId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
        }

        category.setName(request.name());
        category.setType(request.type());
        category.setIcon(request.icon());
        category.setColor(request.color());
        category.setParent(parent);

        TransactionCategory updated = categoryRepository.save(category);
        return TransactionCategoryResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        TransactionCategory category = findCategoryByIdAndUserId(id, userId);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public void toggleActive(Long id, Long userId) {
        TransactionCategory category = findCategoryByIdAndUserId(id, userId);
        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }

    private TransactionCategory findCategoryByIdAndUserId(Long id, Long userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
