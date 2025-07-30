package pl.skf.sws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

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

}
