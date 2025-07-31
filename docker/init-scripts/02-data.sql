INSERT INTO app_user (login, email) VALUES ('TestUser', 'testUser@example.com');

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
)
VALUES (
    'Matrix', 'Lana Wachowski', 1999, 700000000, '/files/matrix.mp4', 1
);