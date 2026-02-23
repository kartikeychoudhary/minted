package com.minted.api.transaction.service;

import com.minted.api.transaction.dto.TransactionCategoryRequest;
import com.minted.api.transaction.dto.TransactionCategoryResponse;
import com.minted.api.admin.entity.DefaultCategory;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.user.entity.User;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.transaction.service.TransactionCategoryService;
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
    private final DefaultCategoryRepository defaultCategoryRepository;

    @Override
    @Transactional
    public List<TransactionCategoryResponse> getAllByUserId(Long userId) {
        List<TransactionCategoryResponse> userCats = categoryRepository.findByUserId(userId).stream()
                .map(TransactionCategoryResponse::from)
                .collect(Collectors.toList());

        List<TransactionCategoryResponse> defaultCats = defaultCategoryRepository.findAll().stream()
                .map(this::mapDefaultToResponse)
                .collect(Collectors.toList());

        return mergeCategories(userCats, defaultCats, userId);
    }

    @Override
    @Transactional
    public List<TransactionCategoryResponse> getAllActiveByUserId(Long userId) {
        List<TransactionCategoryResponse> userCats = categoryRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(TransactionCategoryResponse::from)
                .collect(Collectors.toList());

        List<TransactionCategoryResponse> defaultCats = defaultCategoryRepository.findAll().stream()
                .map(this::mapDefaultToResponse)
                .collect(Collectors.toList());

        return mergeCategories(userCats, defaultCats, userId);
    }

    @Override
    @Transactional
    public List<TransactionCategoryResponse> getAllByUserIdAndType(Long userId, TransactionType type) {
        List<TransactionCategoryResponse> userCats = categoryRepository.findByUserIdAndTypeAndIsActiveTrue(userId, type).stream()
                .map(TransactionCategoryResponse::from)
                .collect(Collectors.toList());

        List<TransactionCategoryResponse> defaultCats = defaultCategoryRepository.findAll().stream()
                .filter(c -> TransactionType.valueOf(c.getType().toUpperCase()) == type)
                .map(this::mapDefaultToResponse)
                .collect(Collectors.toList());

        return mergeCategories(userCats, defaultCats, userId);
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
            throw new com.minted.api.common.exception.DuplicateResourceException("Category with name '" + request.name() +
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

        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new BadRequestException("Default categories cannot be modified");
        }

        // Check if name/type is changing and if new combination already exists
        if ((!category.getName().equals(request.name()) || !category.getType().equals(request.type())) &&
                categoryRepository.existsByNameAndTypeAndUserId(request.name(), request.type(), userId)) {
            throw new com.minted.api.common.exception.DuplicateResourceException("Category with name '" + request.name() +
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

        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new BadRequestException("Default categories cannot be deleted");
        }

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

    private TransactionCategoryResponse mapDefaultToResponse(DefaultCategory category) {
        return new TransactionCategoryResponse(
                null,
                category.getName(),
                TransactionType.valueOf(category.getType().toUpperCase()),
                category.getIcon(),
                getDefaultColorForCategory(category.getName()),
                null,
                null,
                true,
                true,
                null,
                null
        );
    }

    private List<TransactionCategoryResponse> mergeCategories(List<TransactionCategoryResponse> userCats, List<TransactionCategoryResponse> defaultCats, Long userId) {
        // create unique set based on name AND type
        List<String> existingKeys = userCats.stream()
                .map(c -> c.name().toLowerCase() + "-" + c.type().name().toLowerCase())
                .collect(Collectors.toList());

        User user = findUserById(userId);
        List<TransactionCategoryResponse> merged = new java.util.ArrayList<>(userCats);
        for (TransactionCategoryResponse defCat : defaultCats) {
            String key = defCat.name().toLowerCase() + "-" + defCat.type().name().toLowerCase();
            if (!existingKeys.contains(key)) {
                // Auto-create the default category as a real user category
                TransactionCategory category = new TransactionCategory();
                category.setName(defCat.name());
                category.setType(defCat.type());
                category.setIcon(defCat.icon());
                category.setColor(defCat.color());
                category.setUser(user);
                category.setIsActive(true);
                category.setIsDefault(true);
                TransactionCategory saved = categoryRepository.save(category);
                merged.add(TransactionCategoryResponse.from(saved));
            }
        }
        return merged;
    }

    private String getDefaultColorForCategory(String name) {
        return switch (name) {
            case "Salary" -> "#4CAF50";
            case "Freelance" -> "#8BC34A";
            case "Interest" -> "#CDDC39";
            case "Food & Dining" -> "#FF5722";
            case "Groceries" -> "#FF9800";
            case "Transport" -> "#2196F3";
            case "Utilities" -> "#FFC107";
            case "Entertainment" -> "#9C27B0";
            case "Shopping" -> "#E91E63";
            case "Health" -> "#00BCD4";
            case "Education" -> "#3F51B5";
            case "Rent" -> "#795548";
            case "EMI" -> "#607D8B";
            case "Transfer" -> "#9E9E9E";
            default -> "#607D8B";
        };
    }
}
