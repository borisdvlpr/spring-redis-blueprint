package com.boris.springredisblueprint.mapper;

import com.boris.springredisblueprint.model.type.PostStatusEnum;
import com.boris.springredisblueprint.model.dto.TagDto;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCounts")
    TagDto toDto(Tag tag);

    @Named("calculatePostCounts")
    default Integer calculatePostCounts(Set<Post> posts) {
        if (posts == null) {
            return 0;
        }

        return (int) posts.stream()
                .filter(post -> PostStatusEnum.PUBLISHED.equals(post.getStatus()))
                .count();
    }
}
