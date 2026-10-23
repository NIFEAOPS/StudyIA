package com.studyia.backend.web;

import com.studyia.backend.model.Roadmap;
import com.studyia.backend.model.RoadmapActivity;
import com.studyia.backend.model.RoadmapStage;
import com.studyia.backend.model.RoadmapResource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        RoadmapSummary roadmap,
        Metrics metrics,
        List<Stage> stages
) {
    public static DashboardResponse from(Roadmap roadmap, long streakDays) {
        List<Stage> stages = roadmap.getStages().stream().map(Stage::from).toList();
        int total = stages.stream().mapToInt(stage -> stage.activities().size()).sum();
        int completed = stages.stream().mapToInt(stage -> (int) stage.activities().stream()
                .filter(activity -> "COMPLETED".equals(activity.status())).count()).sum();
        int currentStage = stages.stream()
                .filter(stage -> !"COMPLETED".equals(stage.status()))
                .mapToInt(Stage::stageNumber)
                .findFirst()
                .orElse(stages.size());
        return new DashboardResponse(
                new RoadmapSummary(roadmap.getId(), roadmap.getStudentId(), roadmap.getTitle(),
                        roadmap.getLevel(), roadmap.getSummary(), roadmap.getStructuredContent(), roadmap.getStudent().getDurationMonths(),
                        roadmap.getGeneratedAt()),
                new Metrics(total == 0 ? 0 : Math.round(completed * 100f / total), currentStage,
                        stages.size(), completed, total, streakDays),
                stages);
    }

        public record RoadmapSummary(UUID id, UUID studentId, String title, String level, String summary,
                                 String structuredContent, Integer durationMonths, Instant generatedAt) {}

    public record Metrics(int progressPercentage, int currentStage, int totalStages,
                          int completedActivities, int totalActivities, long currentStreakDays) {}

    public record Stage(UUID id, int stageNumber, Integer monthNumber, Integer weekOfMonth,
                        String title, String objective,
                        String status, int progressPercentage, List<Activity> activities,
                        List<Resource> resources) {
        static Stage from(RoadmapStage stage) {
            List<Activity> activities = stage.getActivities().stream().map(Activity::from).toList();
                List<Resource> resources = stage.getResources().stream().map(Resource::from).toList();
            int completed = (int) activities.stream().filter(activity -> "COMPLETED".equals(activity.status())).count();
            return new Stage(stage.getId(), stage.getStageNumber(), stage.getMonthNumber(), stage.getWeekOfMonth(),
                    stage.getTitle(), stage.getObjective(),
                    stage.getStatus(), activities.isEmpty() ? 0 : Math.round(completed * 100f / activities.size()),
                    activities, resources);
        }

    }

    public record Resource(UUID id, String title, String description, String url, String type, int position) {
        static Resource from(RoadmapResource resource) {
            return new Resource(resource.getId(), resource.getTitle(), resource.getDescription(),
                    resource.getUrl(), resource.getType(), resource.getPosition());
        }
    }

    public record Activity(UUID id, String title, String description, String resourceUrl, int position,
                           Integer estimatedMinutes, String status, Instant completedAt) {
        static Activity from(RoadmapActivity activity) {
            return new Activity(activity.getId(), activity.getTitle(), activity.getDescription(),
                    activity.getResourceUrl(), activity.getPosition(), activity.getEstimatedMinutes(),
                    activity.getStatus(), activity.getCompletedAt());
        }
    }
}
