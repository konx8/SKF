package pl.skf.sws.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_availability",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"movie_id", "platform"})
})
@Data
@NoArgsConstructor
public class MovieAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference
    private Movie movie;

    @Column(length = 100, nullable = false)
    private String platform;

}
