package com.studyia.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmaps")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(length = 20)
    private String level;

    @Column(length = 4000)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String structuredContent;

    @Column(nullable = false, updatable = false)
    private Instant generatedAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stageNumber ASC")
    private List<RoadmapStage> stages = new ArrayList<>();

    protected Roadmap() {
    }

    public Roadmap(Student student, String title, String summary, String structuredContent) {
        this(student, null, title, summary, structuredContent);
    }

    public Roadmap(Student student, String level, String title, String summary, String structuredContent) {
        this.student = student;
        this.level = level;
        this.title = title;
        this.summary = summary;
        this.structuredContent = structuredContent;
        this.generatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getLevel() { return level; }
    public String getSummary() { return summary; }
    public String getStructuredContent() { return structuredContent; }
    public Instant getGeneratedAt() { return generatedAt; }
    public List<RoadmapStage> getStages() { return stages; }
    public Student getStudent() { return student; }
    public UUID getStudentId() { return student.getId(); }

    public void addStage(RoadmapStage stage) {
        stages.add(stage);
    }
}
