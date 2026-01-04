package com.boris.springredisblueprint.mapper;

import com.boris.springredisblueprint.domain.CreatePostRequest;
import com.boris.springredisblueprint.domain.UpdatePostRequest;
import com.boris.springredisblueprint.domain.dto.CreatePostRequestDTO;
import com.boris.springredisblueprint.domain.dto.PostDTO;
import com.boris.springredisblueprint.domain.dto.UpdatePostRequestDTO;
import com.boris.springredisblueprint.domain.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    PostDTO toDto(Post post);

    CreatePostRequest toCreatePostRequest(CreatePostRequestDTO dto);

    UpdatePostRequest toUpdatePostRequest(UpdatePostRequestDTO dto);
}
