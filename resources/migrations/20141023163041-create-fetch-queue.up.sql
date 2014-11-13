
CREATE TABLE fetch_queue (
  id bigserial PRIMARY KEY,
  commit_hash varchar(40) NOT NULL CHECK(commit_hash ~ '^[0-9a-f]{40}'),
  repos_id integer NOT NULL,
  ref varchar(200) NOT NULL,
  attempts integer NOT NULL DEFAULT(0),
  urls varchar(200)[] NOT NULL DEFAULT '{}',
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (commit_hash,repos_id,ref,urls)
);

CREATE FUNCTION te_update_timestamp() RETURNS trigger AS $FN$
BEGIN
  NEW.updated := CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$FN$ LANGUAGE plpgsql;

CREATE FUNCTION te_ensure_fkey() RETURNS trigger AS $FN$
DECLARE
  col_name varchar;
  col_val bigint;
  foreign_tbl_name varchar;
  foreign_col_name varchar;
  cnt integer;
BEGIN
  col_name         := TG_ARGV[0];
  foreign_tbl_name := TG_ARGV[1];
  foreign_col_name := TG_ARGV[2];

  EXECUTE 'SELECT $1.' || quote_ident(col_name)
    USING NEW
    INTO col_val;

  EXECUTE 'SELECT 1 FROM ' || quote_ident(foreign_tbl_name)
    || ' WHERE ' || quote_ident(foreign_col_name) || '=$1 LIMIT 1'
    USING col_val;

  GET DIAGNOSTICS cnt = ROW_COUNT;
  IF cnt = 0 THEN
    RAISE 'Foreign key violation; no %.% matching %', foreign_tbl_name, foreign_col_name, col_name
      USING errcode='foreign_key_violation';
  END IF;
  RETURN NULL;
END;
$FN$ LANGUAGE plpgsql;

CREATE TRIGGER te_update_timestamp
  BEFORE UPDATE ON fetch_queue
  FOR EACH ROW
  EXECUTE PROCEDURE te_update_timestamp();

CREATE TRIGGER te_ensure_repo_fkey
  AFTER INSERT OR UPDATE ON fetch_queue
  FOR EACH ROW
  EXECUTE PROCEDURE te_ensure_fkey('repos_id', 'repos_latest', 'id');
