package pl.skf.sws.model;

public enum UserRating {

    MIERNY,
    DOBRY,
    WYBITNY,
    UNKNOWN;

    public static UserRating fromString(String value) {
        try{
            return UserRating.valueOf(value.trim().toUpperCase());
        } catch (Exception e){
            throw new IllegalArgumentException("Unknown rate: " + value);
        }
    }

}
