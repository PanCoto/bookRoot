package pl.studyshare.enums;

/**
 * Types of tasks/questions in the bookRoot platform.
 * Corresponds to YAML spec: task_types
 */
public enum TaskType {
    /** Open-ended question requiring a free-form answer */
    OPEN,
    /** Closed question with a single correct answer */
    CLOSED,
    /** Multiple choice question (multiple correct options possible) */
    MULTIPLE_CHOICE,
    /** True/False question */
    TRUE_FALSE
}
