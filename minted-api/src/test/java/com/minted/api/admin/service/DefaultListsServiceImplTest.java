package com.minted.api.admin.service;

import com.minted.api.admin.dto.DefaultAccountTypeRequest;
import com.minted.api.admin.dto.DefaultAccountTypeResponse;
import com.minted.api.admin.dto.DefaultCategoryRequest;
import com.minted.api.admin.dto.DefaultCategoryResponse;
import com.minted.api.admin.entity.DefaultAccountType;
import com.minted.api.admin.entity.DefaultCategory;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultListsServiceImplTest {

    @Mock private DefaultCategoryRepository categoryRepository;
    @Mock private DefaultAccountTypeRepository accountTypeRepository;

    @InjectMocks
    private DefaultListsServiceImpl defaultListsService;

    // ── categories ────────────────────────────────────────────────────────────

    @Test
    void getAllCategories_returnsList() {
        DefaultCategory cat = buildCategory(1L, "Food", "EXPENSE");
        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<DefaultCategoryResponse> result = defaultListsService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Food");
    }

    @Test
    void createCategory_duplicate_throwsDuplicate() {
        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(true);

        assertThatThrownBy(() -> defaultListsService.createCategory(
                new DefaultCategoryRequest("Food", null, "EXPENSE")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createCategory_success() {
        DefaultCategory saved = buildCategory(1L, "Food", "EXPENSE");
        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(saved);

        DefaultCategoryResponse response = defaultListsService.createCategory(
                new DefaultCategoryRequest("Food", null, "EXPENSE"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Food");
    }

    @Test
    void deleteCategory_notFound_throwsResourceNotFound() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> defaultListsService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCategory_success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        defaultListsService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    // ── account types ─────────────────────────────────────────────────────────

    @Test
    void getAllAccountTypes_returnsList() {
        DefaultAccountType type = buildAccountType(1L, "Bank");
        when(accountTypeRepository.findAll()).thenReturn(List.of(type));

        List<DefaultAccountTypeResponse> result = defaultListsService.getAllAccountTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Bank");
    }

    @Test
    void createAccountType_duplicate_throwsDuplicate() {
        when(accountTypeRepository.existsByNameIgnoreCase("Bank")).thenReturn(true);

        assertThatThrownBy(() -> defaultListsService.createAccountType(
                new DefaultAccountTypeRequest("Bank")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createAccountType_success() {
        DefaultAccountType saved = buildAccountType(1L, "Bank");
        when(accountTypeRepository.existsByNameIgnoreCase("Bank")).thenReturn(false);
        when(accountTypeRepository.save(any())).thenReturn(saved);

        DefaultAccountTypeResponse response = defaultListsService.createAccountType(
                new DefaultAccountTypeRequest("Bank"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Bank");
    }

    @Test
    void deleteAccountType_notFound_throwsResourceNotFound() {
        when(accountTypeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> defaultListsService.deleteAccountType(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAccountType_success() {
        when(accountTypeRepository.existsById(1L)).thenReturn(true);

        defaultListsService.deleteAccountType(1L);

        verify(accountTypeRepository).deleteById(1L);
    }

    // helpers

    private DefaultCategory buildCategory(Long id, String name, String type) {
        DefaultCategory c = new DefaultCategory();
        c.setId(id);
        c.setName(name);
        c.setType(type);
        return c;
    }

    private DefaultAccountType buildAccountType(Long id, String name) {
        DefaultAccountType t = new DefaultAccountType();
        t.setId(id);
        t.setName(name);
        return t;
    }
}
