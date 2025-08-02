package pl.skf.sws.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoviePatchDto {

    private String title;

    private String director;

    private Integer releaseYear;

}
