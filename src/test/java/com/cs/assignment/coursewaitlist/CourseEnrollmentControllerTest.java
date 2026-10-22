package com.cs.assignment.coursewaitlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cs.assignment.coursewaitlist.dto.EnrollmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = "course.capacity=1")
class CourseEnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.update("DELETE FROM enrollment_records");
        jdbcTemplate.update("DELETE FROM sqlite_sequence WHERE name = 'enrollment_records'");
    }

    @Test
    void enrollsLearnerAndReturnsSuccessPayload() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest("spring-learner", Set.of("CS100"));

        mockMvc.perform(post("/api/courses/CS101/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("ENROLLED"));
    }

    @Test
    void returnsCourseEnrollmentState() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest("spring-learner-1", Set.of("CS100"));

        mockMvc.perform(post("/api/courses/CS101/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENROLLED"));

        mockMvc.perform(get("/api/courses/CS101/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("spring-learner-1"));
    }

    @Test
    void dropsLearnerAndListsWaitlist() throws Exception {
        mockMvc.perform(post("/api/courses/CS101/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest("drop-learner", Set.of("CS100")))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/courses/CS101/enrollments/drop-learner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DROPPED"));

        mockMvc.perform(get("/api/courses/CS101/enrollments"))
                .andExpect(status().isOk());
    }
}
