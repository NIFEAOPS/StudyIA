package com.studyia.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.studyia.backend.model.Role;
import com.studyia.backend.model.Student;
import com.studyia.backend.model.User;
import com.studyia.backend.repository.AssessmentRepository;
import com.studyia.backend.repository.StudentRepository;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.service.AiRoadmapService;
import com.studyia.backend.service.AssessmentService;
import com.studyia.backend.web.EvaluateExamRequest;
import com.studyia.backend.web.GenerateExamRequest;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private com.studyia.backend.repository.RoadmapRepository roadmapRepository;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private AiRoadmapService aiRoadmapService;

    @Mock
    private com.studyia.backend.service.ProgressService progressService;

    @InjectMocks
    private AssessmentService assessmentService;

    private static void setId(User user, UUID value) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, value);
    }

    @Test
    void generateAssessmentPersistsQuestionSetAndCorrectAnswers() throws Exception {
        User user = new User("ana@studyia.dev", "hashed", "Ana", Role.STUDENT);
        setId(user, UUID.randomUUID());
        GenerateExamRequest request = new GenerateExamRequest(
                "backend",
                8,
                3,
                "Quiero trabajar como backend developer"
        );

        when(studentRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assessmentRepository.save(any(com.studyia.backend.model.Assessment.class)))
                .thenAnswer(invocation -> {
                    com.studyia.backend.model.Assessment assessment = invocation.getArgument(0);
                    assessment.setId(UUID.randomUUID());
                    return assessment;
                });
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"1\":\"A\"}");
        when(aiRoadmapService.generateAssessment(any(Student.class))).thenReturn(
                new AiRoadmapService.AiAssessmentResult(
                        List.of(new AiRoadmapService.AssessmentQuestion(1, "¿Qué es la JVM?",
                                List.of(
                                        new AiRoadmapService.AssessmentOption("A", "Máquina virtual de Java"),
                                        new AiRoadmapService.AssessmentOption("B", "Un compilador de CSS"),
                                        new AiRoadmapService.AssessmentOption("C", "Una base de datos"),
                                        new AiRoadmapService.AssessmentOption("D", "Un navegador")))),
                        Map.of(1, "A"))
        );

        var response = assessmentService.generateAssessment(user, request);

        assertNotNull(response);
        assertEquals(1, response.questions().size());
        assertEquals(UUID.class, response.assessmentId().getClass());
    }

    @Test
    void evaluateAssessmentCalculatesScoreAndBuildsRoadmap() throws Exception {
        User user = new User("luis@studyia.dev", "hashed", "Luis", Role.STUDENT);
        setId(user, UUID.randomUUID());
        var assessmentId = UUID.randomUUID();
        var request = new EvaluateExamRequest(assessmentId, List.of(
                new EvaluateExamRequest.AnswerRequest(1, "A")
        ));

        var assessment = new com.studyia.backend.model.Assessment(
                user,
                user.getName(),
                user.getEmail(),
                "backend",
                "Quiero trabajar en software",
                "{\"1\":\"A\"}",
                null
        );
        assessment.setId(assessmentId);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(studentRepository.findByUserId(user.getId())).thenReturn(Optional.of(
                new Student(user, user.getName(), user.getEmail(), "backend", 8, 3, "Quiero trabajar en software")));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roadmapRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(Map.of(1, "A"));
        when(aiRoadmapService.generateRoadmap(any(Student.class), any(String.class))).thenReturn(
                new AiRoadmapService.AiRoadmapResult("Roadmap inicial", "Resumen", "{}")
        );

        var result = assessmentService.evaluateAssessment(user, request);

        assertNotNull(result);
        assertEquals("Roadmap inicial", result.title());
    }
}
