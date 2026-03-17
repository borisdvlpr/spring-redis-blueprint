package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.service.UserService;
import com.boris.springredisblueprint.service.command.CategoryCommandService;
import com.boris.springredisblueprint.service.command.PostCommandService;
import com.boris.springredisblueprint.service.command.TagCommandService;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import com.boris.springredisblueprint.service.query.PostQueryService;
import com.boris.springredisblueprint.service.query.TagQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.reset;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected PostQueryService postQueryService;

    @MockitoBean
    protected CategoryQueryService categoryQueryService;

    @MockitoBean
    protected TagQueryService tagQueryService;

    @MockitoBean
    protected PostCommandService postCommandService;

    @MockitoBean
    protected CategoryCommandService categoryCommandService;

    @MockitoBean
    protected TagCommandService tagCommandService;

    @MockitoBean
    protected UserService userService;

    @BeforeEach
    void resetSpies() {
        reset(
                postQueryService,
                categoryQueryService,
                tagQueryService,
                postCommandService,
                categoryCommandService,
                tagCommandService,
                userService
        );
    }
}
