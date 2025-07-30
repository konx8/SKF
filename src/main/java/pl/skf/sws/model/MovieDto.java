package pl.skf.sws.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovieDto {

    private String title;
    private String director;
    private Integer year;
    private Long sizeInBytes;

}
