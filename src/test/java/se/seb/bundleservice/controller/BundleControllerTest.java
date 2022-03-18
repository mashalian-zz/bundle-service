package se.seb.bundleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import se.seb.bundleservice.model.Age;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Status;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.service.BundleService;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static se.seb.bundleservice.model.Bundle.GOLD;
import static se.seb.bundleservice.model.Bundle.JUNIOR_SAVER;
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
        BundleResponse bundleResponse = BundleResponse.builder()
                .BundleName(JUNIOR_SAVER.getName())
                .products(JUNIOR_SAVER.getProducts())
                .build();
        QuestionRequest question = new QuestionRequest(Age.UNDER_AGE, Student.NO, 0);
        given(bundleService.suggestBundle(eq(question))).willReturn(bundleResponse);

        mockMvc.perform(post("/suggest")
                        .content(objectMapper.writeValueAsString(question))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bundleName", equalTo("Junior Saver")))
                .andExpect(jsonPath("$.products", equalTo(List.of(JUNIOR_SAVER_ACCOUNT.name()))))
                .andDo(print());
    }

    @Test
    void shouldCustomizeGoldBundle() throws Exception {

        BundleResponse bundleResponse = BundleResponse.builder()
                .BundleName(GOLD.getName())
                .products(GOLD.getProducts())
                .build();
        QuestionRequest question = new QuestionRequest(Age.UNDER_AGE, Student.NO, 0);
        CustomizeBundleRequest modifyBundleRequest = new CustomizeBundleRequest(GOLD, question, List.of(CURRENT_ACCOUNT_PLUS), List.of(CURRENT_ACCOUNT));
        CustomizedBundleResponse response = CustomizedBundleResponse.builder()
                .bundleName(bundleResponse.getBundleName())
                .status(Status.SUCCESSFUL)
                .products(List.of(CURRENT_ACCOUNT, DEBIT_CARD, GOLD_CREDIT_CARD))
                .build();
        given(bundleService.customizeBundle(eq(modifyBundleRequest))).willReturn(ResponseEntity.accepted().body(response));


        mockMvc.perform(put("/customize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyBundleRequest)))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.bundleName", equalTo(GOLD.getName())))
                .andExpect(jsonPath("$.status", equalTo(Status.SUCCESSFUL.name())))
                .andExpect(jsonPath("$.products", equalTo(List.of(CURRENT_ACCOUNT.name(), DEBIT_CARD.name(), GOLD_CREDIT_CARD.name()))));
    }
}