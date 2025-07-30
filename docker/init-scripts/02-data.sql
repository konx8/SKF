INSERT INTO app_user (login, email) VALUES ('TestUser', 'testUser@example.com');

INSERT INTO movie (
    title, director, release_year, file_size, ranking,
    production, user_rating, last_update, user_id
)
VALUES (
    'Matrix', 'Lana Wachowski', 1999, 700000000, 350,
    0, 'wybitny', CURRENT_TIMESTAMP, 1
);

INSERT INTO movie_availability (movie_id, platform) VALUES (1, 'netflix');
INSERT INTO movie_availability (movie_id, platform) VALUES (1, 'youtube');