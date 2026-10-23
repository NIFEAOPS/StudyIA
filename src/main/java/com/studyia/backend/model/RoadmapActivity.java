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
import java.util.UUID;

@Entity
@Table(name = "roadmap_activities")
public class RoadmapActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private RoadmapStage stage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String resourceUrl;

    @Column(nullable = false)
    private Integer position;

    private Integer estimatedMinutes;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    private Instant completedAt;

    protected RoadmapActivity() {
    }

    public RoadmapActivity(RoadmapStage stage, String title, String description,
                           String resourceUrl, Integer position, Integer estimatedMinutes) {
        this.stage = stage;
        this.title = title;
        this.description = description;
        this.resourceUrl = resourceUrl;
        this.position = position;
        this.estimatedMinutes = estimatedMinutes;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getResourceUrl() { return resourceUrl; }
    public Integer getPosition() { return position; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public String getStatus() { return status; }
    public Instant getCompletedAt() { return completedAt; }

    public void complete() {
        status = "COMPLETED";
        completedAt = Instant.now();
    }

    public void reopen() {
        status = "PENDING";
        completedAt = null;
    }
}
