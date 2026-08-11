CREATE TABLE conversations (
    id                     BIGSERIAL PRIMARY KEY,
    listing_id             BIGINT    NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    initiator_company_id   BIGINT    NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    created_at             TIMESTAMP NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP NOT NULL DEFAULT now(),
    -- Ayni firma ayni ilan icin ikinci bir gorusme acamaz (get-or-create'in DB seviyesinde garantisi).
    CONSTRAINT uq_conversation_listing_initiator UNIQUE (listing_id, initiator_company_id)
);

CREATE INDEX idx_conversations_initiator_company_id ON conversations (initiator_company_id);
CREATE INDEX idx_conversations_listing_id ON conversations (listing_id);

CREATE TABLE messages (
    id                BIGSERIAL PRIMARY KEY,
    conversation_id   BIGINT       NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    sender_id         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content           VARCHAR(4000) NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);
