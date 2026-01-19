package com.boris.springredisblueprint.repository;

import com.boris.springredisblueprint.domain.PostStatus;
import com.boris.springredisblueprint.domain.entities.Category;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.Tag;
import com.boris.springredisblueprint.domain.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @EntityGraph(attributePaths = {"author", "category", "tags"})
    List<Post> findAllByStatusAndCategoryAndTagsContaining(PostStatus status, Category category, Tag tag);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    List<Post> findAllByStatusAndCategory(PostStatus status, Category category);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    List<Post> findAllByStatusAndTagsContaining(PostStatus status, Tag tag);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    List<Post> findAllByStatus(PostStatus status);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    List<Post> findAllByAuthorAndStatus(User author, PostStatus status);
}
