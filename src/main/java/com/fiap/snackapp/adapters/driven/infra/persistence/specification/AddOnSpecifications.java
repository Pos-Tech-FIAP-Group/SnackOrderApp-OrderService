package com.fiap.snackapp.adapters.driven.infra.persistence.specification; // Novo pacote

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.AddOnEntity;
import com.fiap.snackapp.core.domain.enums.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AddOnSpecifications {

    private AddOnSpecifications() {}

    public static Specification<AddOnEntity> byFilters(Boolean active, Category category) {
        Specification<AddOnEntity> spec = Specification.where(null);

        if (active != null) {
            spec = spec.and(isActive(active));
        }

        if (category != null) {
            spec = spec.and(hasCategory(category));
        }

        return spec;
    }

    private static Specification<AddOnEntity> isActive(boolean active) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }

    private static Specification<AddOnEntity> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<AddOnEntity> byFiltersDynamic(Boolean active, Category category) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
