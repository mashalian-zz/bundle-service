package se.seb.bundleservice.repository;

import org.springframework.stereotype.Component;
import se.seb.bundleservice.model.CustomerBundle;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomerBundleRepository {
    private final Map<String, CustomerBundle> customerBundleMap = new HashMap<>();

    public Optional<CustomerBundle> getCustomerBundleByCustomerName(String customerName) {
        return Optional.ofNullable(customerBundleMap.get(customerName));
    }

    public CustomerBundle save(CustomerBundle customerBundle) {
        customerBundleMap.remove(customerBundle.getCustomerName());
        customerBundleMap.put(customerBundle.getCustomerName(), customerBundle);
        return customerBundleMap.get(customerBundle.getCustomerName());
    }
    public void removeSuggestion(String customerName){
        customerBundleMap.remove(customerName);
    }
}
