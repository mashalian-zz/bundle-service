package se.seb.bundleservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleResponse;
import se.seb.bundleservice.model.CustomizeBundleRequest;
import se.seb.bundleservice.model.CustomizedBundleResponse;
import se.seb.bundleservice.model.Product;
import se.seb.bundleservice.model.QuestionRequest;
import se.seb.bundleservice.model.Student;

import java.util.ArrayList;
import java.util.List;

import static se.seb.bundleservice.model.Age.UNDER_AGE;
import static se.seb.bundleservice.model.Bundle.CLASSIC;
import static se.seb.bundleservice.model.Bundle.CLASSIC_PLUS;
import static se.seb.bundleservice.model.Bundle.EMPTY;
import static se.seb.bundleservice.model.Bundle.GOLD;
import static se.seb.bundleservice.model.Bundle.JUNIOR_SAVER;
import static se.seb.bundleservice.model.Bundle.STUDENT;
import static se.seb.bundleservice.model.Message.ACCOUNTS_ISSUE;
import static se.seb.bundleservice.model.Message.ALSO;
import static se.seb.bundleservice.model.Message.SUCCESSFUL;
import static se.seb.bundleservice.model.Message.UNSUCCESSFUL;
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
        return customizeProducts(request);
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

    private CustomizedBundleResponse getCustomizedBundleResponse(CustomizeBundleRequest request, String message, List<Product> products) {
        return CustomizedBundleResponse.builder()
                .bundleName(request.getBundle().getName())
                .products(products)
                .message(message)
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
        if (questionRequest.getStudent().equals(Student.YES)) {
            return getCustomizedBundleResponseForStudent(request, products, hasAccount);
        } else if (income == 0 && products.size() > 0) {
            return getCustomizedBundleResponseForIncomeZero(request, products);
        }
        if (income <= 12000) {
            return getCustomizedBundleResponseForIncomeUpTo12000(request, products, hasAccount);
        } else if (income <= 40000) {
            return getCustomizedBundleResponseForIncomeUpTo40000(request, products, hasAccount);
        } else { //income > 40000
            return getCustomizedBundleResponseForIncomeMoreThan40000(request, products, hasAccount);
        }
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForJuniorSaver(CustomizeBundleRequest request, List<Product> products) {
        List<String> invalidProducts = products.stream()
                .filter(product -> !product.equals(JUNIOR_SAVER_ACCOUNT))
                .map(Product::getLabel)
                .toList();
        String message = "Junior Saver cannot do any modification,".concat(String.join(",", invalidProducts));
        return getCustomizedBundleResponse(request, message, List.of(JUNIOR_SAVER_ACCOUNT));
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeMoreThan40000(CustomizeBundleRequest request, List<Product> products, long hasAccount) {
        String message;
        List<Product> forbidden = List.of(JUNIOR_SAVER_ACCOUNT, STUDENT_ACCOUNT);

        List<String> forbiddenProducts = getForbiddenProducts(products, forbidden).stream()
                .map(Product::getLabel)
                .toList();
        if (forbiddenProducts.isEmpty()) {
            message = SUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            if (hasAccount == 0 || hasAccount > 1) {
                message = ACCOUNTS_ISSUE.getText();
            }
        } else {
            message = UNSUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            message = addAccountIssueTextMessage(hasAccount, message);
            log.warn(message);
        }
        return getCustomizedBundleResponse(request, message, products);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeUpTo40000(CustomizeBundleRequest request, List<Product> products, long hasAccount) {
        String message;
        List<Product> forbidden = List.of(CURRENT_ACCOUNT_PLUS, GOLD_CREDIT_CARD, STUDENT_ACCOUNT, JUNIOR_SAVER_ACCOUNT);

        List<String> forbiddenProducts = getForbiddenProducts(products, forbidden).stream()
                .map(Product::getLabel)
                .toList();
        if (forbiddenProducts.isEmpty()) {
            message = SUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            if (hasAccount == 0 || hasAccount > 1) {
                message = ACCOUNTS_ISSUE.getText();
            }
        } else {
            message = UNSUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            message = addAccountIssueTextMessage(hasAccount, message);
            log.warn(message);
        }
        return getCustomizedBundleResponse(request, message, products);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeUpTo12000(CustomizeBundleRequest request, List<Product> products, long hasAccount) {
        String message;
        List<Product> forbidden = List.of(CURRENT_ACCOUNT_PLUS, CREDIT_CARD, GOLD_CREDIT_CARD, STUDENT_ACCOUNT, JUNIOR_SAVER_ACCOUNT);

        List<String> forbiddenProducts = getForbiddenProducts(products, forbidden).stream()
                .map(Product::getLabel)
                .toList();
        if (forbiddenProducts.isEmpty()) {
            message = SUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            if (hasAccount == 0 || hasAccount > 1) {
                message = ACCOUNTS_ISSUE.getText();
            }
        } else {
            message = UNSUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            message = addAccountIssueTextMessage(hasAccount, message);
            log.warn(message);
        }
        return getCustomizedBundleResponse(request, message, products);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForIncomeZero(CustomizeBundleRequest request, List<Product> products) {
        List<String> invalidProducts = products.stream()
                .map(Product::getLabel)
                .toList();
        return getCustomizedBundleResponse(request, UNSUCCESSFUL.getText().concat(String.join(",", invalidProducts)), products);
    }

    private CustomizedBundleResponse getCustomizedBundleResponseForStudent(CustomizeBundleRequest request, List<Product> products, long hasAccount) {
        String message;
        List<Product> forbidden = List.of(CURRENT_ACCOUNT_PLUS, CURRENT_ACCOUNT, GOLD_CREDIT_CARD, JUNIOR_SAVER_ACCOUNT);
        List<String> forbiddenProducts = getForbiddenProducts(products, forbidden).stream()
                .map(Product::getLabel)
                .toList();
        if (forbiddenProducts.isEmpty()) {
            message = SUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            if (hasAccount == 0 || hasAccount > 1) {
                message = ACCOUNTS_ISSUE.getText();
            }
        } else {
            message = UNSUCCESSFUL.getText().concat(String.join(",", forbiddenProducts));
            message = addAccountIssueTextMessage(hasAccount, message);
            log.warn(message);
        }
        return getCustomizedBundleResponse(request, message, products);
    }

    private List<Product> getForbiddenProducts(List<Product> source, List<Product> forbidden) {
        if (CollectionUtils.containsAny(source, forbidden)) {
            return source.stream()
                    .filter(product -> forbidden.stream().anyMatch(product::equals))
                    .toList();
        }
        return List.of();
    }

    private String addAccountIssueTextMessage(long hasAccount, String message) {
        if (hasAccount == 0 || hasAccount > 1) {
            message = message.concat(ALSO.getText()).concat(ACCOUNTS_ISSUE.getText());
        }
        return message;
    }

    private CustomizeBundleRequest validateRequest(CustomizeBundleRequest customizeBundleRequest) {
        List<Product> addProducts = customizeBundleRequest.getAddProducts() == null ? List.of() : customizeBundleRequest.getAddProducts();
        List<Product> removeProducts = customizeBundleRequest.getRemoveProducts() == null ? List.of() : customizeBundleRequest.getRemoveProducts();
        QuestionRequest questionRequest = customizeBundleRequest.getQuestionRequest();
        Bundle bundle = customizeBundleRequest.getBundle();
        return new CustomizeBundleRequest(bundle, questionRequest, removeProducts, addProducts);
    }
}
