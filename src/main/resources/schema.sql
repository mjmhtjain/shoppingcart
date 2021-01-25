DROP TABLE IF EXISTS cart;

CREATE TABLE IF NOT EXISTS cart (
	id serial PRIMARY KEY,
	cartid Integer NOT NULL,
	itemid Integer NOT NULL,
	quantity Integer NOT NULL,
	itemmetadata VARCHAR ( 256 )
);


INSERT INTO cart(cartid, itemid, quantity, itemmetadata)
	VALUES (1, 1, 2 , '{"itemName" : "item1"}');

INSERT INTO cart(cartid, itemid, quantity, itemmetadata)
	VALUES (1, 2, 2 , '{"itemName" : "item2"}');