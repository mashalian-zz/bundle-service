package se.seb.bundleservice.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder(toBuilder = true)
public class Suggestion {
    String customerName;
    @With
    Bundle bundle;
}
