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
import java.util.UUID;

@Entity
@Table(name = "roadmap_resources")
public class RoadmapResource {

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

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 40)
    private String type;

    @Column(nullable = false)
    private Integer position;

    protected RoadmapResource() {
    }

    public RoadmapResource(RoadmapStage stage, String title, String description, String url, String type, Integer position) {
        this.stage = stage;
        this.title = title;
        this.description = description;
        this.url = url;
        this.type = type;
        this.position = position;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public Integer getPosition() { return position; }
}
