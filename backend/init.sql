-- Table: users
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: carver_matrices
CREATE TABLE IF NOT EXISTS carver_matrices (
    matrix_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id),
    name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    hosts TEXT[],
    participants TEXT[]
);

-- Table: carver_items
CREATE TABLE IF NOT EXISTS carver_items (
    item_id SERIAL PRIMARY KEY,
    matrix_id INT NOT NULL REFERENCES carver_matrices(matrix_id) ON DELETE CASCADE,
    item_name VARCHAR(255) NOT NULL,
    criticality INT,
    accessibility INT,
    recoverability INT,
    vulnerability INT,
    effect INT,
    recognizability INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: matrix_images
CREATE TABLE IF NOT EXISTS matrix_images (
    image_id SERIAL PRIMARY KEY,
    matrix_id INT NOT NULL REFERENCES carver_matrices(matrix_id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
