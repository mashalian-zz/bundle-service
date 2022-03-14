package se.seb.bundleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.ModifySuggestedBundleRequest;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.model.Suggestion;
import se.seb.bundleservice.service.BundleService;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static se.seb.bundleservice.model.BundleName.GOLD;
import static se.seb.bundleservice.model.BundleName.JUNIOR_SAVER;
import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;

@WebMvcTest(controllers = {BundleController.class})
class BundleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BundleService bundleService;

    @Test
    void shouldSuggestBundleOfJuniorSaver() throws Exception {
        when(bundleService.suggestBundle(any(QuestionRequest.class)))
                .thenReturn(Suggestion.builder()
                        .customerName("Robin")
                        .bundle(new Bundle(JUNIOR_SAVER.getLabel(), List.of(JUNIOR_SAVER_ACCOUNT), 0))
                        .build());
        QuestionRequest question = new QuestionRequest("Robin", 15, Student.NO, 0);

        mockMvc.perform(post("/suggest")
                        .content(objectMapper.writeValueAsString(question))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName", equalTo("Robin")))
                .andExpect(jsonPath("$.bundle.name", equalTo("Junior Saver")))
                .andExpect(jsonPath("$.bundle.value", equalTo(0)))
                .andExpect(jsonPath("$.bundle.products", equalTo(List.of(JUNIOR_SAVER_ACCOUNT.name()))))
                .andDo(print());
    }

    @Test
    void shouldCustomizeGoldBundle() throws Exception {
        ModifySuggestedBundleRequest request = new ModifySuggestedBundleRequest("Robin", List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD), List.of(CURRENT_ACCOUNT, CREDIT_CARD));
        when(bundleService.modifySuggestedBundle(eq(request)))
                .thenReturn(Suggestion.builder()
                        .customerName("Robin")
                        .bundle(new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 3))
                        .build());

        mockMvc.perform(put("/customize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.customerName", equalTo("Robin")))
                .andExpect(jsonPath("$.bundle.name", equalTo(GOLD.getLabel())))
                .andExpect(jsonPath("$.bundle.value", equalTo(3)))
                .andExpect(jsonPath("$.bundle.products", equalTo(List.of(CURRENT_ACCOUNT.name(), DEBIT_CARD.name(), CREDIT_CARD.name()))));
    }
}