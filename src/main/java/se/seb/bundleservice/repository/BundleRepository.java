package se.seb.bundleservice.repository;

import org.springframework.stereotype.Component;
import se.seb.bundleservice.model.Bundle;
import se.seb.bundleservice.model.BundleName;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se.seb.bundleservice.model.BundleName.CLASSIC;
import static se.seb.bundleservice.model.BundleName.CLASSIC_PLUS;
import static se.seb.bundleservice.model.BundleName.GOLD;
import static se.seb.bundleservice.model.BundleName.JUNIOR_SAVER;
import static se.seb.bundleservice.model.BundleName.STUDENT;
import static se.seb.bundleservice.model.Product.CREDIT_CARD;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT;
import static se.seb.bundleservice.model.Product.CURRENT_ACCOUNT_PLUS;
import static se.seb.bundleservice.model.Product.DEBIT_CARD;
import static se.seb.bundleservice.model.Product.GOLD_CREDIT_CARD;
import static se.seb.bundleservice.model.Product.JUNIOR_SAVER_ACCOUNT;
import static se.seb.bundleservice.model.Product.STUDENT_ACCOUNT;

@Component
public class BundleRepository {
    private final Map<BundleName, Bundle> bundles = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        bundles.put(JUNIOR_SAVER, new Bundle(JUNIOR_SAVER.getLabel(), List.of(JUNIOR_SAVER_ACCOUNT), 0));
        bundles.put(STUDENT, new Bundle(STUDENT.getLabel(), List.of(STUDENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 0));
        bundles.put(CLASSIC, new Bundle(CLASSIC.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD), 1));
        bundles.put(CLASSIC_PLUS, new Bundle(CLASSIC_PLUS.getLabel(), List.of(CURRENT_ACCOUNT, DEBIT_CARD, CREDIT_CARD), 2));
        bundles.put(GOLD, new Bundle(GOLD.getLabel(), List.of(CURRENT_ACCOUNT_PLUS, DEBIT_CARD, GOLD_CREDIT_CARD), 3));
    }

    public Bundle getBundleByName(BundleName bundleName) {
        return bundles.get(bundleName);
    }
}
