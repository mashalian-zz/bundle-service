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

    public CustomizedBundleResponse modifySuggestedBundle(CustomizeBundleRequest modifyBundleRequest) {
        Bundle bundle = modifyBundleRequest.getBundle();
        QuestionRequest questionRequest = modifyBundleRequest.getQuestionRequest();
        CustomizeBundleRequest request = validateRequest(bundle, modifyBundleRequest, questionRequest);
        return switch (bundle) {
            case GOLD -> modifyGoldBundle(bundle, request, questionRequest);
            case CLASSIC_PLUS -> modifyClassicPlusBundle(bundle, request, questionRequest);
            case CLASSIC -> modifyClassicBundle(bundle, request, questionRequest);
            case STUDENT -> modifyStudentBundle(bundle, request, questionRequest);
            case JUNIOR_SAVER -> CustomizedBundleResponse.builder()
                    .bundleName(bundle.getName())
                    .customerName(questionRequest.getCustomerName())
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

    private CustomizedBundleResponse modifyStudentBundle(Bundle bundle, CustomizeBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (CollectionUtils.containsAny(modifyBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, CURRENT_ACCOUNT))) {
            log.warn("Customer with name {} and Student bundle is not allowed to have current account/plus or any gold credit card.", questionRequest.getCustomerName());
            return CustomizedBundleResponse.builder()
                    .bundleName(bundle.getName())
                    .customerName(questionRequest.getCustomerName())
                    .message("Having products from Gold bundle or current account are not allowed in Student bundle")
                    .build();
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse modifyClassicBundle(Bundle bundle, CustomizeBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (CollectionUtils.containsAny(modifyBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, CREDIT_CARD))) {
            log.warn("Customer with name {} and classic bundle is not allowed to have current account plus or any credit card.", questionRequest.getCustomerName());
            return CustomizedBundleResponse.builder()
                    .bundleName(bundle.getName())
                    .customerName(questionRequest.getCustomerName())
                    .message("Having products from Gold bundle or any credit cards are not allowed in Classic bundle")
                    .build();
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse modifyClassicPlusBundle(Bundle bundle, CustomizeBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (CollectionUtils.containsAny(modifyBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD))) {
            log.warn("Customer with name {} tries to have current account plus or gold credit card.", questionRequest.getCustomerName());
            return CustomizedBundleResponse.builder()
                    .bundleName(bundle.getName())
                    .customerName(questionRequest.getCustomerName())
                    .message("Having products from Gold bundle are not allowed in Classic Plus bundle")
                    .build();
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse modifyGoldBundle(Bundle bundle, CustomizeBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (modifyBundleRequest.getAddProducts().contains(CURRENT_ACCOUNT) && !modifyBundleRequest.getRemoveProducts().contains(CURRENT_ACCOUNT_PLUS)) {
            log.warn("Customer with name {} tries to have more that one account.", questionRequest.getCustomerName());
            return CustomizedBundleResponse.builder()
                    .bundleName(bundle.getName())
                    .customerName(questionRequest.getCustomerName())
                    .message("Having more than one account is not allowed")
                    .build();
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse doModifyProductsForBundle(Bundle bundle, CustomizeBundleRequest request, String customerName) {
        List<Product> removeProducts = request.getRemoveProducts() == null ? List.of() : request.getRemoveProducts();
        List<Product> addProducts = request.getAddProducts() == null ? List.of() : request.getAddProducts();
        List<Product> products = new ArrayList<>(bundle.getProducts());
        products.addAll(addProducts);
        products.removeAll(removeProducts);
        long count = products.stream()
                .filter(Product::isAccount)
                .distinct()
                .count();
        if (count == 0) {
            return CustomizedBundleResponse.builder()
                    .bundleName(bundle.getName())
                    .customerName(request.getQuestionRequest().getCustomerName())
                    .message("Having at least one account is necessary")
                    .build();
        }
        List<Product> finalProducts = products.stream().distinct().toList();

        log.info("Customer with name {} customized products {}", customerName, finalProducts.stream().map(Product::getLabel).toList());
        return CustomizedBundleResponse.builder()
                .customerName(request.getQuestionRequest().getCustomerName())
                .bundleName(bundle.getName())
                .products(finalProducts)
                .message("Products has been modified successfully.")
                .build();
    }

    private CustomizeBundleRequest validateRequest(Bundle bundle, CustomizeBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        List<Product> addProducts = modifyBundleRequest.getAddProducts() == null ? List.of() : modifyBundleRequest.getAddProducts();
        List<Product> removeProducts = modifyBundleRequest.getRemoveProducts() == null ? List.of() : modifyBundleRequest.getRemoveProducts();
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
