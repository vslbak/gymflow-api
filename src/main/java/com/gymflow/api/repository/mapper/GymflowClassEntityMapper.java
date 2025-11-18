package com.gymflow.api.repository.mapper;

import com.gymflow.api.core.ClassSession;
import com.gymflow.api.core.GymflowClass;
import com.gymflow.api.repository.entity.ClassSessionEntity;
import com.gymflow.api.repository.entity.GymflowClassEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface GymflowClassEntityMapper {

    GymflowClass from(GymflowClassEntity entity);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromCore(GymflowClass source, @MappingTarget GymflowClassEntity target);

    ClassSession from(ClassSessionEntity entity);

    ClassSessionEntity from(ClassSession domain);

}
