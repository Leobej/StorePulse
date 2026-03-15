CREATE TABLE user_store_access (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    store_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_user_store_access_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_user_store_access_store FOREIGN KEY (store_id) REFERENCES store(id)
);

CREATE UNIQUE INDEX uk_user_store_access_user_store ON user_store_access(user_id, store_id);

INSERT INTO user_store_access (id, user_id, store_id, created_at)
SELECT RANDOM_UUID(), id, store_id, CURRENT_TIMESTAMP
FROM app_user;
