package interfaces;

import java.util.Map;

/**
 * Functional interface to abstract repository selection logic.
 * It enables dependency injection, improves testability by avoiding static method dependencies,
 * and supports flexible, reusable design using lambdas or method references.
 */
@FunctionalInterface
public interface RepositorySelector {
    String matchRepositoryIndexToName(Map<Integer, String> repoMap);
}
