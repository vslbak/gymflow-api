package com.gymflow.api.repository.mapper;

import com.gymflow.api.core.GymflowUser;
import com.gymflow.api.repository.entity.GymflowUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface GymflowUserEntityMapper {

    GymflowUser from(GymflowUserEntity entity);

    @Mapping(target = "password", ignore = true)
    GymflowUserEntity from(GymflowUser domain);
}
