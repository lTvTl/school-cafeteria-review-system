-- Read-only preflight. Run in the application's Oracle schema, not SYS/SYSTEM.
SET LINESIZE 200
SET PAGESIZE 100
SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN ('TBL_MENU', 'SEQ_MENU', 'IDX_MENU_CATEGORY');

SELECT column_name, data_type, data_length, data_precision, data_scale,
       nullable, data_default
FROM user_tab_columns WHERE table_name = 'TBL_MENU' ORDER BY column_id;

SELECT constraint_name, constraint_type, status, validated, search_condition
FROM user_constraints WHERE table_name = 'TBL_MENU';

SELECT constraint_name, column_name, position
FROM user_cons_columns WHERE table_name = 'TBL_MENU'
ORDER BY constraint_name, position;

SELECT index_name, column_name, column_position
FROM user_ind_columns WHERE table_name = 'TBL_MENU'
ORDER BY index_name, column_position;

SELECT sequence_name, min_value, max_value, increment_by, cycle_flag,
       cache_size, last_number
FROM user_sequences WHERE sequence_name = 'SEQ_MENU';

-- LAST_NUMBER is not necessarily the next value when a sequence is cached.
-- If TBL_MENU already exists, compare the above with 01_create_menu.sql.
-- Review existing IDs and sequence allocation before reusing that schema.
