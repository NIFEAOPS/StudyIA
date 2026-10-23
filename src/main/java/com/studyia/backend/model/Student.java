package com.studyia.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 80)
    private String learningPath;

    @Column(nullable = false)
    private Integer weeklyAvailabilityHours;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(nullable = false, length = 2000)
    private String professionalGoals;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "student_interests", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "interest", nullable = false, length = 120)
    private List<String> interests = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "student_current_knowledge", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "knowledge", nullable = false, length = 120)
    private List<String> currentKnowledge = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Student() {
    }

    public Student(String name, String email, Integer weeklyAvailabilityHours, String professionalGoals) {
        this(null, name, email, "general", weeklyAvailabilityHours, 3, professionalGoals);
    }

    public Student(User user, String name, String email, Integer weeklyAvailabilityHours, String professionalGoals) {
        this(user, name, email, "general", weeklyAvailabilityHours, 3, professionalGoals);
    }

    public Student(User user, String name, String email, String learningPath,
                   Integer weeklyAvailabilityHours, String professionalGoals) {
        this(user, name, email, learningPath, weeklyAvailabilityHours, 3, professionalGoals);
    }

    public Student(User user, String name, String email, String learningPath,
                   Integer weeklyAvailabilityHours, Integer durationMonths, String professionalGoals) {
        this(user, name, email, learningPath, weeklyAvailabilityHours, durationMonths,
                professionalGoals, List.of(), List.of());
    }

    public Student(User user, String name, String email, String learningPath,
                   Integer weeklyAvailabilityHours, Integer durationMonths, String professionalGoals,
                   List<String> interests, List<String> currentKnowledge) {
        this.user = user;
        this.name = name;
        this.email = email;
        this.learningPath = learningPath;
        this.weeklyAvailabilityHours = weeklyAvailabilityHours;
        this.durationMonths = durationMonths;
        this.professionalGoals = professionalGoals;
        this.interests = new ArrayList<>(interests == null ? List.of() : interests);
        this.currentKnowledge = new ArrayList<>(currentKnowledge == null ? List.of() : currentKnowledge);
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getLearningPath() { return learningPath; }
    public Integer getWeeklyAvailabilityHours() { return weeklyAvailabilityHours; }
    public Integer getDurationMonths() { return durationMonths; }
    public String getProfessionalGoals() { return professionalGoals; }
    public List<String> getInterests() { return List.copyOf(interests); }
    public List<String> getCurrentKnowledge() { return List.copyOf(currentKnowledge); }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setLearningPath(String learningPath) { this.learningPath = learningPath; }
    public void setWeeklyAvailabilityHours(Integer weeklyAvailabilityHours) { this.weeklyAvailabilityHours = weeklyAvailabilityHours; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }
    public void setProfessionalGoals(String professionalGoals) { this.professionalGoals = professionalGoals; }
    public void setInterests(List<String> interests) {
        this.interests = new ArrayList<>(interests == null ? List.of() : interests);
    }
    public void setCurrentKnowledge(List<String> currentKnowledge) {
        this.currentKnowledge = new ArrayList<>(currentKnowledge == null ? List.of() : currentKnowledge);
    }
}
