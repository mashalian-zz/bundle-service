package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Product {
    CURRENT_ACCOUNT("Current Account"),
    CURRENT_ACCOUNT_PLUS("Current Account Plus"),
    JUNIOR_SAVER_ACCOUNT("Junior Saver Account"),
    STUDENT_ACCOUNT("Student Account"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    GOLD_CREDIT_CARD("Gold Credit Card");

    @Getter
    private final String label;

}
