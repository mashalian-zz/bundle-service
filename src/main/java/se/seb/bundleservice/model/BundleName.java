package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BundleName {

    JUNIOR_SAVER("Junior Saver"),
    STUDENT("Student"),
    CLASSIC("Classic"),
    CLASSIC_PLUS("Classic Plus"),
    GOLD("Gold");

    @Getter
    private final String label;
}
