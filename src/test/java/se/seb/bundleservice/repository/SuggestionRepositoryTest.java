package se.seb.bundleservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleName;
import se.seb.bundleservice.model.Suggestion;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;


@SpringBootTest(classes = {SuggestionRepository.class})
class SuggestionRepositoryTest {

    @Autowired
    private SuggestionRepository repository;

    @Test
    void shouldSaveSuggestion() {
        Suggestion suggestion = Suggestion.builder()
                .customerName("Amir")
                .bundle(new Bundle(BundleName.GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .build();
        Suggestion saveSuggestion = repository.saveSuggestion(suggestion);

        assertThat(saveSuggestion).isNotNull();
        assertThat(saveSuggestion.getCustomerName()).isEqualTo("Amir");
        assertThat(saveSuggestion.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(saveSuggestion.getBundle().getName()).isEqualTo(BundleName.GOLD.getLabel());
        assertThat(saveSuggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldRemoveSuggestion() {
        Suggestion suggestion = Suggestion.builder()
                .customerName("Amir")
                .bundle(new Bundle(BundleName.GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .build();
        repository.saveSuggestion(suggestion);
        repository.removeSuggestion("Amir");
        Optional<Suggestion> amir = repository.getSuggestionByCustomerName("Amir");
        assertThat(amir.isEmpty()).isTrue();
    }

    @Test
    void shouldGetSuggestion() {
        Suggestion suggestion = Suggestion.builder()
                .customerName("Amir")
                .bundle(new Bundle(BundleName.GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .build();
        repository.saveSuggestion(suggestion);
        Optional<Suggestion> amir = repository.getSuggestionByCustomerName("Amir");
        assertThat(amir.isPresent()).isTrue();
        assertThat(amir.get().getCustomerName()).isEqualTo("Amir");
        assertThat(amir.get().getBundle().getProducts().size()).isEqualTo(3);
        assertThat(amir.get().getBundle().getName()).isEqualTo(BundleName.GOLD.getLabel());
        assertThat(amir.get().getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }
}