package com.boris.springredisblueprint.repository;

import com.boris.springredisblueprint.domain.PostStatus;
import com.boris.springredisblueprint.domain.entities.Category;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.Tag;
import com.boris.springredisblueprint.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatusAndCategoryAndTagsContaining(PostStatus status, Category category, Tag tag, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatusAndCategory(PostStatus status, Category category, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatusAndTagsContaining(PostStatus status, Tag tag, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags"})
    Page<Post> findAllByAuthorAndStatus(User author, PostStatus status, Pageable pageable);
}

