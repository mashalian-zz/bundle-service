package se.seb.bundleservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.seb.bundleservice.exception.UnmatchedConditionsException;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomerBundle;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.ModifyBundleRequest;
import se.seb.bundleservice.model.Product;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.repository.CustomerBundleRepository;

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
    private final CustomerBundleRepository repository;

    public BundleResponse suggestBundle(QuestionRequest request) {
        return switch (request.getAge()) {
            case UNDER_AGE -> getBundleResponse(JUNIOR_SAVER);
            case ADULT, PENSION -> suggestAdultBundle(request);
        };
    }

    public CustomizedBundleResponse modifySuggestedBundle(ModifyBundleRequest modifyBundleRequest) {
        Bundle bundle = modifyBundleRequest.getBundle();
        QuestionRequest questionRequest = modifyBundleRequest.getQuestionRequest();
        ModifyBundleRequest request = validateRequest(bundle, modifyBundleRequest, questionRequest);
        return switch (bundle) {
            case GOLD -> modifyGoldBundle(bundle, request, questionRequest);
            case CLASSIC_PLUS -> modifyClassicPlusBundle(bundle, request, questionRequest);
            case CLASSIC -> modifyClassicBundle(bundle, request, questionRequest);
            case STUDENT -> modifyStudentBundle(bundle, request, questionRequest);
            case JUNIOR_SAVER -> throw new UnmatchedConditionsException("Junior Saver cannot do any modification");
        };
    }

    private BundleResponse suggestAdultBundle(QuestionRequest request) {
        if (request.getStudent().equals(Student.YES)) {
            return getBundleResponse(STUDENT);
        } else {
            int income = request.getIncome();
            if (income <= 12000) {
                return getBundleResponse(CLASSIC);
            } else if (income <= 40000) {
                return getBundleResponse(CLASSIC_PLUS);
            } else {
                return getBundleResponse(GOLD);
            }
        }
    }

    private BundleResponse getBundleResponse(Bundle bundle) {
        return BundleResponse.builder()
                .BundleName(bundle.getName())
                .bundle(bundle)
                .products(bundle.getProducts())
                .build();
    }

    private CustomizedBundleResponse modifyStudentBundle(Bundle bundle, ModifyBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (CollectionUtils.containsAny(modifyBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, CURRENT_ACCOUNT))) {
            log.warn("Customer with name {} and Student bundle is not allowed to have current account/plus or any gold credit card.", questionRequest.getCustomerName());
            throw new UnmatchedConditionsException("Having products from Gold bundle or current account are not allowed in Student bundle");
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse modifyClassicBundle(Bundle bundle, ModifyBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (CollectionUtils.containsAny(modifyBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, CREDIT_CARD))) {
            log.warn("Customer with name {} and classic bundle is not allowed to have current account plus or any credit card.", questionRequest.getCustomerName());
            throw new UnmatchedConditionsException("Having products from Gold bundle or any credit cards are not allowed in Classic bundle");
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse modifyClassicPlusBundle(Bundle bundle, ModifyBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (CollectionUtils.containsAny(modifyBundleRequest.getAddProducts(), List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD))) {
            log.warn("Customer with name {} tries to have current account plus or gold credit card.", questionRequest.getCustomerName());
            throw new UnmatchedConditionsException("Having products from Gold bundle are not allowed in Classic Plus bundle");
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse modifyGoldBundle(Bundle bundle, ModifyBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        if (modifyBundleRequest.getAddProducts().contains(CURRENT_ACCOUNT) && !modifyBundleRequest.getRemoveProducts().contains(CURRENT_ACCOUNT_PLUS)) {
            log.warn("Customer with name {} tries to have more that one account.", questionRequest.getCustomerName());
            throw new UnmatchedConditionsException("Having more than one account is not allowed");
        } else {
            return doModifyProductsForBundle(bundle, modifyBundleRequest, questionRequest.getCustomerName());
        }
    }

    private CustomizedBundleResponse doModifyProductsForBundle(Bundle bundle, ModifyBundleRequest request, String customerName) {
        List<Product> removeProducts = request.getRemoveProducts() == null ? List.of() : request.getRemoveProducts();
        List<Product> addProducts = request.getAddProducts() == null ? List.of() : request.getAddProducts();
        List<Product> products = new ArrayList<>(bundle.getProducts());
        products.removeAll(removeProducts);
        products.addAll(addProducts);
        products.stream()
                .filter(Product::isAccount)
                .findAny()
                .orElseThrow(() -> {
                    log.warn("Customer with name {} must have at least one account.", customerName);
                    throw new UnmatchedConditionsException("Having at least one account is necessary");
                });

        log.info("Customer with name {} customized products", customerName);
        CustomerBundle customerBundle = repository.save(CustomerBundle.builder()
                .bundleName(bundle.getName())
                .products(products)
                .customerName(customerName)
                .build());
        return CustomizedBundleResponse.builder()
                .customerName(customerBundle.getCustomerName())
                .bundleName(customerBundle.getBundleName())
                .products(customerBundle.getProducts())
                .build();
    }

    private ModifyBundleRequest validateRequest(Bundle bundle, ModifyBundleRequest modifyBundleRequest, QuestionRequest questionRequest) {
        List<Product> addProducts = modifyBundleRequest.getAddProducts() == null ? List.of() : modifyBundleRequest.getAddProducts();
        List<Product> removeProducts = modifyBundleRequest.getRemoveProducts() == null ? List.of() : modifyBundleRequest.getRemoveProducts();
        ModifyBundleRequest bundleRequest = new ModifyBundleRequest(bundle, questionRequest, removeProducts, addProducts);
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
