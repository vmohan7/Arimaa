CREATE DATABASE IF NOT EXISTS Arimaa;

USE Arimaa;

CREATE TABLE IF NOT EXISTS players(
	id int,
	username varchar(255),
	title text,
	country varchar(255),
	player_type varchar(5),
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS games(
	id int,
	white_id int,
	black_id int,
	white_rating int,
	black_rating int, 
	white_ratingk int,
	black_ratingk int, 
	event_id int,
	site_id int,
	time_M int,
	time_R int,
	time_P int,
	time_L int,
	time_E int,
	time_T int,
	postal text,
	startts int,
	endts int,
	result varchar(1),
	termination_id int,
	corrupt int,
	w_state text,
	b_state text,
	movelist text,
	events text,
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS events(
	id int,
	event text,
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS sites(
	id int,
	site text,
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS terminations(
	id int,
	termination varchar(10),
	PRIMARY KEY(id)
);
