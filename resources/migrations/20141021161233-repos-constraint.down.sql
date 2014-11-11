CREATE TEMPORARY TABLE dummy 
  ON COMMIT DROP
  AS SELECT te_drop_uniqueness_trigger('repos', 'slug');
DROP FUNCTION te_add_uniqueness_trigger(tablename varchar, columnname varchar);
DROP FUNCTION te_drop_uniqueness_trigger(tablename varchar, columnname varchar);
DROP FUNCTION te_ensure_unique(tablename varchar, columnname varchar);
