package com.studyia.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assessments")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String studentName;

    @Column(nullable = false, length = 320)
    private String studentEmail;

    @Column(nullable = false, length = 80)
    private String learningPath;

    @Column(nullable = false, length = 2000)
    private String professionalGoals;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctAnswersJson;

    @Column(columnDefinition = "TEXT")
    private String questionsJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Assessment() {
    }

    public Assessment(User user, String studentName, String studentEmail, String learningPath,
                      String professionalGoals, String correctAnswersJson, String questionsJson) {
        this.user = user;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
            this.learningPath = learningPath;
        this.professionalGoals = professionalGoals;
        this.correctAnswersJson = correctAnswersJson;
        this.questionsJson = questionsJson;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getLearningPath() { return learningPath; }
    public String getProfessionalGoals() { return professionalGoals; }
    public String getCorrectAnswersJson() { return correctAnswersJson; }
    public String getQuestionsJson() { return questionsJson; }
    public Instant getCreatedAt() { return createdAt; }
}
