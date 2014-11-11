CREATE TEMPORARY TABLE dummy
  ON COMMIT DROP
  AS SELECT te_drop_uniqueness_trigger('tests', 'slug');
DROP VIEW tests_latest;
DROP TABLE tests;
DROP SEQUENCE tests_seq;
