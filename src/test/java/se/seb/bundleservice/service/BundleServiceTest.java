package se.seb.bundleservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import se.seb.bundleservice.exception.NotFoundException;
import se.seb.bundleservice.exception.UnmatchedConditionsException;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleName;
import se.seb.bundleservice.model.ModifySuggestedBundleRequest;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.model.Suggestion;
import se.seb.bundleservice.repository.SuggestionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.seb.bundleservice.model.BundleName.CLASSIC;
import static se.seb.bundleservice.model.BundleName.CLASSIC_PLUS;
import static se.seb.bundleservice.model.BundleName.GOLD;
import static se.seb.bundleservice.model.BundleName.JUNIOR_SAVER;
import static se.seb.bundleservice.model.BundleName.STUDENT;
import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;
import static se.seb.bundleservice.model.Product.STUDENT_ACCOUNT;


@SpringBootTest
class BundleServiceTest {

    @MockBean
    private SuggestionRepository suggestionRepository;

    @Autowired
    private BundleService bundleService;

    @Test
    void shouldSuggestBundleOfJuniorSaver() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, 15, Student.NO, 0);
        Suggestion mockSuggestion = Suggestion.builder()
                .bundle(new Bundle(JUNIOR_SAVER.getLabel(), List.of(JUNIOR_SAVER_ACCOUNT), 0))
                .customerName(robin)
                .build();

        when(suggestionRepository.saveSuggestion(any(Suggestion.class))).thenReturn(mockSuggestion);

        Suggestion suggestion = bundleService.suggestBundle(question);

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getCustomerName()).isEqualTo(robin);
        assertThat(suggestion.getBundle().getName()).isEqualTo("Junior Saver");
        assertThat(suggestion.getBundle().getProducts().size()).isEqualTo(1);
        assertThat(suggestion.getBundle().getProducts()).containsExactly(JUNIOR_SAVER_ACCOUNT);
    }

    @Test
    void shouldSuggestBundleOfStudent() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, 19, Student.YES, 12000);
        Suggestion mockSuggestion = Suggestion.builder()
                .bundle(new Bundle(STUDENT.getLabel(), List.of(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 0))
                .customerName(robin)
                .build();

        when(suggestionRepository.saveSuggestion(any(Suggestion.class))).thenReturn(mockSuggestion);

        Suggestion suggestion = bundleService.suggestBundle(question);

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getCustomerName()).isEqualTo(robin);
        assertThat(suggestion.getBundle().getName()).isEqualTo("Student");
        assertThat(suggestion.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(suggestion.getBundle().getProducts()).containsExactly(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassic() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, 30, Student.NO, 12000);
        Suggestion mockSuggestion = Suggestion.builder()
                .bundle(new Bundle(CLASSIC.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD), 1))
                .customerName(robin)
                .build();

        when(suggestionRepository.saveSuggestion(any(Suggestion.class))).thenReturn(mockSuggestion);

        Suggestion suggestion = bundleService.suggestBundle(question);

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getCustomerName()).isEqualTo(robin);
        assertThat(suggestion.getBundle().getName()).isEqualTo("Classic");
        assertThat(suggestion.getBundle().getProducts().size()).isEqualTo(2);
        assertThat(suggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
        verify(suggestionRepository).saveSuggestion(any(Suggestion.class));
    }

    @Test
    void shouldSuggestBundleOfClassicPlus() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, 30, Student.NO, 35000);
        Suggestion mockSuggestion = Suggestion.builder()
                .bundle(new Bundle(CLASSIC_PLUS.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 2))
                .customerName(robin)
                .build();

        when(suggestionRepository.saveSuggestion(any(Suggestion.class))).thenReturn(mockSuggestion);

        Suggestion suggestion = bundleService.suggestBundle(question);

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getCustomerName()).isEqualTo(robin);
        assertThat(suggestion.getBundle().getName()).isEqualTo("Classic Plus");
        assertThat(suggestion.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(suggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
        verify(suggestionRepository).saveSuggestion(any(Suggestion.class));
    }

    @Test
    void shouldSuggestBundleOfGold() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, 30, Student.NO, 45000);
        Suggestion mockSuggestion = Suggestion.builder()
                .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .customerName(robin)
                .build();

        when(suggestionRepository.saveSuggestion(any(Suggestion.class))).thenReturn(mockSuggestion);

        Suggestion suggestion = bundleService.suggestBundle(question);

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getCustomerName()).isEqualTo(robin);
        assertThat(suggestion.getBundle().getName()).isEqualTo("Gold");
        assertThat(suggestion.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(suggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
        verify(suggestionRepository).saveSuggestion(any(Suggestion.class));
    }

    @Test
    void shouldThrowUnmatchedConditionsExceptionWhenConditionsAreNotMatched() {
        String jason = "Jason";
        QuestionRequest question = new QuestionRequest(jason, 30, Student.NO, 0);

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.suggestBundle(question))
                .withMessage("Conditions are not matched to suggest any bundle!");
    }

    @Test
    void shouldModifyGoldBundle() {
        String robin = "Robin";
        Suggestion goldSuggestion = Suggestion.builder()
                .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .customerName(robin)
                .build();
        Suggestion modifiedGoldSuggestion = Suggestion.builder()
                .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD), 3))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(goldSuggestion));
        doNothing().when(suggestionRepository).removeSuggestion(eq(robin));
        when(suggestionRepository.saveSuggestion(eq(modifiedGoldSuggestion))).thenReturn(modifiedGoldSuggestion);

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(GOLD_CREDIT_CARD), List.of(CREDIT_CARD));
        Suggestion modifiedSuggestion = bundleService.modifySuggestedBundle(modifySuggestedBundleRequest);

        verify(suggestionRepository).removeSuggestion(eq(robin));
        verify(suggestionRepository).saveSuggestion(eq(modifiedGoldSuggestion));
        assertThat(modifiedSuggestion).isNotNull();
        assertThat(modifiedSuggestion.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(modifiedSuggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfCustomerWantsToHaveTwoAccounts() {
        String robin = "Robin";
        Suggestion goldSuggestion = Suggestion.builder()
                .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(goldSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(GOLD_CREDIT_CARD), List.of(CURRENT_ACCOUNT, CREDIT_CARD));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Having more than one account is not allowed");
    }

    @Test
    void shouldThrowExceptionIfGoldBundleWantsToHaveJuniorOrStudentAccount() {
        String robin = "Robin";
        Suggestion goldSuggestion = Suggestion.builder()
                .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(goldSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Junior Saver Account or Student Account is not acceptable for Gold or classic or classic plus bundle");
    }

    @Test
    void shouldThrowNotFoundExceptionIfCustomerDoesNotHaveSuggestionAndWantsToModify() {
        String amir = "Amir";
        when(suggestionRepository.getSuggestionByCustomerName(amir)).thenReturn(Optional.empty());
        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(amir, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage(String.format("Customer with name %s does not have any suggestion to modify", amir));
    }

    @Test
    void shouldModifyClassicPlusBundle() {
        String robin = "Robin";
        Suggestion classicPlusSuggestion = Suggestion.builder()
                .bundle(new Bundle(CLASSIC_PLUS.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 2))
                .customerName(robin)
                .build();
        Suggestion modifiedClassicPlus = Suggestion.builder()
                .bundle(new Bundle(CLASSIC_PLUS.getLabel(), List.of(CURRENT_ACCOUNT, CREDIT_CARD), 2))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(classicPlusSuggestion));
        doNothing().when(suggestionRepository).removeSuggestion(eq(robin));
        when(suggestionRepository.saveSuggestion(eq(modifiedClassicPlus))).thenReturn(modifiedClassicPlus);

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(DEBIT_CARD), null);
        Suggestion modifiedSuggestion = bundleService.modifySuggestedBundle(modifySuggestedBundleRequest);

        verify(suggestionRepository).removeSuggestion(eq(robin));
        verify(suggestionRepository).saveSuggestion(eq(modifiedClassicPlus));
        assertThat(modifiedSuggestion).isNotNull();
        assertThat(modifiedSuggestion.getBundle().getProducts().size()).isEqualTo(2);
        assertThat(modifiedSuggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT, CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfClassicPlusBundleWantsToHaveGoldProducts() {
        String robin = "Robin";
        Suggestion classicPlusSuggestion = Suggestion.builder()
                .bundle(new Bundle(CLASSIC_PLUS.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 2))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(classicPlusSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(CURRENT_ACCOUNT), List.of(CURRENT_ACCOUNT_PLUS));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Having products from Gold bundle are not allowed in Classic Plus bundle");
    }

    @Test
    void shouldModifyClassicBundle() {
        String robin = "Robin";
        Suggestion classicSuggestion = Suggestion.builder()
                .bundle(new Bundle(CLASSIC.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD), 1))
                .customerName(robin)
                .build();
        Suggestion modifiedClassic = Suggestion.builder()
                .bundle(new Bundle(CLASSIC.getLabel(), List.of(CURRENT_ACCOUNT), 1))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(classicSuggestion));
        doNothing().when(suggestionRepository).removeSuggestion(eq(robin));
        when(suggestionRepository.saveSuggestion(eq(modifiedClassic))).thenReturn(modifiedClassic);

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(DEBIT_CARD), null);
        Suggestion modifiedSuggestion = bundleService.modifySuggestedBundle(modifySuggestedBundleRequest);

        verify(suggestionRepository).removeSuggestion(eq(robin));
        verify(suggestionRepository).saveSuggestion(eq(modifiedClassic));
        assertThat(modifiedSuggestion).isNotNull();
        assertThat(modifiedSuggestion.getBundle().getProducts().size()).isEqualTo(1);
        assertThat(modifiedSuggestion.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT);
    }

    @Test
    void shouldThrowExceptionIfClassicBundleWantsToHaveCreditCard() {
        String robin = "Robin";
        Suggestion classicSuggestion = Suggestion.builder()
                .bundle(new Bundle(CLASSIC.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD), 1))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(classicSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, null, List.of(CREDIT_CARD));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Having products from Gold bundle or any credit cards are not allowed in Classic bundle");
    }

    @Test
    void shouldModifyStudentBundle() {
        String robin = "Robin";
        Suggestion studentSuggestion = Suggestion.builder()
                .bundle(new Bundle(STUDENT.getLabel(), List.of(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 0))
                .customerName(robin)
                .build();
        Suggestion modifiedStudent = Suggestion.builder()
                .bundle(new Bundle(STUDENT.getLabel(), List.of(STUDENT_ACCOUNT, CREDIT_CARD), 0))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(studentSuggestion));
        doNothing().when(suggestionRepository).removeSuggestion(eq(robin));
        when(suggestionRepository.saveSuggestion(eq(modifiedStudent))).thenReturn(modifiedStudent);

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(DEBIT_CARD), null);
        Suggestion modifiedSuggestion = bundleService.modifySuggestedBundle(modifySuggestedBundleRequest);

        verify(suggestionRepository).removeSuggestion(eq(robin));
        verify(suggestionRepository).saveSuggestion(eq(modifiedStudent));
        assertThat(modifiedSuggestion).isNotNull();
        assertThat(modifiedSuggestion.getBundle().getProducts().size()).isEqualTo(2);
        assertThat(modifiedSuggestion.getBundle().getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfStudentBundleWantsToHaveCurrentAccount() {
        String robin = "Robin";
        Suggestion studentSuggestion = Suggestion.builder()
                .bundle(new Bundle(STUDENT.getLabel(), List.of(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 0))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(studentSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(STUDENT_ACCOUNT), List.of(CURRENT_ACCOUNT));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Having products from Gold bundle or current account are not allowed in Student bundle");
    }

    @Test
    void shouldThrowExceptionIfJuniorBundleWantsToModify() {
        String robin = "Robin";
        Suggestion juniorSuggestion = Suggestion.builder()
                .bundle(new Bundle(JUNIOR_SAVER.getLabel(), List.of(JUNIOR_SAVER_ACCOUNT), 0))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(juniorSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(STUDENT_ACCOUNT), List.of(CURRENT_ACCOUNT));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Junior Saver cannot do any modification");
    }


    @Test
    void shouldThrowExceptionIfCustomerDoesNotHaveAnyAccountThroughModify() {
        String robin = "Robin";
        Suggestion juniorSuggestion = Suggestion.builder()
                .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3))
                .customerName(robin)
                .build();
        when(suggestionRepository.getSuggestionByCustomerName(eq(robin))).thenReturn(Optional.of(juniorSuggestion));

        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(robin, List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD), List.of(CREDIT_CARD));
        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
                .withMessage("Having at least one account is necessary");
    }
}