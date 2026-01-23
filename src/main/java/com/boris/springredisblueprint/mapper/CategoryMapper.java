package com.boris.springredisblueprint.mapper;

import com.boris.springredisblueprint.domain.type.PostStatusEnum;
import com.boris.springredisblueprint.domain.dto.CategoryDto;
import com.boris.springredisblueprint.domain.dto.CreateCategoryRequestDto;
import com.boris.springredisblueprint.domain.entities.Category;
import com.boris.springredisblueprint.domain.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    CategoryDto toDTO(Category category);

    Category toEntity(CreateCategoryRequestDto createCategoryRequestDto);

    @Named("calculatePostCount")
    default long calculatePostCount(List<Post> posts) {
        if(posts == null) {
            return 0;
        }

        return posts.stream()
                .filter(post -> PostStatusEnum.PUBLISHED.equals(post.getStatus()))
                .count();
    }
}
