package pl.skf.sws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.skf.sws.model.enums.AvailabilityPlatform;
import pl.skf.sws.model.enums.ProductionType;
import pl.skf.sws.model.enums.UserRating;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
public class DigiKatResponse {

    @JsonProperty("tytul")
    private String title;

    @JsonProperty("produkcja")
    private Integer production;

    @JsonProperty("dostepnosc")
    private List<String> availability;

    @JsonProperty("ocenaUzytkwonikow")
    private String userRating;

    @JsonProperty("ostaniaAktualizacja")
    private String lastUpdate;

    public ProductionType getProductionType() {
        if (this.production == null) {
            return ProductionType.UNKNOWN;
        }
        return ProductionType.fromCode(this.production);
    }

    public List<AvailabilityPlatform> getAvailabilityPlatforms() {
        if (this.availability == null) {
            return Collections.emptyList();
        }
        return availability.stream()
                .map(AvailabilityPlatform::fromString)
                .filter(Objects::nonNull)
                .toList();
    }

    public UserRating getUserRatingEnum() {
        if (this.userRating == null || this.userRating.isBlank()) {
            return UserRating.UNKNOWN;
        }
        return UserRating.fromString(this.userRating);
    }



}
