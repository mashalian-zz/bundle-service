package se.seb.bundleservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.seb.bundleservice.exception.UnmatchedConditionsException;
import se.seb.bundleservice.model.Age;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.seb.bundleservice.model.Bundle.CLASSIC;
import static se.seb.bundleservice.model.Bundle.CLASSIC_PLUS;
import static se.seb.bundleservice.model.Bundle.GOLD;
import static se.seb.bundleservice.model.Bundle.JUNIOR_SAVER;
import static se.seb.bundleservice.model.Bundle.STUDENT;
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

    @Autowired
    private BundleService bundleService;

    @Test
    void shouldSuggestBundleOfJuniorSaver() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.UNDER_AGE, Student.NO, 0);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Junior Saver");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(1);
        assertThat(bundleResponse.getProducts()).containsExactly(JUNIOR_SAVER_ACCOUNT);
    }

    @Test
    void shouldSuggestBundleOfStudent() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.YES, 12000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Student");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getProducts()).containsExactly(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassic() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 12000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Classic");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(2);
        assertThat(bundleResponse.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassicPlus() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 35000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Classic Plus");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfGold() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 45000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Gold");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldNotSuggestAnyBundleIfIncomeIsZero() {
        String robin = "Robin";
        QuestionRequest question = new QuestionRequest(robin, Age.ADULT, Student.NO, 0);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Cannot suggest any bundle");
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
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CREDIT_CARD));


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldNotCustomizeGoldBundleIfCustomerWantsToHaveTwoAccounts() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CURRENT_ACCOUNT, CREDIT_CARD));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getMessage()).isEqualTo("Having more than one account is not allowed");
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts()).isNull();
    }

    @Test
    void shouldSkipAddingProductsIfBundleContains() {
        String robin = "Robin";

        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, null, List.of(GOLD_CREDIT_CARD));


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldSkipRemovingProductsIfBundleDoesNotContains() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(CREDIT_CARD),null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfGoldBundleWantsToHaveJuniorOrStudentAccount() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));

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
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CREDIT_CARD), null);


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(classicBundlePlusResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
    }

    @Test
    void shouldNotCustomizeIfClassicPlusBundleWantsToHaveGoldProducts() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 25000);
        BundleResponse classicPlusBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CURRENT_ACCOUNT), List.of(CURRENT_ACCOUNT_PLUS));

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getMessage()).isEqualTo("Having products from Gold bundle are not allowed in Classic Plus bundle");
        assertThat(response.getBundleName()).isEqualTo(classicPlusBundleResponse.getBundleName());
        assertThat(response.getProducts()).isNull();
    }

    @Test
    void shouldModifyClassicBundle() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 11000);
        BundleResponse classicBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, List.of(DEBIT_CARD), null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(classicBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(1);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT);
    }

    @Test
    void shouldThrowExceptionIfClassicBundleWantsToHaveCreditCard() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 11000);
        BundleResponse classicBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, null, List.of(CREDIT_CARD));

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(classicBundle.getBundleName());
        assertThat(response.getProducts()).isNull();
        assertThat(response.getMessage()).isEqualTo("Having products from Gold bundle or any credit cards are not allowed in Classic bundle");
    }

    @Test
    void shouldModifyStudentBundle() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(DEBIT_CARD), null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);


        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getBundleName()).isEqualTo(studentBundle.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD);
    }

    @Test
    void shouldNotCustomizeIfStudentBundleWantsToHaveCurrentAccount() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(STUDENT_ACCOUNT), List.of(CURRENT_ACCOUNT));

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getMessage()).isEqualTo("Having products from Gold bundle or current account are not allowed in Student bundle");
        assertThat(response.getBundleName()).isEqualTo(studentBundle.getBundleName());
        assertThat(response.getProducts()).isNull();
    }

    @Test
    void shouldNotCustomizeIfJuniorBundleWantsToModify() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.UNDER_AGE, Student.NO, 0);
        BundleResponse juniorSaveBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(JUNIOR_SAVER, questionRequest, null, List.of(DEBIT_CARD));


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getMessage()).isEqualTo("Junior Saver cannot do any modification");
        assertThat(response.getBundleName()).isEqualTo(juniorSaveBundle.getBundleName());
        assertThat(response.getProducts()).isNull();
    }

    @Test
    void shouldNotCustomizeIfCustomerDoesNotHaveAnyAccountThroughModify() {
        String robin = "Robin";
        QuestionRequest questionRequest = new QuestionRequest(robin, Age.ADULT, Student.NO, 18000);
        BundleResponse classicPlusBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CURRENT_ACCOUNT, CREDIT_CARD), null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo(robin);
        assertThat(response.getMessage()).isEqualTo("Having at least one account is necessary");
        assertThat(response.getBundleName()).isEqualTo(classicPlusBundle.getBundleName());
        assertThat(response.getProducts()).isNull();
    }
}