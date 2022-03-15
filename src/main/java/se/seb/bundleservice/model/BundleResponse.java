package se.seb.bundleservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Value
@Builder
public class BundleResponse {
    @NotNull
    @JsonProperty("bundle")
    Bundle bundle;

    @NotEmpty
    @JsonProperty("bundleName")
    String BundleName;

    @NotNull @Size(min = 1)
    @JsonProperty("products")
    List<Product> products;
}
