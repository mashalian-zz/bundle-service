package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;
import static se.seb.bundleservice.model.Product.STUDENT_ACCOUNT;

@AllArgsConstructor
public enum Bundle {

    JUNIOR_SAVER(1, "Junior Saver", List.of(JUNIOR_SAVER_ACCOUNT), 0),
    STUDENT(2, "Student", List.of(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 0),
    CLASSIC(3, "Classic", List.of(CURRENT_ACCOUNT, DEBIT_CARD), 1),
    CLASSIC_PLUS(4, "Classic Plus", List.of(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 2),
    GOLD(5, "Gold", List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3);

    @Getter
    private final int id;
    @Getter
    private final String name;
    @Getter
    private final List<Product> products;
    @Getter
    private final int value;
}
