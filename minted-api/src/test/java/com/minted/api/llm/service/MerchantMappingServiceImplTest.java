package com.minted.api.llm.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.llm.dto.MerchantMappingRequest;
import com.minted.api.llm.dto.MerchantMappingResponse;
import com.minted.api.llm.entity.MerchantCategoryMapping;
import com.minted.api.llm.repository.MerchantCategoryMappingRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantMappingServiceImplTest {

    @Mock private MerchantCategoryMappingRepository mappingRepository;
    @Mock private TransactionCategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private MerchantMappingServiceImpl merchantMappingService;

    // ── getMappings ───────────────────────────────────────────────────────────

    @Test
    void getMappings_returnsList() {
        MerchantCategoryMapping mapping = buildMapping(1L, "amazon,flipkart");
        when(mappingRepository.findByUserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(mapping));

        List<MerchantMappingResponse> result = merchantMappingService.getMappings(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).snippets()).isEqualTo("amazon,flipkart");
    }

    // ── createMapping ─────────────────────────────────────────────────────────

    @Test
    void createMapping_success() {
        User user = buildUser(1L);
        TransactionCategory category = buildCategory(1L);
        MerchantCategoryMapping saved = buildMapping(10L, "amazon");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));
        when(mappingRepository.save(any(MerchantCategoryMapping.class))).thenReturn(saved);

        MerchantMappingResponse response = merchantMappingService.createMapping(
                new MerchantMappingRequest("amazon", 1L), 1L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void createMapping_userNotFound_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> merchantMappingService.createMapping(
                new MerchantMappingRequest("amazon", 1L), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateMapping ─────────────────────────────────────────────────────────

    @Test
    void updateMapping_success() {
        MerchantCategoryMapping existing = buildMapping(1L, "amazon");
        TransactionCategory category = buildCategory(1L);
        when(mappingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));
        when(mappingRepository.save(existing)).thenReturn(existing);

        MerchantMappingResponse response = merchantMappingService.updateMapping(1L,
                new MerchantMappingRequest("amazon,flipkart", 1L), 1L);

        assertThat(existing.getSnippets()).isEqualTo("amazon,flipkart");
    }

    @Test
    void updateMapping_notFound_throwsResourceNotFound() {
        when(mappingRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> merchantMappingService.updateMapping(99L,
                new MerchantMappingRequest("amazon", 1L), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteMapping ─────────────────────────────────────────────────────────

    @Test
    void deleteMapping_success() {
        MerchantCategoryMapping mapping = buildMapping(1L, "amazon");
        when(mappingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mapping));

        merchantMappingService.deleteMapping(1L, 1L);

        verify(mappingRepository).delete(mapping);
    }

    // helpers

    private MerchantCategoryMapping buildMapping(Long id, String snippets) {
        MerchantCategoryMapping m = new MerchantCategoryMapping();
        m.setId(id);
        m.setSnippets(snippets);
        m.setCategory(buildCategory(1L));
        return m;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }

    private TransactionCategory buildCategory(Long id) {
        TransactionCategory c = new TransactionCategory();
        c.setId(id);
        c.setName("Shopping");
        c.setType(TransactionType.EXPENSE);
        return c;
    }
}
