package se.seb.bundleservice.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class CustomizedBundleResponse {

    @NotEmpty
    String customerName;
    @NotEmpty
    String bundleName;
    @With
    List<Product> products;
    @NotEmpty
    String message;
}
