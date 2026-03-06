package com.minted.api.transaction.service;

import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.transaction.dto.TransactionCategoryRequest;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceImplTest {

    @Mock TransactionCategoryRepository categoryRepository;
    @Mock UserRepository userRepository;
    @Mock DefaultCategoryRepository defaultCategoryRepository;

    @InjectMocks TransactionCategoryServiceImpl categoryService;

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_duplicateNameAndType_throwsDuplicate() {
        when(categoryRepository.existsByNameAndTypeAndUserId("Food", TransactionType.EXPENSE, 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(
                new TransactionCategoryRequest("Food", TransactionType.EXPENSE, "icon", "#fff", null), 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_success() {
        when(categoryRepository.existsByNameAndTypeAndUserId("Food", TransactionType.EXPENSE, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));

        TransactionCategory saved = buildCategory(1L, "Food", TransactionType.EXPENSE, false);
        when(categoryRepository.save(any(TransactionCategory.class))).thenReturn(saved);

        var response = categoryService.create(
                new TransactionCategoryRequest("Food", TransactionType.EXPENSE, "icon", "#fff", null), 1L);

        assertThat(response.name()).isEqualTo("Food");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_defaultCategory_throwsBadRequest() {
        TransactionCategory defaultCat = buildCategory(1L, "Food", TransactionType.EXPENSE, true);
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(defaultCat));

        assertThatThrownBy(() -> categoryService.update(1L,
                new TransactionCategoryRequest("NewFood", TransactionType.EXPENSE, null, null, null), 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be modified");
    }

    @Test
    void update_selfParent_throwsBadRequest() {
        TransactionCategory cat = buildCategory(1L, "Food", TransactionType.EXPENSE, false);
        TransactionCategory parent = buildCategory(1L, "Food", TransactionType.EXPENSE, false); // same id
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(cat))
                .thenReturn(Optional.of(parent));

        // parentId = same id = 1L
        assertThatThrownBy(() -> categoryService.update(1L,
                new TransactionCategoryRequest("Food", TransactionType.EXPENSE, null, null, 1L), 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own parent");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_defaultCategory_throwsBadRequest() {
        TransactionCategory defaultCat = buildCategory(1L, "Food", TransactionType.EXPENSE, true);
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(defaultCat));

        assertThatThrownBy(() -> categoryService.delete(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be deleted");
    }

    @Test
    void delete_success() {
        TransactionCategory cat = buildCategory(1L, "Food", TransactionType.EXPENSE, false);
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(cat));

        categoryService.delete(1L, 1L);

        verify(categoryRepository).delete(cat);
    }

    // ── getById ────────────────────────────────────────────────────────────────

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(categoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // helpers

    private TransactionCategory buildCategory(Long id, String name, TransactionType type, boolean isDefault) {
        TransactionCategory c = new TransactionCategory();
        c.setId(id);
        c.setName(name);
        c.setType(type);
        c.setIsActive(true);
        c.setIsDefault(isDefault);
        return c;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }
}
