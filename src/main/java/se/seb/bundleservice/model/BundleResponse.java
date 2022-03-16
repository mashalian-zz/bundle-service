package se.seb.bundleservice.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Value
@Builder
public class BundleResponse {

    @NotEmpty
    String BundleName;
    List<Product> products;
}
