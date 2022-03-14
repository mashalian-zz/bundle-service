package se.seb.bundleservice.model;

import lombok.Value;
import lombok.With;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Value
public class Bundle {
    String name;
    @With
    List<Product> products;
    @Min(0)
    @Max(3)
    int value;
}
