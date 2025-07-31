INSERT INTO app_user (login, email) VALUES ('TestUser', 'testUser@example.com');
INSERT INTO app_user (login, email) VALUES ('Bob', 'Bob@example.com');
INSERT INTO app_user (login, email) VALUES ('Tom', 'Tom@example.com');

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
)VALUES
('Matrix', 'Lana Wachowski', 1999, 700000000, '/files/matrix.mp4', 1);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('Avengers', 'Joss Whedon', 2012, 1500000000, '/files/avengers.mp4', 1);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('Inception', 'Christopher Nolan', 2010, 1500000000, '/files/inception.mp4', 2);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('The Godfather', 'Francis Ford Coppola', 1972, 1800000000, '/files/godfather.mp4', 3);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('The Dark Knight', 'Christopher Nolan', 2008, 1400000000, '/files/darkknight.mp4', 2);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('Fight Club', 'David Fincher', 1999, 1100000000, '/files/fightclub.mp4', 1);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('Forrest Gump', 'Robert Zemeckis', 1994, 1000000000, '/files/forrestgump.mp4', 3);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('Gladiator', 'Ridley Scott', 2000, 1300000000, '/files/gladiator.mp4', 1);

INSERT INTO movie (
    title, director, release_year, file_size, file_path, user_id
) VALUES
('Titanic', 'James Cameron', 1997, 2000000000, '/files/titanic.mp4', 2);
