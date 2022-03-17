package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Message {
    SUCCESSFUL("Products has been modified successfully."),
    ACCOUNTS_ISSUE("Having more than one or does not have any account is not allowed"),
    UNSUCCESSFUL("Due to your given response, these products cannot be chosen "),
    ALSO(" Also ");
    @Getter
    private final String text;
}
