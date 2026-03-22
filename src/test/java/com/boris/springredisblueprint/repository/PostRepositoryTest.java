package com.boris.springredisblueprint.repository;

import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.model.entity.User;
import com.boris.springredisblueprint.model.type.PostStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PostRepository")
class PostRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User author;
    private Category categoryA;
    private Category categoryB;
    private Tag tagJava;
    private Tag tagSpring;

    private final Pageable firstPage = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        author = entityManager.persist(buildUser("Alice", "alice@example.com"));
        categoryA = entityManager.persist(buildCategory("Backend"));
        categoryB = entityManager.persist(buildCategory("Frontend"));
        tagJava = entityManager.persist(buildTag("java"));
        tagSpring = entityManager.persist(buildTag("spring"));

        entityManager.flush();
    }

    @Nested
    @DisplayName("findAllByStatus")
    class FindAllByStatus {

        @Test
        @DisplayName("should return only PUBLISHED posts")
        void findAllByStatus_returnsOnlyPublished() {
            persist(buildPost("Published Post", PostStatusEnum.PUBLISHED, categoryA));
            persist(buildPost("Draft Post", PostStatusEnum.DRAFT, categoryA));

            Page<Post> result = postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, firstPage);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Published Post");
        }

        @Test
        @DisplayName("should return empty page when no posts exist")
        void findAllByStatus_emptyWhenNoPosts() {
            Page<Post> result = postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, firstPage);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByStatusAndCategory")
    class FindAllByStatusAndCategory {

        @Test
        @DisplayName("should filter by status and category")
        void findAllByStatusAndCategory_filtersCorrectly() {
            persist(buildPost("Backend Published", PostStatusEnum.PUBLISHED, categoryA));
            persist(buildPost("Frontend Published", PostStatusEnum.PUBLISHED, categoryB));
            persist(buildPost("Backend Draft", PostStatusEnum.DRAFT, categoryA));

            Page<Post> result = postRepository.findAllByStatusAndCategory(
                    PostStatusEnum.PUBLISHED, categoryA, firstPage);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Backend Published");
        }

        @Test
        @DisplayName("should return empty when category has no published posts")
        void findAllByStatusAndCategory_emptyForWrongCategory() {
            persist(buildPost("Backend Published", PostStatusEnum.PUBLISHED, categoryA));

            Page<Post> result = postRepository.findAllByStatusAndCategory(
                    PostStatusEnum.PUBLISHED, categoryB, firstPage);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByStatusAndTagsContaining")
    class FindAllByStatusAndTagsContaining {

        @Test
        @DisplayName("should return posts that contain the given tag")
        void findAllByStatusAndTagsContaining_filtersCorrectly() {
            Post withJava = buildPost("Java Post", PostStatusEnum.PUBLISHED, categoryA);
            Post withSpring = buildPost("Spring Post", PostStatusEnum.PUBLISHED, categoryA);
            withJava.getTags().add(tagJava);
            withSpring.getTags().add(tagSpring);
            persist(withJava);
            persist(withSpring);

            Page<Post> result = postRepository.findAllByStatusAndTagsContaining(
                    PostStatusEnum.PUBLISHED, tagJava, firstPage);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Java Post");
        }

        @Test
        @DisplayName("should exclude drafts even when tag matches")
        void findAllByStatusAndTagsContaining_excludesDrafts() {
            Post draft = buildPost("Java Draft", PostStatusEnum.DRAFT, categoryA);
            draft.getTags().add(tagJava);
            persist(draft);

            Page<Post> result = postRepository.findAllByStatusAndTagsContaining(
                    PostStatusEnum.PUBLISHED, tagJava, firstPage);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByStatusAndCategoryAndTagsContaining")
    class FindAllByStatusAndCategoryAndTagsContaining {

        @Test
        @DisplayName("should narrow results by both category and tag")
        void findAllByStatusAndCategoryAndTagsContaining_filtersCorrectly() {
            Post backendJava = buildPost("Backend+Java", PostStatusEnum.PUBLISHED, categoryA);
            Post frontendJava = buildPost("Frontend+Java", PostStatusEnum.PUBLISHED, categoryB);
            Post backendSpring = buildPost("Backend+Spring", PostStatusEnum.PUBLISHED, categoryA);
            backendJava.getTags().add(tagJava);
            frontendJava.getTags().add(tagJava);
            backendSpring.getTags().add(tagSpring);
            persist(backendJava);
            persist(frontendJava);
            persist(backendSpring);

            Page<Post> result = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatusEnum.PUBLISHED, categoryA, tagJava, firstPage);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Backend+Java");
        }

        @Test
        @DisplayName("should return empty when no post matches category and tag combination")
        void findAllByStatusAndCategoryAndTagsContaining_emptyWhenNoMatch() {
            Post post = buildPost("Backend+Spring", PostStatusEnum.PUBLISHED, categoryA);
            post.getTags().add(tagSpring);
            persist(post);

            Page<Post> result = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatusEnum.PUBLISHED, categoryA, tagJava, firstPage);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByAuthorAndStatus")
    class FindAllByAuthorAndStatus {

        @Test
        @DisplayName("should return only the given author's posts with the given status")
        void findAllByAuthorAndStatus_filtersCorrectly() {
            User otherAuthor = entityManager.persist(buildUser("Bob", "bob@example.com"));
            entityManager.flush();

            persist(buildPost("Alice Draft", PostStatusEnum.DRAFT, categoryA, author));
            persist(buildPost("Alice Published", PostStatusEnum.PUBLISHED, categoryA, author));
            persist(buildPost("Bob Draft", PostStatusEnum.DRAFT, categoryA, otherAuthor));

            Page<Post> result = postRepository.findAllByAuthorAndStatus(
                    author, PostStatusEnum.DRAFT, firstPage);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Alice Draft");
        }

        @Test
        @DisplayName("should return empty when author has no posts with the given status")
        void findAllByAuthorAndStatus_emptyWhenNoMatch() {
            persist(buildPost("Alice Published", PostStatusEnum.PUBLISHED, categoryA, author));

            Page<Post> result = postRepository.findAllByAuthorAndStatus(
                    author, PostStatusEnum.DRAFT, firstPage);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("EntityGraph")
    class EntityGraph {

        @Test
        @DisplayName("should eagerly load author, category and tags associations")
        void entityGraph_associationsAreInitialised() {
            Post post = buildPost("Graph Test", PostStatusEnum.PUBLISHED, categoryA);
            post.getTags().add(tagJava);
            persist(post);

            // Detach everything so Hibernate cannot serve associations from the
            // first-level cache — only the EntityGraph can satisfy them.
            entityManager.clear();

            Page<Post> result = postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, firstPage);

            Post loaded = result.getContent().getFirst();
            // These would throw LazyInitializationException without the EntityGraph
            assertThat(loaded.getAuthor()).isNotNull();
            assertThat(loaded.getCategory()).isNotNull();
            assertThat(loaded.getTags()).isNotEmpty();
        }
    }

    private void persist(Post post) {
        entityManager.persist(post);
        entityManager.flush();
    }

    private Post buildPost(String title, PostStatusEnum status, Category category) {
        return buildPost(title, status, category, author);
    }

    private Post buildPost(String title, PostStatusEnum status, Category category, User postAuthor) {
        return Post.builder()
                .title(title)
                .content("Content for " + title)
                .readingTime(5)
                .status(status)
                .author(postAuthor)
                .category(category)
                .tags(new HashSet<>())
                .build();
    }

    private User buildUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .password("password")
                .build();
    }

    private Category buildCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    private Tag buildTag(String name) {
        return Tag.builder()
                .name(name)
                .build();
    }
}
