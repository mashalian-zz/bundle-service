package se.seb.bundleservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.Product;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Status;
import se.seb.bundleservice.model.Student;
import se.seb.bundleservice.model.Violations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static se.seb.bundleservice.model.Age.UNDER_AGE;
import static se.seb.bundleservice.model.Bundle.CLASSIC;
import static se.seb.bundleservice.model.Bundle.CLASSIC_PLUS;
import static se.seb.bundleservice.model.Bundle.EMPTY;
import static se.seb.bundleservice.model.Bundle.GOLD;
import static se.seb.bundleservice.model.Bundle.JUNIOR_SAVER;
import static se.seb.bundleservice.model.Bundle.STUDENT;
import static se.seb.bundleservice.model.Violations.ACCOUNT_ISSUE;
import static se.seb.bundleservice.model.Violations.ILLEGAL_PRODUCTS_MORE_THAN_40K;
import static se.seb.bundleservice.model.Violations.ILLEGAL_PRODUCTS_UP_TO_12K;
import static se.seb.bundleservice.model.Violations.ILLEGAL_PRODUCTS_UP_TO_40K;
import static se.seb.bundleservice.model.Violations.INCOME_ZERO;


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

    public ResponseEntity<CustomizedBundleResponse> customizeBundle(CustomizeBundleRequest customizeBundleRequest) {
        CustomizeBundleRequest request = validateRequest(customizeBundleRequest);
        CustomizedBundleResponse response = customizeProducts(request);
        if (response.getStatus().equals(Status.SUCCESSFUL)) {
            return ResponseEntity.accepted().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).body(response);
        }
    }

    private BundleResponse suggestAdultBundle(QuestionRequest request) {
        if (request.getStudent().equals(Student.YES)) {
            return getBundleResponse(STUDENT);
        } else {
            int income = request.getIncome();
            if (income == 0) {
                return getBundleResponse(EMPTY);
            } else if (income <= 12000) {
                return getBundleResponse(CLASSIC);
            } else if (income <= 40000) {
                return getBundleResponse(CLASSIC_PLUS);
            }
        }
        return getBundleResponse(GOLD);
    }

    private BundleResponse getBundleResponse(Bundle bundle) {
        return BundleResponse.builder()
                .BundleName(bundle.getName())
                .products(bundle.getProducts())
                .build();
    }

    private CustomizedBundleResponse getCustomizedBundleResponse(CustomizeBundleRequest request, List<Violations> violations, List<Product> products, List<Product> forbiddenProducts) {
        return CustomizedBundleResponse.builder()
                .bundleName(request.getBundle().getName())
                .products(products)
                .illegalProducts(forbiddenProducts)
                .violations(violations)
                .status(violations.size() == 0 ? Status.SUCCESSFUL : Status.ERROR)
                .build();
    }

    private long checkCustomizedProductsHasAccount(List<Product> products) {
        return products.stream()
                .filter(Product::isAccount)
                .count();
    }

    private CustomizedBundleResponse customizeProducts(CustomizeBundleRequest request) {
        List<Product> removeProducts = request.getRemoveProducts() == null ? List.of() : request.getRemoveProducts();
        List<Product> addProducts = request.getAddProducts() == null ? List.of() : request.getAddProducts();
        List<Product> products = new ArrayList<>(request.getBundle().getProducts());
        products.addAll(addProducts);
        products.removeAll(removeProducts);
        return validateCustomizedProducts(request, products.stream().distinct().toList());
    }

    private CustomizedBundleResponse validateCustomizedProducts(CustomizeBundleRequest request, List<Product> products) {
        QuestionRequest questionRequest = request.getQuestionRequest();
        if (questionRequest.getAge().equals(UNDER_AGE)) {
            return getCustomizedBundleResponseForJuniorSaver(request, products);
        }
        int income = questionRequest.getIncome();
        long hasAccount = checkCustomizedProductsHasAccount(products);
        List<Violations> violations = new ArrayList<>();
        if (hasAccount == 0 || hasAccount > 1) {
            violations.add(ACCOUNT_ISSUE);
        }
        if (questionRequest.getStudent().equals(Student.YES)) {
            return getCustomizedBundleResponseForStudent(request, products, violations);
        } else if (income == 0 && products.size() > 0) {
            return getCustomizedBundleResponseForIncomeZero(request, products, violations);
        }
        if (income <= 12000) {
            return getCustomizedBundleResponseForIncomeUpTo12K(request, products, violations);
        } else if (income <= 40000) {
            return getCustomizedBundleResponseForIncomeUpTo40K(request, products, violations);
        } else { //income > 40000
            return getCustomizedBundleResponseForIncomeMoreThan40K(request, products, violations);
        }
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForJuniorSaver(CustomizeBundleRequest request, List<Product> products) {
        List<Product> forbiddenProducts = getForbiddenProducts(products, Violations.JUNIOR_ISSUE.getProducts());
        return getCustomizedBundleResponseWithViolations(request, products, List.of(Violations.JUNIOR_ISSUE), forbiddenProducts);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeMoreThan40K(CustomizeBundleRequest request, List<Product> products, List<Violations> violations) {
        List<Product> forbiddenProducts = getForbiddenProducts(products, ILLEGAL_PRODUCTS_MORE_THAN_40K.getProducts());
        if (!forbiddenProducts.isEmpty()) {
            violations.add(ILLEGAL_PRODUCTS_MORE_THAN_40K);
        }
        return getCustomizedBundleResponseWithViolations(request, products, violations, forbiddenProducts);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseWithViolations(CustomizeBundleRequest request, List<Product> products, List<Violations> violations, List<Product> forbiddenProducts) {
        log.warn("Unable to customize.");
        if (violations.contains(ACCOUNT_ISSUE)) {
            forbiddenProducts = Stream.concat(forbiddenProducts.stream(),
                    products.stream().filter(Product::isAccount)).distinct().toList();
        }
        return getCustomizedBundleResponse(request, violations, products, forbiddenProducts);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeUpTo40K(CustomizeBundleRequest request, List<Product> products, List<Violations> violations) {
        List<Product> forbiddenProducts = getForbiddenProducts(products, ILLEGAL_PRODUCTS_UP_TO_40K.getProducts());

        if (!forbiddenProducts.isEmpty()) {
            violations.add(Violations.ILLEGAL_PRODUCTS_UP_TO_40K);
        }
        return getCustomizedBundleResponseWithViolations(request, products, violations, forbiddenProducts);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeUpTo12K(CustomizeBundleRequest request, List<Product> products, List<Violations> violations) {
        List<Product> forbiddenProducts = getForbiddenProducts(products, ILLEGAL_PRODUCTS_UP_TO_12K.getProducts());

        if (!forbiddenProducts.isEmpty()) {
            violations.add(ILLEGAL_PRODUCTS_UP_TO_12K);
        }
        return getCustomizedBundleResponseWithViolations(request, products, violations, forbiddenProducts);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeZero(CustomizeBundleRequest request, List<Product> products, List<Violations> violations) {
        List<Product> forbiddenProducts = getForbiddenProducts(products, INCOME_ZERO.getProducts());
        if (!forbiddenProducts.isEmpty()) {
            violations.add(INCOME_ZERO);
        }
        return getCustomizedBundleResponseWithViolations(request, products, violations, forbiddenProducts);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForStudent(CustomizeBundleRequest request, List<Product> products, List<Violations> violations) {
        List<Product> forbiddenProducts = getForbiddenProducts(products,Violations.ILLEGAL_PRODUCTS_FOR_STUDENT.getProducts());
        if (!forbiddenProducts.isEmpty()) {
            violations.add(Violations.ILLEGAL_PRODUCTS_FOR_STUDENT);
        }
        return getCustomizedBundleResponseWithViolations(request, products, violations, forbiddenProducts);
    }

    private List<Product> getForbiddenProducts(List<Product> source, List<Product> forbidden) {
        if (CollectionUtils.containsAny(source, forbidden)) {
            return source.stream()
                    .filter(product -> forbidden.stream().anyMatch(product::equals))
                    .toList();
        }
        return List.of();
    }

    private CustomizeBundleRequest validateRequest(CustomizeBundleRequest customizeBundleRequest) {
        List<Product> addProducts = customizeBundleRequest.getAddProducts() == null ? List.of() : customizeBundleRequest.getAddProducts();
        List<Product> removeProducts = customizeBundleRequest.getRemoveProducts() == null ? List.of() : customizeBundleRequest.getRemoveProducts();
        QuestionRequest questionRequest = customizeBundleRequest.getQuestionRequest();
        Bundle bundle = customizeBundleRequest.getBundle();
        return new CustomizeBundleRequest(bundle, questionRequest, removeProducts, addProducts);
    }
}
