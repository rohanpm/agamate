CREATE SEQUENCE tests_seq;
CREATE TABLE tests (
       id integer DEFAULT nextval('tests_seq'),
       version integer DEFAULT 1,
       slug varchar(20),
       name varchar(200),
       definition json,
       PRIMARY KEY (id,version)
);
CREATE VIEW tests_latest AS
       SELECT * FROM tests WHERE tests.version=(
       	      SELECT MAX(i.version) FROM tests i WHERE i.id=tests.id
);

CREATE TEMPORARY TABLE dummy
  ON COMMIT DROP
  AS SELECT te_add_uniqueness_trigger('tests', 'slug');
