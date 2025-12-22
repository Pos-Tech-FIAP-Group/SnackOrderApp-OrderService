package com.fiap.snackapp.core.application.mapper;

import com.fiap.snackapp.core.application.dto.request.AddOnCreateRequest;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import org.springframework.stereotype.Component;

@Component
public class AddOnMapper {

    public AddOnDefinition toAddOnDefinition(AddOnCreateRequest dto) {
        if (dto == null) return null;
        return new AddOnDefinition(null, dto.name(), dto.category(), dto.price());
    }
}