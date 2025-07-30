INSERT INTO movie (title, director, release_year, file_size, ranking, production, user_rating, last_update)
VALUES ('Matrix', 'Lana Wachowski', 1999, 700000000, 350, 0, 'wybitny', CURRENT_TIMESTAMP);

INSERT INTO movie_availability (movie_id, platform) VALUES (1, 'netflix');
INSERT INTO movie_availability (movie_id, platform) VALUES (1, 'youtube');
