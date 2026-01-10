package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.admin.category.patch.CategoryPatchRequest;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto categoryDto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(CategoryPatchRequest patch, @MappingTarget Category category);
}
