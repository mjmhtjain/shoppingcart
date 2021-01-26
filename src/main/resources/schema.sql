DROP TABLE IF EXISTS cart;

CREATE TABLE IF NOT EXISTS cart (
	id serial PRIMARY KEY,
	cartid Integer NOT NULL,
	itemid Integer NOT NULL,
	quantity Integer NOT NULL,
	itemmetadata VARCHAR ( 256 )
);
