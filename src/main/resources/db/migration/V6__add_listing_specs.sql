ALTER TABLE listings ADD COLUMN spec_sheet_url VARCHAR(500);

CREATE TABLE listing_attributes (
    listing_id      BIGINT       NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    attribute_key   VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(500),
    PRIMARY KEY (listing_id, attribute_key)
);
