CREATE TABLE IF NOT EXISTS app_user (
    id SERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS movie (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    director VARCHAR(255),
    release_year INT,
    file_size BIGINT,
    ranking INT,
    file_path VARCHAR(500),
    production SMALLINT,
    user_rating VARCHAR(20),
    last_update TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    user_id INT REFERENCES app_user(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS movie_availability (
    id SERIAL PRIMARY KEY,
    movie_id INT NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    platform VARCHAR(100) NOT NULL,
    UNIQUE(movie_id, platform)
);

