package pl.skf.sws.model;

public enum AvailabilityPlatform {
    NETFLIX,
    YOUTUBE,
    DISNEY,
    HBO;

    public static AvailabilityPlatform fromString(String value) {
        try {
            return AvailabilityPlatform.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown platform: " + value);
        }
    }

}
