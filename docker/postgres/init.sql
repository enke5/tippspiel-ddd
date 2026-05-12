-- Creates the two application databases
-- (postgres superuser runs this at container start)

CREATE DATABASE tournament_management;
CREATE DATABASE betting_scoring;

GRANT ALL PRIVILEGES ON DATABASE tournament_management TO tippspiel;
GRANT ALL PRIVILEGES ON DATABASE betting_scoring TO tippspiel;
