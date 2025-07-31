package pl.skf.sws.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class MovieDto {

    @NotBlank
    private String title;
    @NotBlank
    private String director;
    @NotNull
    private Integer releaseYear;

}
