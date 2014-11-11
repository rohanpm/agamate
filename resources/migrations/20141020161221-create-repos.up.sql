CREATE SEQUENCE repos_seq;
CREATE TABLE repos (
       id integer DEFAULT nextval('repos_seq'),
       version integer DEFAULT 1,
       slug varchar(20),
       name varchar(200),
       "canonical-url" varchar(500),
       PRIMARY KEY (id,version)
);
CREATE VIEW repos_latest AS
       SELECT * FROM repos WHERE repos.version=(
       	      SELECT MAX(i.version) FROM repos i WHERE i.id=repos.id
);
