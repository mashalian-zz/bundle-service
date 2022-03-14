package se.seb.bundleservice.repository;

import org.springframework.stereotype.Component;
import se.seb.bundleservice.model.Suggestion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SuggestionRepository {
    private final Map<String, Suggestion> suggestionMap = new HashMap<>();

    public Optional<Suggestion> getSuggestionByCustomerName(String name) {
        return Optional.ofNullable(suggestionMap.get(name));
    }

    public Suggestion saveSuggestion(Suggestion suggestion) {
        suggestionMap.put(suggestion.getCustomerName(), suggestion);
        return suggestionMap.get(suggestion.getCustomerName());
    }
    public void removeSuggestion(String customerName){
        suggestionMap.remove(customerName);
    }
}
