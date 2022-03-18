package se.seb.bundleservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.seb.bundleservice.model.Age;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.Message;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;

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
        String expectedMessage = Message.ACCOUNTS_ISSUE.getText();
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(request);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo("Classic");
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getProducts().size()).isEqualTo(1);
        assertThat(response.getProducts()).containsExactly(DEBIT_CARD);
    }

    @Test
    void shouldNotCustomizeStudentBundleIfDoesNotHaveAccountWithCorrectZeroIncome() {
        QuestionRequest question = new QuestionRequest(Age.ADULT, Student.YES, 0);
        CustomizeBundleRequest request = new CustomizeBundleRequest(STUDENT, question, List.of(STUDENT_ACCOUNT), null);
        String expectedMessage = Message.ACCOUNTS_ISSUE.getText();
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(request);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo("Student");
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(DEBIT_CARD,CREDIT_CARD);
    }

    @Test
    void shouldModifyGoldBundle() {

        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CREDIT_CARD));


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD);
    }

    @Test
    void shouldNotCustomizeGoldBundleIfCustomerWantsToHaveTwoAccounts() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(CURRENT_ACCOUNT, CREDIT_CARD));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Message.ACCOUNTS_ISSUE.getText());
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts()).isNotNull();
    }

    @Test
    void shouldSkipAddingProductsIfBundleContains() {

        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, null, List.of(GOLD_CREDIT_CARD));


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldSkipRemovingProductsIfBundleDoesNotContains() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        BundleResponse goldBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(CREDIT_CARD), null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(goldBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD);
    }

    @Test
    void shouldThrowExceptionIfGoldBundleWantsToHaveJuniorOrStudentAccount() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 50000);
        CustomizeBundleRequest customizeBundleRequest = new CustomizeBundleRequest(GOLD, questionRequest, List.of(GOLD_CREDIT_CARD), List.of(STUDENT_ACCOUNT));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(customizeBundleRequest);

        assertThat(response).isNotNull();
        String expectedMessage = Message.UNSUCCESSFUL.getText().concat(String.join(",", STUDENT_ACCOUNT.getLabel())).concat(Message.ALSO.getText()).concat(Message.ACCOUNTS_ISSUE.getText());
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void shouldModifyClassicPlusBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 25000);
        BundleResponse classicBundlePlusResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CREDIT_CARD), null);


        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(classicBundlePlusResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT, DEBIT_CARD);
    }

    @Test
    void shouldNotCustomizeIfClassicPlusBundleWantsToHaveGoldProductsWithoutHavingExpectedRules() {
        String expectedMessage = Message.UNSUCCESSFUL.getText().concat(String.join(",", CURRENT_ACCOUNT_PLUS.getLabel()));
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 25000);
        BundleResponse classicPlusBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CURRENT_ACCOUNT), List.of(CURRENT_ACCOUNT_PLUS));

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBundleName()).isEqualTo(classicPlusBundleResponse.getBundleName());
        assertThat(response.getProducts()).isNotNull();
    }

    @Test
    void shouldModifyClassicBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 11000);
        BundleResponse classicBundleResponse = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, List.of(DEBIT_CARD), null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(classicBundleResponse.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(1);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT);
    }

    @Test
    void shouldNotCustomizeIfClassicBundleWantsToHaveCreditCard() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 11000);
        BundleResponse classicBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, null, List.of(CREDIT_CARD));
        String expectedMessage = Message.UNSUCCESSFUL.getText().concat(String.join(",", CREDIT_CARD.getLabel()));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(classicBundle.getBundleName());
        assertThat(response.getProducts()).isNotNull();
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void shouldModifyStudentBundle() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(DEBIT_CARD), null);

        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(studentBundle.getBundleName());
        assertThat(response.getProducts().size()).isEqualTo(2);
        assertThat(response.getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD);
    }

    @Test
    void shouldNotCustomizeIfStudentBundleWantsToHaveCurrentAccount() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(STUDENT_ACCOUNT), List.of(CURRENT_ACCOUNT));
        String expectedMessage = Message.UNSUCCESSFUL.getText().concat(String.join(",", CURRENT_ACCOUNT.getLabel()));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBundleName()).isEqualTo(studentBundle.getBundleName());
        assertThat(response.getProducts()).isNotNull();
    }

    @Test
    void shouldNotCustomizeIfJuniorBundleWantsToModify() {
        QuestionRequest questionRequest = new QuestionRequest(Age.UNDER_AGE, Student.NO, 0);
        BundleResponse juniorSaveBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(JUNIOR_SAVER, questionRequest, null, List.of(DEBIT_CARD));
        String expectedMessage = "Junior Saver cannot do any modification,".concat(String.join(",", DEBIT_CARD.getLabel()));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBundleName()).isEqualTo(juniorSaveBundle.getBundleName());
        assertThat(response.getProducts()).isNotNull();
    }

    @Test
    void shouldNotCustomizeIfCustomerDoesNotHaveAnyAccountThroughModify() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 18000);
        BundleResponse classicPlusBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC_PLUS, questionRequest, List.of(CURRENT_ACCOUNT, CREDIT_CARD), null);
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Message.ACCOUNTS_ISSUE.getText());
        assertThat(response.getBundleName()).isEqualTo(classicPlusBundle.getBundleName());
        assertThat(response.getProducts()).isNotNull();
    }

    @Test
    void shouldNotCustomizeClassicBundleWithZeroAsIncome() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.NO, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(CLASSIC, questionRequest, List.of(DEBIT_CARD), null);
        String expectedMessage = Message.UNSUCCESSFUL.getText().concat(String.join(",", CURRENT_ACCOUNT.getLabel()));
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(CLASSIC.getName());
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getProducts().size()).isEqualTo(1);
        assertThat(response.getProducts()).containsExactly(CURRENT_ACCOUNT);
    }

    @Test
    void shouldNotModifyStudentBundleIfWantsToHaveMoreThanOneAccount() {
        QuestionRequest questionRequest = new QuestionRequest(Age.ADULT, Student.YES, 0);
        BundleResponse studentBundle = bundleService.suggestBundle(questionRequest);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(STUDENT, questionRequest, List.of(DEBIT_CARD), List.of(CURRENT_ACCOUNT));
        String expectedMessage = Message.UNSUCCESSFUL.getText()
                .concat(String.join(",", CURRENT_ACCOUNT.getLabel()))
                .concat(Message.ALSO.getText())
                .concat(Message.ACCOUNTS_ISSUE.getText());
        CustomizedBundleResponse response = bundleService.modifySuggestedBundle(modifyBundleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBundleName()).isEqualTo(studentBundle.getBundleName());
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getProducts().size()).isEqualTo(3);
        assertThat(response.getProducts()).containsExactly(STUDENT_ACCOUNT, CREDIT_CARD,CURRENT_ACCOUNT);
    }
}