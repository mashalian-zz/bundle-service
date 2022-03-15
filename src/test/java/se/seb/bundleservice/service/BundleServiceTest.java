package se.seb.bundleservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.seb.bundleservice.exception.UnmatchedConditionsException;
import se.seb.bundleservice.model.Age;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomerBundle;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.ModifyBundleRequest;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.repository.CustomerBundleRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;
import static se.seb.bundleservice.model.Product.STUDENT_ACCOUNT;


@ExtendWith(SpringExtension.class)
@Import(BundleService.class)
class BundleServiceTest {

    @MockBean
    private CustomerBundleRepository customerBundleRepository;

    @Autowired
    private BundleService bundleService;

    @Test
    void shouldSuggestBundleOfJuniorSaver() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.UNDER_AGE, Student.NO, 0);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundle().getName()).isEqualTo("Junior Saver");
        assertThat(bundleResponse.getBundle().getProducts().size()).isEqualTo(1);
        assertThat(bundleResponse.getBundle().getProducts()).containsExactly(JUNIOR_SAVER_ACCOUNT);
    }

    @Test
    void shouldSuggestBundleOfStudent() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.YES, 12000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundle().getName()).isEqualTo("Student");
        assertThat(bundleResponse.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getBundle().getProducts()).containsExactly(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassic() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 12000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundle().getName()).isEqualTo("Classic");
        assertThat(bundleResponse.getBundle().getProducts().size()).isEqualTo(2);
        assertThat(bundleResponse.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassicPlus() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 35000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundle().getName()).isEqualTo("Classic Plus");
        assertThat(bundleResponse.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfGold() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 45000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundle().getName()).isEqualTo("Gold");
        assertThat(bundleResponse.getBundle().getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getBundle().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

//    @Test
//    void shouldThrowUnmatchedConditionsExceptionWhenConditionsAreNotMatched() {
//        String jason = "Jason";
//        QuestionRequest question = new QuestionRequest(jason, 30, Student.NO, 0);
//
//        assertThatExceptionOfType(UnmatchedConditionsException.class)
//                .isThrownBy(() -> bundleService.suggestBundle(question))
//                .withMessage("Conditions are not matched to suggest any bundle!");
//    }


    @Test
    void shouldModifyGoldBundle() {
        String robin = "Robin";

        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(goldBundleResponse.getBundle(), questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CREDIT_CARD));
        CustomerBundle customerBundle = CustomerBundle.builder()
                .customerName(robin)
                .bundleName("Gold")
                .products(List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD))
                .build();
        given(customerBundleRepository.save(any())).willReturn(customerBundle);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundle().getName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfCustomerWantsToHaveTwoAccounts() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(goldBundleResponse.getBundle(), questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CURRENT_ACCOUNT, CREDIT_CARD));

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Having more than one account is not allowed");
    }

    @Test
    void shouldThrowExceptionIfGoldBundleWantsToHaveJuniorOrStudentAccount() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(goldBundleResponse.getBundle(), questionRequest, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Junior Saver Account or Student Account is not acceptable for Gold or classic or classic plus bundle");
    }

//    @Test
//    void shouldThrowNotFoundExceptionIfCustomerDoesNotHaveSuggestionAndWantsToModify() {
//        String amir = "Amir";
//        when(suggestionRepository.getSuggestionByCustomerName(amir)).thenReturn(Optional.empty());
//        ModifyBundleRequest modifySuggestedBundleRequest = new ModifyBundleRequest(amir, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));
//
//        assertThatExceptionOfType(NotFoundException.class)
//                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifySuggestedBundleRequest))
//                .withMessage(String.format("Customer with name %s does not have any suggestion to modify", amir));
//    }

    @Test
    void shouldModifyClassicPlusBundle() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 25000);
        BundleResponse classicBundlePlusResponse = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(classicBundlePlusResponse.getBundle(), questionRequest, List.of(CREDIT_CARD), null);
        CustomerBundle customerBundle = CustomerBundle.builder()
                .customerName(robin)
                .bundleName(classicBundlePlusResponse.getBundle().getName())
                .products(List.of(CURRENT_ACCOUNT, DEBIT_CARD))
                .build();
        given(customerBundleRepository.save(eq(customerBundle))).willReturn(customerBundle);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);


        verify(customerBundleRepository).save(eq(customerBundle));
        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(classicBundlePlusResponse.getBundle().getName());
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfClassicPlusBundleWantsToHaveGoldProducts() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 25000);
        BundleResponse classicPlusBundleResponse = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(classicPlusBundleResponse.getBundle(), questionRequest, List.of(CURRENT_ACCOUNT), List.of(CURRENT_ACCOUNT_PLUS));

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Having products from Gold bundle are not allowed in Classic Plus bundle");
    }

    @Test
    void shouldModifyClassicBundle() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 11000);
        BundleResponse classicBundleResponse = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(classicBundleResponse.getBundle(), questionRequest, List.of(DEBIT_CARD), null);
        CustomerBundle customerBundle = CustomerBundle.builder()
                .customerName(robin)
                .bundleName(classicBundleResponse.getBundle().getName())
                .products(List.of(CURRENT_ACCOUNT))
                .build();
        given(customerBundleRepository.save(eq(customerBundle))).willReturn(customerBundle);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);


        verify(customerBundleRepository).save(eq(customerBundle));
        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(classicBundleResponse.getBundle().getName());
        assertThat(response.getProducts().size()).isEqualTo(1);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT);
    }

    @Test
    void shouldThrowExceptionIfClassicBundleWantsToHaveCreditCard() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 11000);
        BundleResponse classicBundle = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(classicBundle.getBundle(), questionRequest, null, List.of(CREDIT_CARD));

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Having products from Gold bundle or any credit cards are not allowed in Classic bundle");
    }

    @Test
    void shouldModifyStudentBundle() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(studentBundle.getBundle(), questionRequest, List.of(DEBIT_CARD), null);
        CustomerBundle customerBundle = CustomerBundle.builder()
                .customerName(robin)
                .bundleName(studentBundle.getBundle().getName())
                .products(List.of(STUDENT_ACCOUNT, CREDIT_CARD))
                .build();
        given(customerBundleRepository.save(eq(customerBundle))).willReturn(customerBundle);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);


        verify(customerBundleRepository).save(eq(customerBundle));
        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(studentBundle.getBundle().getName());
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfStudentBundleWantsToHaveCurrentAccount() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(studentBundle.getBundle(), questionRequest, List.of(STUDENT_ACCOUNT), List.of(CURRENT_ACCOUNT));

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Having products from Gold bundle or current account are not allowed in Student bundle");
    }

    @Test
    void shouldThrowExceptionIfJuniorBundleWantsToModify() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.UNDER_AGE, Student.NO, 0);
        BundleResponse juniorSaveBundle = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(juniorSaveBundle.getBundle(), questionRequest, null, List.of(DEBIT_CARD));

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Junior Saver cannot do any modification");
    }

    @Test
    void shouldThrowExceptionIfCustomerDoesNotHaveAnyAccountThroughModify() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 18000);
        BundleResponse classicPlusBundle = bundleService.suggestBundle(questionRequest);
        ModifyBundleRequest modifyBundleRequest = new ModifyBundleRequest(classicPlusBundle.getBundle(), questionRequest, List.of(CURRENT_ACCOUNT, CREDIT_CARD), null);

        assertThatExceptionOfType(UnmatchedConditionsException.class)
                .isThrownBy(() -> bundleService.modifySuggestedBundle(modifyBundleRequest))
                .withMessage("Having at least one account is necessary");
    }
}