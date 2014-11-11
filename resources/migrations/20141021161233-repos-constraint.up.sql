CREATE OR REPLACE FUNCTION te_ensure_unique(tablename varchar, columnname varchar) RETURNS void AS $PROC$
DECLARE
  dupe_count integer;
  stmt varchar;
BEGIN
  stmt := 'SELECT ' || quote_ident(columnname)
    || ' FROM ' || quote_ident(tablename)
    || ' GROUP BY ' || quote_ident(columnname)
    || ' HAVING COUNT(*) > 1';
  EXECUTE stmt;
  GET DIAGNOSTICS dupe_count = ROW_COUNT;
  IF dupe_count > 0 THEN
    RAISE 'Duplicate %', columnname USING errcode='unique_violation';
  END IF;
END;
$PROC$ LANGUAGE plpgsql;

CREATE FUNCTION te_add_uniqueness_trigger(tablename varchar, columnname varchar) RETURNS integer AS $PROC$
DECLARE
  fn_name varchar;
  latest_tablename varchar := tablename || '_latest';
BEGIN
  fn_name := 'ensure_' || tablename || '_' || columnname || '_unique';

  EXECUTE
    'CREATE FUNCTION ' || fn_name || $$() RETURNS trigger AS $FN$
    BEGIN
      PERFORM te_ensure_unique($$ || quote_literal(latest_tablename) || ',' || quote_literal(columnname) || $$);
      RETURN NULL;
    END;
    $FN$ LANGUAGE plpgsql
  $$;

  EXECUTE
    'CREATE TRIGGER ' || fn_name || ' AFTER INSERT OR UPDATE ON '
    || quote_ident(tablename) || ' EXECUTE PROCEDURE ' || fn_name || '()'
  ;

  RETURN NULL;
END;
$PROC$ LANGUAGE plpgsql;

CREATE FUNCTION te_drop_uniqueness_trigger(tablename varchar, columnname varchar) RETURNS integer AS $PROC$
DECLARE
  fn_name varchar;
BEGIN
  fn_name := 'ensure_' || tablename || '_' || columnname || '_unique';

  EXECUTE
    'DROP TRIGGER ' || fn_name || ' ON ' || quote_ident(tablename);

  EXECUTE
    'DROP FUNCTION ' || fn_name || '()';

  RETURN NULL;
END;
$PROC$ LANGUAGE plpgsql;

CREATE TEMPORARY TABLE dummy 
  ON COMMIT DROP
  AS SELECT te_add_uniqueness_trigger('repos', 'slug');
