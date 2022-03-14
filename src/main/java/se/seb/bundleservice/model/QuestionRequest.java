package se.seb.bundleservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotEmpty
    @NotNull
    String customerName;
    @Positive
    int age;
    @NotNull
    Student student;
    @Min(0)
    int income;
}
