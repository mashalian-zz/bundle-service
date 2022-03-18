package se.seb.bundleservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.seb.bundleservice.model.Age;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Status;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.model.Violations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        QuestionRequest question = new QuestionRequest(Age.UNDER_AGE, Student.NO, 0);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Junior Saver");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(1);
        assertThat(bundleResponse.getProducts()).containsExactly(JUNIOR_SAVER_ACCOUNT);
    }

    @Test
    void shouldSuggestBundleOfStudent() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.YES, 12000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Student");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getProducts()).containsExactly(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassic() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.NO, 12000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Classic");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(2);
        assertThat(bundleResponse.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfClassicPlus() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.NO, 35000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Classic Plus");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldSuggestBundleOfGold() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.NO, 45000);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Gold");
        assertThat(bundleResponse.getProducts().size()).isEqualTo(3);
        assertThat(bundleResponse.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldNotSuggestAnyBundleIfIncomeIsZero() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.NO, 0);

        BundleResponse bundleResponse = bundleService.suggestBundle(question);

        assertThat(bundleResponse).isNotNull();
        assertThat(bundleResponse.getBundleName()).isEqualTo("Empty");
    }

    @Test
    void shouldNotCustomizeBundleIfCustomerDoesNotHaveAccountWithCorrectIncome() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.NO, 10000);
        CustomizeBundleRequest request = new CustomizeBundleRequest(CLASSIC, question, List.of(CURRENT_ACCOUNT), null);
        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Classic");
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getViolations()).contains(Violations.ACCOUNT_ISSUE);
        assertThat(response.getBody().getProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getProducts()).contains(DEBIT_CARD);
    }

    @Test
    void shouldNotCustomizeStudentBundleIfDoesNotHaveAccountWithCorrectZeroIncome() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.YES, 0);
        CustomizeBundleRequest request = new CustomizeBundleRequest(STUDENT, question, List.of(STUDENT_ACCOUNT), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBundleName()).isEqualTo("Student");
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getProducts().size()).isEqualTo(2);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getViolations()).contains(Violations.ACCOUNT_ISSUE);
        assertThat(response.getBody().getProducts()).contains(DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldModifyGoldBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CREDIT_CARD));


        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getBundleName()).isEqualTo("Gold");
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getProducts()).contains(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.SUCCESSFUL);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(0);
    }

    @Test
    void shouldNotCustomizeGoldBundleIfCustomerWantsToHaveTwoAccounts() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CURRENT_ACCOUNT, CREDIT_CARD));
        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getViolations()).contains(Violations.ACCOUNT_ISSUE);
        assertThat(response.getBody().getBundleName()).isEqualTo("Gold");
        assertThat(response.getBody().getProducts().size()).isEqualTo(4);
        assertThat(response.getBody().getProducts()).contains(CURRENT_ACCOUNT, CURRENT_ACCOUNT_PLUS, CREDIT_CARD, DEBIT_CARD);
        assertThat(response.getBody().getIllegalProducts()).contains(CURRENT_ACCOUNT, CURRENT_ACCOUNT_PLUS);
    }

    @Test
    void shouldSkipAddingProductsIfBundleContains() {

        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, null, List.of(GOLD_CREDIT_CARD));


        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBundleName()).isEqualTo("Gold");
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.SUCCESSFUL);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(0);
    }

    @Test
    void shouldSkipRemovingProductsIfBundleDoesNotContains() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(CREDIT_CARD), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getBundleName()).isEqualTo("Gold");
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.SUCCESSFUL);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(0);
    }

    @Test
    void shouldNotCustomizeIfGoldBundleWantsToHaveJuniorOrStudentAccount() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        CustomizeBundleRequest customizeBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(customizeBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBundleName()).isEqualTo("Gold");
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(2);
        assertThat(response.getBody().getViolations().size()).isEqualTo(2);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getViolations()).contains(Violations.ILLEGAL_PRODUCTS_MORE_THAN_40K, Violations.ACCOUNT_ISSUE);
    }

    @Test
    void shouldModifyClassicPlusBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 25000);
        BundleResponse classicBundlePlusResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CREDIT_CARD), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getBundleName()).isEqualTo("Classic Plus");
        assertThat(response.getBody().getProducts().size()).isEqualTo(2);
        assertThat(response.getBody().getProducts()).contains(CURRENT_ACCOUNT, DEBIT_CARD);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(0);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.SUCCESSFUL);

    }

    @Test
    void shouldNotCustomizeIfClassicPlusBundleWantsToHaveGoldProductsWithoutHavingExpectedRules() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 25000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CURRENT_ACCOUNT), List.of(CURRENT_ACCOUNT_PLUS));

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Classic Plus");
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getIllegalProducts()).contains(CURRENT_ACCOUNT_PLUS);
        assertThat(response.getBody().getViolations()).contains(Violations.ILLEGAL_PRODUCTS_UP_TO_40K);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
    }

    @Test
    void shouldModifyClassicBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 11000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, List.of(DEBIT_CARD), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getBundleName()).isEqualTo("Classic");
        assertThat(response.getBody().getProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getProducts()).containsExactly(CURRENT_ACCOUNT);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(0);
    }

    @Test
    void shouldNotCustomizeIfClassicBundleWantsToHaveCreditCard() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 11000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, null, List.of(CREDIT_CARD));

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Classic");
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getIllegalProducts()).contains(CREDIT_CARD);
        assertThat(response.getBody().getViolations()).contains(Violations.ILLEGAL_PRODUCTS_UP_TO_12K);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
    }

    @Test
    void shouldModifyStudentBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.YES, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(DEBIT_CARD), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getBundleName()).isEqualTo("Student");
        assertThat(response.getBody().getProducts().size()).isEqualTo(2);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(0);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.SUCCESSFUL);
        assertThat(response.getBody().getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD);
    }

    @Test
    void shouldNotCustomizeIfStudentBundleWantsToHaveCurrentAccount() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.YES, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(STUDENT_ACCOUNT), List.of(CURRENT_ACCOUNT));

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Student");
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getProducts()).contains(CURRENT_ACCOUNT, CREDIT_CARD, DEBIT_CARD);
        assertThat(response.getBody().getViolations()).contains(Violations.ILLEGAL_PRODUCTS_FOR_STUDENT);
    }

    @Test
    void shouldNotCustomizeIfJuniorBundleWantsToModify() {
        QuestionRequest questionRequest = new QuestionRequest(Age.UNDER_AGE, Student.NO, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(JUNIOR_SAVER, questionRequest, null, List.of(DEBIT_CARD));

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Junior Saver");
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getProducts().size()).isEqualTo(2);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getIllegalProducts()).contains(DEBIT_CARD);
        assertThat(response.getBody().getProducts()).contains(DEBIT_CARD, JUNIOR_SAVER_ACCOUNT);
        assertThat(response.getBody().getIllegalProducts()).containsExactly(DEBIT_CARD);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getViolations()).containsExactly(Violations.JUNIOR_ISSUE);
    }

    @Test
    void shouldNotCustomizeIfCustomerDoesNotHaveAnyAccountThroughModify() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 18000);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CURRENT_ACCOUNT, CREDIT_CARD), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Classic Plus");
        assertThat(response.getBody().getProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(0);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getViolations()).containsExactly(Violations.ACCOUNT_ISSUE);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
    }

    @Test
    void shouldNotCustomizeClassicBundleWithZeroAsIncome() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, List.of(DEBIT_CARD), null);

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo(CLASSIC.getName());
        assertThat(response.getBody().getProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getProducts()).containsExactly(CURRENT_ACCOUNT);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(1);
        assertThat(response.getBody().getViolations().size()).isEqualTo(1);
        assertThat(response.getBody().getIllegalProducts()).containsExactly(CURRENT_ACCOUNT);
        assertThat(response.getBody().getViolations()).containsExactly(Violations.INCOME_ZERO);
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
    }

    @Test
    void shouldNotModifyStudentBundleIfWantsToHaveMoreThanOneAccount() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.YES, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(DEBIT_CARD), List.of(CURRENT_ACCOUNT));

        ResponseEntity<CustomizedBundleResponse> response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        assertThat(response.getBody().getBundleName()).isEqualTo("Student");
        assertThat(response.getBody().getStatus()).isEqualTo(Status.ERROR);
        assertThat(response.getBody().getProducts().size()).isEqualTo(3);
        assertThat(response.getBody().getIllegalProducts().size()).isEqualTo(2);
        assertThat(response.getBody().getViolations().size()).isEqualTo(2);
        assertThat(response.getBody().getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD, CURRENT_ACCOUNT);
        assertThat(response.getBody().getIllegalProducts()).containsExactly(CURRENT_ACCOUNT, STUDENT_ACCOUNT);
        assertThat(response.getBody().getViolations()).containsExactly(Violations.ACCOUNT_ISSUE, Violations.ILLEGAL_PRODUCTS_FOR_STUDENT);
    }
}