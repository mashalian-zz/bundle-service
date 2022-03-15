package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Product {
    CURRENT_ACCOUNT("Current Account", true),
    CURRENT_ACCOUNT_PLUS("Current Account Plus", true),
    JUNIOR_SAVER_ACCOUNT("Junior Saver Account", true),
    STUDENT_ACCOUNT("Student Account", true),
    DEBIT_CARD("Debit Card", false),
    CREDIT_CARD("Credit Card", false),
    GOLD_CREDIT_CARD("Gold Credit Card", false);

    @Getter
    private final String label;

    @Getter
    private final boolean isAccount;

}
