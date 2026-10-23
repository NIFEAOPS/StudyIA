package com.studyia.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmap_stages")
public class RoadmapStage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false)
    private Integer stageNumber;

    @Column
    private Integer monthNumber;

    @Column
    private Integer weekOfMonth;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, length = 2000)
    private String objective;

    @Column(nullable = false, length = 20)
    private String status;

    private Instant completedAt;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<RoadmapActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<RoadmapResource> resources = new ArrayList<>();

    protected RoadmapStage() {
    }

    public RoadmapStage(Roadmap roadmap, Integer stageNumber, String title, String objective) {
        this(roadmap, stageNumber, null, null, title, objective);
    }

    public RoadmapStage(Roadmap roadmap, Integer stageNumber, Integer monthNumber,
                        Integer weekOfMonth, String title, String objective) {
        this.roadmap = roadmap;
        this.stageNumber = stageNumber;
        this.monthNumber = monthNumber;
        this.weekOfMonth = weekOfMonth;
        this.title = title;
        this.objective = objective;
        this.status = stageNumber == 1 ? "AVAILABLE" : "LOCKED";
    }

    public UUID getId() { return id; }
    public Integer getStageNumber() { return stageNumber; }
    public Integer getMonthNumber() { return monthNumber; }
    public Integer getWeekOfMonth() { return weekOfMonth; }
    public String getTitle() { return title; }
    public String getObjective() { return objective; }
    public String getStatus() { return status; }
    public Instant getCompletedAt() { return completedAt; }
    public List<RoadmapActivity> getActivities() { return activities; }
    public List<RoadmapResource> getResources() { return resources; }

    public void addActivity(RoadmapActivity activity) {
        activities.add(activity);
    }

    public void addResource(RoadmapResource resource) {
        resources.add(resource);
    }

    public void setStatus(String status) {
        this.status = status;
        if ("COMPLETED".equals(status)) {
            this.completedAt = Instant.now();
        }
    }
}
