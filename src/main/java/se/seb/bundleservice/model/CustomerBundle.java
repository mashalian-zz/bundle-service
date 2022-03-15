package se.seb.bundleservice.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Value
public class CustomerBundle {

    @NotEmpty
    String customerName;
    @NotEmpty
    String bundleName;
    @NotNull @Size(min = 1)
    List<Product> products;
}
