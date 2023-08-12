create table Post (
	id serial primary key,
	name text,
	text text,
	link text UNIQUE,
	created Date 
);