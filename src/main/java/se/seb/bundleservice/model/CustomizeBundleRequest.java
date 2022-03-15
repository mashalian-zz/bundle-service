package se.seb.bundleservice.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Value
@Builder
public class CustomizeBundleRequest {

    @NotNull @Valid
    QuestionRequest questionRequest;

    @NotNull
    Bundle bundle;

    @NotNull @Valid
    ModifyBundleRequest modifyBundleRequest;
}
