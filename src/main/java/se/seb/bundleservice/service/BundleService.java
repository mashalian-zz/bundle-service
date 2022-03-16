package se.seb.bundleservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.seb.bundleservice.exception.UnmatchedConditionsException;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.Product;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;

import java.util.ArrayList;
import java.util.List;

import static se.seb.bundleservice.model.Bundle.CLASSIC;
import static se.seb.bundleservice.model.Bundle.CLASSIC_PLUS;
import static se.seb.bundleservice.model.Bundle.GOLD;
import static se.seb.bundleservice.model.Bundle.JUNIOR_SAVER;
import static se.seb.bundleservice.model.Bundle.STUDENT;
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

    public BundleResponse suggestBundle(QuestionRequest request) {
        return switch (request.getAge()) {
            case UNDER_AGE -> getBundleResponse(JUNIOR_SAVER);
            case ADULT, PENSION -> suggestAdultBundle(request);
        };
    }

    public CustomizedBundleResponse modifySuggestedBundle(CustomizeBundleRequest customizeBundleRequest) {
        CustomizeBundleRequest request = validateRequest(customizeBundleRequest);
        return switch (customizeBundleRequest.getBundle()) {
            case GOLD -> modifyGoldBundle(request);
            case CLASSIC_PLUS -> modifyClassicPlusBundle(request);
            case CLASSIC -> modifyClassicBundle(request);
            case STUDENT -> modifyStudentBundle(request);
            case JUNIOR_SAVER -> CustomizedBundleResponse.builder()
                    .bundleName(customizeBundleRequest.getBundle().getName())
                    .customerName(customizeBundleRequest.getQuestionRequest().getCustomerName())
                    .message("Junior Saver cannot do any modification")
                    .build();
        };
    }

    private BundleResponse suggestAdultBundle(QuestionRequest request) {
        if (request.getStudent().equals(Student.YES)) {
            return getBundleResponse(STUDENT);
        } else {
            int income = request.getIncome();
            if (income == 0) {
                return getBundleResponse(null);
            } else if (income <= 12000) {
                return getBundleResponse(CLASSIC);
            } else if (income <= 40000) {
                return getBundleResponse(CLASSIC_PLUS);
            }
        }
        return getBundleResponse(GOLD);
    }

    private BundleResponse getBundleResponse(Bundle bundle) {
        if (bundle == null) {
            return BundleResponse.builder()
                    .BundleName("Cannot suggest any bundle")
                    .build();
        }
        return BundleResponse.builder()
                .BundleName(bundle.getName())
                .products(bundle.getProducts())
                .build();
    }

    private CustomizedBundleResponse modifyStudentBundle(CustomizeBundleRequest customizeBundleRequest) {
        if (CollectionUtils.containsAny(customizeBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, CURRENT_ACCOUNT))) {
            log.warn("Customer with name {} and Student bundle is not allowed to have current account/plus or any gold credit card.",
                    customizeBundleRequest.getQuestionRequest().getCustomerName());

            return CustomizedBundleResponse.builder()
                    .bundleName(customizeBundleRequest.getBundle().getName())
                    .customerName(customizeBundleRequest.getQuestionRequest().getCustomerName())
                    .message("Having products from Gold bundle or current account are not allowed in Student bundle")
                    .build();
        } else {
            return doModifyProductsForBundle(customizeBundleRequest);
        }
    }

    private CustomizedBundleResponse modifyClassicBundle(CustomizeBundleRequest customizeBundleRequest) {
        if (CollectionUtils.containsAny(customizeBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, CREDIT_CARD))) {
            log.warn("Customer with name {} and classic bundle is not allowed to have current account plus or any credit card.",
                    customizeBundleRequest.getQuestionRequest().getCustomerName());

            return CustomizedBundleResponse.builder()
                    .bundleName(customizeBundleRequest.getBundle().getName())
                    .customerName(customizeBundleRequest.getQuestionRequest().getCustomerName())
                    .message("Having products from Gold bundle or any credit cards are not allowed in Classic bundle")
                    .build();
        } else {
            return doModifyProductsForBundle(customizeBundleRequest);
        }
    }

    private CustomizedBundleResponse modifyClassicPlusBundle(CustomizeBundleRequest customizeBundleRequest) {
        if (CollectionUtils.containsAny(customizeBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD))) {
            log.warn("Customer with name {} tries to have current account plus or gold credit card.",
                    customizeBundleRequest.getQuestionRequest().getCustomerName());

            return CustomizedBundleResponse.builder()
                    .bundleName(customizeBundleRequest.getBundle().getName())
                    .customerName(customizeBundleRequest.getQuestionRequest().getCustomerName())
                    .message("Having products from Gold bundle are not allowed in Classic Plus bundle")
                    .build();
        } else {
            return doModifyProductsForBundle(customizeBundleRequest);
        }
    }

    private CustomizedBundleResponse modifyGoldBundle(CustomizeBundleRequest customizeBundleRequest) {
        if (customizeBundleRequest.getAddProducts().contains(CURRENT_ACCOUNT) &&
                !customizeBundleRequest.getRemoveProducts().contains(CURRENT_ACCOUNT_PLUS)) {
            log.warn("Customer with name {} tries to have more that one account.",
                    customizeBundleRequest.getQuestionRequest().getCustomerName());

            return CustomizedBundleResponse.builder()
                    .bundleName(customizeBundleRequest.getBundle().getName())
                    .customerName(customizeBundleRequest.getQuestionRequest().getCustomerName())
                    .message("Having more than one account is not allowed")
                    .build();
        } else {
            return doModifyProductsForBundle(customizeBundleRequest);
        }
    }

    private CustomizedBundleResponse doModifyProductsForBundle(CustomizeBundleRequest request) {
        List<Product> products = customizeProducts(request);
        long count = checkCustomizedProductsHasAccount(products);
        String message;
        if (count == 0) {
            message = "Having at least one account is necessary";
            return getCustomizedBundleResponse(request, message, null);
        }

        log.info("Customer with name {} customized products {}", request.getQuestionRequest().getCustomerName(), products.stream().map(Product::getLabel).toList());
        message = "Products has been modified successfully.";
        return getCustomizedBundleResponse(request, message, products);
    }

    private CustomizedBundleResponse getCustomizedBundleResponse(CustomizeBundleRequest request, String message, List<Product> products) {
        return CustomizedBundleResponse.builder()
                .bundleName(request.getBundle().getName())
                .customerName(request.getQuestionRequest().getCustomerName())
                .products(products)
                .message(message)
                .build();
    }

    private long checkCustomizedProductsHasAccount(List<Product> products) {
        return products.stream()
                .filter(Product::isAccount)
                .count();
    }

    private List<Product> customizeProducts(CustomizeBundleRequest request) {
        List<Product> removeProducts = request.getRemoveProducts() == null ? List.of() : request.getRemoveProducts();
        List<Product> addProducts = request.getAddProducts() == null ? List.of() : request.getAddProducts();
        List<Product> products = new ArrayList<>(request.getBundle().getProducts());
        products.addAll(addProducts);
        products.removeAll(removeProducts);
        return products.stream().distinct().toList();
    }

    private CustomizeBundleRequest validateRequest(CustomizeBundleRequest customizeBundleRequest) {
        List<Product> addProducts = customizeBundleRequest.getAddProducts() == null ? List.of() : customizeBundleRequest.getAddProducts();
        List<Product> removeProducts = customizeBundleRequest.getRemoveProducts() == null ? List.of() : customizeBundleRequest.getRemoveProducts();
        QuestionRequest questionRequest = customizeBundleRequest.getQuestionRequest();
        Bundle bundle = customizeBundleRequest.getBundle();
        CustomizeBundleRequest bundleRequest = new CustomizeBundleRequest(bundle, questionRequest, removeProducts, addProducts);
        List<String> largeBundles = List.of(GOLD.getName(), CLASSIC.getName(), CLASSIC_PLUS.getName());
        if (largeBundles.contains(bundle.getName())) {
            if (CollectionUtils.containsAny(addProducts, List.of(JUNIOR_SAVER_ACCOUNT, STUDENT_ACCOUNT))) {
                log.warn("Gold or classic or classic plus bundle for customer {} does not have conditions for Junior or Student Account", questionRequest.getCustomerName());
                throw new UnmatchedConditionsException("Junior Saver Account or Student Account is not acceptable for Gold or classic or classic plus bundle");
            }
        }
        return bundleRequest;
    }
}
