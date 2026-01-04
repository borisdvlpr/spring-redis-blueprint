package com.boris.springredisblueprint.mapper;

import com.boris.springredisblueprint.domain.PostStatus;
import com.boris.springredisblueprint.domain.dto.TagDTO;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCounts")
    TagDTO toTagResponse(Tag tag);

    @Named("calculatePostCounts")
    default Integer calculatePostCounts(Set<Post> posts) {
        if (posts == null) {
            return 0;
        }

        return (int) posts.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }
}
