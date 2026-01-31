package com.example.assignments_organizer;

public class DaySummary {

    private int hardDuration, mediumDuration, easyDuration;

    public DaySummary() {
        hardDuration = 0;
        mediumDuration = 0;
        easyDuration = 0;
    }

    public void setDuration(int duration, Difficulty difficulty) {
        switch (difficulty) {
            case HARD:
                hardDuration = duration;
                break;
            case MEDIUM:
                mediumDuration = duration;
                break;
            case EASY:
                easyDuration = duration;
                break;
            default:
                throw new RuntimeException("No difficulty provided");
        };
    }

    public int getDuration(Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> hardDuration;
            case MEDIUM -> mediumDuration;
            case EASY -> easyDuration;
            default -> throw new RuntimeException("No difficulty provided");
        };
    }

    public void subtractFromDuration(int decrement, Difficulty difficulty) {
        switch (difficulty) {
            case HARD:
                hardDuration -= decrement;
                break;
            case MEDIUM:
                mediumDuration -= decrement;
                break;
            case EASY:
                easyDuration -= decrement;
                break;
            default:
                throw new RuntimeException("No difficulty provided");
        };
    }

    public void addToDuration(int increment, Difficulty difficulty) {
        subtractFromDuration(-increment, difficulty);
    }

}
