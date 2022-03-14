package se.seb.bundleservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.seb.bundleservice.exception.NotFoundException;
import se.seb.bundleservice.exception.UnmatchedConditionsException;
import se.seb.bundleservice.model.BundleName;
import se.seb.bundleservice.model.ModifySuggestedBundleRequest;
import se.seb.bundleservice.model.Product;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.model.Suggestion;
import se.seb.bundleservice.repository.BundleRepository;
import se.seb.bundleservice.repository.SuggestionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static se.seb.bundleservice.model.BundleName.CLASSIC;
import static se.seb.bundleservice.model.BundleName.CLASSIC_PLUS;
import static se.seb.bundleservice.model.BundleName.GOLD;
import static se.seb.bundleservice.model.BundleName.JUNIOR_SAVER;
import static se.seb.bundleservice.model.BundleName.STUDENT;
import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;
import static se.seb.bundleservice.model.Product.STUDENT_ACCOUNT;

@Service
@Slf4j
@AllArgsConstructor
public class BundleService {
    private final BundleRepository bundleRepository;
    private final SuggestionRepository suggestionRepository;

    public Suggestion suggestBundle(QuestionRequest request) {
        String customerName = request.getCustomerName();
        Suggestion suggestion = suggestionRepository.getSuggestionByCustomerName(customerName).orElse(null);
        if (Objects.nonNull(suggestion)) {
            return suggestion;
        }
        if (request.getAge() < 18) {
            return buildAndSaveSuggestion(customerName, JUNIOR_SAVER);
        }
        if (request.getAge() > 17) {
            if (request.getStudent().equals(Student.YES)) {
                return buildAndSaveSuggestion(customerName, STUDENT);
            } else {
                int income = request.getIncome();
                if (income > 0 && income <= 12000) {
                    return buildAndSaveSuggestion(customerName, CLASSIC);
                } else if (income > 12000 && income <= 40000) {
                    return buildAndSaveSuggestion(customerName, CLASSIC_PLUS);
                } else if (income > 40000) {
                    return buildAndSaveSuggestion(customerName, GOLD);
                }
            }
        }
        log.warn("cannot suggest any bundle due to legal rules for customer with name {}", customerName);
        throw new UnmatchedConditionsException("Conditions are not matched to suggest any bundle!");
    }

    public Suggestion modifySuggestedBundle(ModifySuggestedBundleRequest modifySuggestedBundleRequest) {
        String customerName = modifySuggestedBundleRequest.getCustomerName();
        Suggestion suggestion = suggestionRepository.getSuggestionByCustomerName(customerName)
                .orElseThrow(() -> {
                    log.warn("{} does not have any bundle to modify", customerName);
                    return new NotFoundException(String.format("Customer with name %s does not have any suggestion to modify", customerName));
                });
        ModifySuggestedBundleRequest request = validateRequest(modifySuggestedBundleRequest, suggestion);

        if (suggestion.getBundle().getName().equals(GOLD.getLabel())) {
            return modifyGoldSuggestion(request, customerName, suggestion);

        } else if (suggestion.getBundle().getName().equals(CLASSIC_PLUS.getLabel())) {
            return modifyClassicPlusSuggestion(request, customerName, suggestion);

        } else if (suggestion.getBundle().getName().equals(CLASSIC.getLabel())) {
            return modifyClassicSuggestion(request, customerName, suggestion);

        } else if (suggestion.getBundle().getName().equals(STUDENT.getLabel())) {
            return modifyStudentSuggestion(request, customerName, suggestion);

        } else if (suggestion.getBundle().getName().equals(JUNIOR_SAVER.getLabel())) {
            log.warn("Customer with name {} and Junior saver bundle is not allowed to have any modification.", customerName);
            throw new UnmatchedConditionsException("Junior Saver cannot do any modification");
        }
        return null;
    }

    private Suggestion modifyStudentSuggestion(ModifySuggestedBundleRequest request, String customerName, Suggestion suggestion) {
        if (request.getAddProducts().contains(CURRENT_ACCOUNT_PLUS) ||
                request.getAddProducts().contains(GOLD_CREDIT_CARD) ||
                request.getAddProducts().contains(CURRENT_ACCOUNT)) {
            log.warn("Customer with name {} and Student bundle is not allowed to have current account/plus or any gold credit card.", customerName);
            throw new UnmatchedConditionsException("Having products from Gold bundle or current account are not allowed in Student bundle");
        } else {
            return doModifyProductsForBundle(request, customerName, suggestion);
        }
    }

    private Suggestion modifyClassicSuggestion(ModifySuggestedBundleRequest request, String customerName, Suggestion suggestion) {
        if (request.getAddProducts().contains(CURRENT_ACCOUNT_PLUS) ||
                request.getAddProducts().contains(GOLD_CREDIT_CARD) ||
                request.getAddProducts().contains(CREDIT_CARD)) {
            log.warn("Customer with name {} and classic bundle is not allowed to have current account plus or any credit card.", customerName);
            throw new UnmatchedConditionsException("Having products from Gold bundle or any credit cards are not allowed in Classic bundle");
        } else {
            return doModifyProductsForBundle(request, customerName, suggestion);
        }
    }

    private Suggestion modifyClassicPlusSuggestion(ModifySuggestedBundleRequest request, String customerName, Suggestion suggestion) {
        if (request.getAddProducts().contains(CURRENT_ACCOUNT_PLUS) || request.getAddProducts().contains(GOLD_CREDIT_CARD)) {
            log.warn("Customer with name {} tries to have current account plus or gold credit card.", customerName);
            throw new UnmatchedConditionsException("Having products from Gold bundle are not allowed in Classic Plus bundle");
        } else {
            return doModifyProductsForBundle(request, customerName, suggestion);
        }
    }

    private Suggestion modifyGoldSuggestion(ModifySuggestedBundleRequest request, String customerName, Suggestion suggestion) {
        if (request.getAddProducts().contains(CURRENT_ACCOUNT) && !request.getRemoveProducts().contains(CURRENT_ACCOUNT_PLUS)) {
            log.warn("Customer with name {} tries to have more that one account.", customerName);
            throw new UnmatchedConditionsException("Having more than one account is not allowed");
        } else {
            return doModifyProductsForBundle(request, customerName, suggestion);
        }
    }

    private Suggestion doModifyProductsForBundle(ModifySuggestedBundleRequest request, String customerName, Suggestion suggestion) {
        List<Product> removeProducts = request.getRemoveProducts() == null ? List.of() : request.getRemoveProducts();
        List<Product> addProducts = request.getAddProducts() == null ? List.of() : request.getAddProducts();
        List<Product> products = new ArrayList<>(suggestion.getBundle().getProducts());
        products.removeAll(removeProducts);
        products.addAll(addProducts);
        List<Product> finalProducts = products.stream()
                .distinct()
                .toList();
        List<Product> availableAccounts = List.of(CURRENT_ACCOUNT, CURRENT_ACCOUNT_PLUS, STUDENT_ACCOUNT, JUNIOR_SAVER_ACCOUNT);
        if (!CollectionUtils.containsAny(finalProducts, availableAccounts)) {
            log.warn("Customer with name {} must have at least one account.", customerName);
            throw new UnmatchedConditionsException("Having at least one account is necessary");
        }
        Suggestion modifiedSuggestion = suggestion.toBuilder()
                .bundle(suggestion.getBundle().withProducts(finalProducts))
                .build();
        log.info("Customer with name {} customized products", customerName);
        suggestionRepository.removeSuggestion(customerName);
        return suggestionRepository.saveSuggestion(modifiedSuggestion);
    }

    private Suggestion buildAndSaveSuggestion(String customerName, BundleName bundleName) {
        log.info("Suggested bundle {} for customer with name {} has been saved.", bundleName.getLabel(), customerName);
        return suggestionRepository.saveSuggestion(Suggestion.builder()
                .customerName(customerName)
                .bundle(bundleRepository.getBundleByName(bundleName))
                .build());
    }

    private ModifySuggestedBundleRequest validateRequest(ModifySuggestedBundleRequest request, Suggestion suggestion) {
        List<Product> addProducts = request.getAddProducts() == null ? List.of() : request.getAddProducts();
        List<Product> removeProducts = request.getRemoveProducts() == null ? List.of() : request.getRemoveProducts();
        ModifySuggestedBundleRequest modifySuggestedBundleRequest = new ModifySuggestedBundleRequest(request.getCustomerName(), removeProducts, addProducts);
        List<String> largeBundles = List.of(GOLD.getLabel(), CLASSIC.getLabel(), CLASSIC_PLUS.getLabel());
        if (largeBundles.contains(suggestion.getBundle().getName())) {
            if (addProducts.contains(JUNIOR_SAVER_ACCOUNT) || addProducts.contains(STUDENT_ACCOUNT)) {
                log.warn("Gold or classic or classic plus bundle for customer {} does not have conditions for Junior or Student Account", request.getCustomerName());
                throw new UnmatchedConditionsException("Junior Saver Account or Student Account is not acceptable for Gold or classic or classic plus bundle");
            }
        }
        return modifySuggestedBundleRequest;
    }
}
