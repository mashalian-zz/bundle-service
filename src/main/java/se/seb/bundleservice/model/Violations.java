package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;
import static se.seb.bundleservice.model.Product.STUDENT_ACCOUNT;

@AllArgsConstructor
public enum Violations {
    ILLEGAL_PRODUCTS_UP_TO_40K("Your income is not enough to choose", List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, STUDENT_ACCOUNT, JUNIOR_SAVER_ACCOUNT)),
    ILLEGAL_PRODUCTS_UP_TO_12K("Your income is not enough to choose", List.of(CURRENT_ACCOUNT_PLUS, CREDIT_CARD, GOLD_CREDIT_CARD, STUDENT_ACCOUNT, JUNIOR_SAVER_ACCOUNT)),
    ILLEGAL_PRODUCTS_MORE_THAN_40K("Your income is not enough to choose", List.of(JUNIOR_SAVER_ACCOUNT, STUDENT_ACCOUNT)),
    ACCOUNT_ISSUE("Your cannot have none or more than one account", List.of(JUNIOR_SAVER_ACCOUNT, STUDENT_ACCOUNT, CURRENT_ACCOUNT, CURRENT_ACCOUNT_PLUS)),
    JUNIOR_ISSUE("You have age limit, cannot do anything", List.of(STUDENT_ACCOUNT, CURRENT_ACCOUNT, CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD, GOLD_CREDIT_CARD)),
    INCOME_ZERO("You do not have income", List.of(JUNIOR_SAVER_ACCOUNT, STUDENT_ACCOUNT, CURRENT_ACCOUNT, CURRENT_ACCOUNT_PLUS, DEBIT_CARD, CREDIT_CARD, GOLD_CREDIT_CARD)),
    ILLEGAL_PRODUCTS_FOR_STUDENT("Student cannot have", List.of(CURRENT_ACCOUNT_PLUS, CURRENT_ACCOUNT, GOLD_CREDIT_CARD, JUNIOR_SAVER_ACCOUNT));

    @Getter
    private final String description;
    @Getter
    private final List<Product> products;
}
