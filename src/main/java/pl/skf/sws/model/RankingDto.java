package pl.skf.sws.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RankingDto {

    private String title;

    private Integer releaseYear;

    private Integer ranking;

    private Integer production;

    private List<String> availability;

    private String userRating;

}
