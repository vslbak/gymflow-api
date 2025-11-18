package com.gymflow.api.controller.mapper;

import com.gymflow.api.controller.dto.ClassSessionDto;
import com.gymflow.api.controller.dto.GymflowClassDto;
import com.gymflow.api.core.ClassSession;
import com.gymflow.api.core.GymflowClass;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GymflowClassDtoMapper {
    GymflowClassDto fromCore(GymflowClass gymflowClass);
    GymflowClass toCore(GymflowClassDto gymflowClassDto);
    ClassSessionDto fromCore(ClassSession classSession);
}
