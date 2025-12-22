package com.fiap.snackapp.adapters.driven.infra.persistence.specification;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.OrderEntity;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<OrderEntity> byFilters(List<OrderStatus> orderStatus) {
        Specification<OrderEntity> spec = Specification.where(null);

        if (orderStatus != null) {
            spec = spec.and(hasOrderStatus(orderStatus));
        }

        return spec;
    }

    private static Specification<OrderEntity> hasOrderStatus (List<OrderStatus> orderStatus) {
        return (root, query, criteriaBuilder) ->
                root.get("status").in(orderStatus);
    }
}
