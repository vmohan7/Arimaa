CREATE UNIQUE INDEX player_username_index ON players(username); 
CREATE INDEX player_country_index ON players(country); 
CREATE INDEX player_type_index ON players(player_type); 


CREATE INDEX white_id_index ON games(white_id); 
CREATE INDEX black_id_index ON games(black_id); 
CREATE INDEX white_rating_index ON games(white_rating); 
CREATE INDEX black_rating_index ON games(black_rating); 
CREATE INDEX white_ratingk_index ON games(white_ratingk); 
CREATE INDEX black_ratingk_index ON games(black_ratingk); 
CREATE INDEX event_index ON games(event_id); 
CREATE INDEX site_index ON games(site_id); 
CREATE INDEX termination_index ON games(termination_id); 
CREATE INDEX startts_index  ON games(startts); 
CREATE INDEX endts_index ON games(endts); 
CREATE INDEX result_index ON games(result); 
CREATE INDEX corrupt_index ON games(corrupt); 

