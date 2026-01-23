package com.boris.springredisblueprint.repository;

import com.boris.springredisblueprint.domain.type.PostStatusEnum;
import com.boris.springredisblueprint.domain.entities.Category;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.Tag;
import com.boris.springredisblueprint.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatusAndCategoryAndTagsContaining(PostStatusEnum status, Category category, Tag tag, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatusAndCategory(PostStatusEnum status, Category category, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatusAndTagsContaining(PostStatusEnum status, Tag tag, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatus(PostStatusEnum status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByAuthorAndStatus(User author, PostStatusEnum status, Pageable pageable);
}

