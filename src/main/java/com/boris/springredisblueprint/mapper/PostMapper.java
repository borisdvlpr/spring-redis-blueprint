package com.boris.springredisblueprint.mapper;

import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
import com.boris.springredisblueprint.model.dto.CreatePostRequestDto;
import com.boris.springredisblueprint.model.dto.PostDto;
import com.boris.springredisblueprint.model.dto.UpdatePostRequestDto;
import com.boris.springredisblueprint.model.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    PostDto toDto(Post post);

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    Post fromDto(PostDto postDto);

    CreatePostRequest toCreatePostRequest(CreatePostRequestDto dto);

    UpdatePostRequest toUpdatePostRequest(UpdatePostRequestDto dto);
}
