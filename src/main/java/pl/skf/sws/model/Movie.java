package pl.skf.sws.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movie")
@Data
@NoArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String director;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "file_size")
    private Long fileSize;

    private Integer ranking;

    @Column(name = "file_path", length = 500)
    private String filePath;

    private Integer production;

    @Column(name = "user_rating", length = 20)
    private String userRating;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<MovieAvailability> availabilities;

}
