-- ============================================================
-- 遷移腳本：修復 expense_records.sales_order_id 長度 + 恢復全文索引
-- 數據庫：MariaDB 10.11 / mc_sport
-- 日期：2026-10-08
--
-- 【背景】此腳本有兩件事要修：
--   1. sales_order_id 為 varchar(50)，長單號寫入報 1406
--      「Data too long for column」→ 擴展至 varchar(255)
--   2. 全文索引 ft_expense_record 在先前一次失敗的遷移中被 DROP
--      且重建失敗，目前【處於缺失狀態】，搜索功能是壞的 → 需重建
--
-- 【為什麼不能用 WITH PARSER Mroonga】
--   該實例的 Mroonga 僅註冊為 STORAGE ENGINE，而非 FULLTEXT PARSER：
--       SELECT PLUGIN_NAME, PLUGIN_TYPE, PLUGIN_STATUS
--       FROM information_schema.PLUGINS WHERE PLUGIN_NAME='Mroonga';
--       → PLUGIN_TYPE = 'STORAGE ENGINE'
--   故 MariaDB 找不到名為 Mroonga 的 parser，重建時報：
--       ERROR 1128: Function 'Mroonga' is not defined
--   本腳本改用內建 InnoDB 全文索引，不需要 root / INSTALL SONAME 權限。
--
-- 【關鍵：執行順序】
--   先 ALTER 再 CREATE。若反過來先建索引，MariaDB 允許在 FULLTEXT
--   索引存在的情況下直接 MODIFY 欄位（實測 10.11 確實允許，索引與
--   MATCH() 結果均不受影響），但先改欄位再建索引語義更乾淨：
--   索引一次性建立在最終欄位寬度上，不存在任何寬度不一致的窗口期。
-- ============================================================


-- ------------------------------------------------------------
-- 步驟 1：執行前確認現況
-- ------------------------------------------------------------
SELECT '--- 欄位長度（預期 varchar(50)）---' AS ``;
SELECT COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='mc_sport' AND TABLE_NAME='expense_records'
  AND COLUMN_NAME='sales_order_id';

SELECT '--- 全文索引（預期：查不到，因為已被誤刪）---' AS ``;
SELECT INDEX_NAME, INDEX_TYPE, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS cols
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA='mc_sport' AND TABLE_NAME='expense_records'
  AND INDEX_TYPE='FULLTEXT'
GROUP BY INDEX_NAME, INDEX_TYPE;

SELECT '--- 資料量與現有髒資料檢查 ---' AS ``;
SELECT COUNT(*) AS total_rows, MAX(CHAR_LENGTH(sales_order_id)) AS max_so_len
FROM expense_records;


-- ------------------------------------------------------------
-- 步驟 2：備份（強烈建議）
-- ------------------------------------------------------------
-- CREATE TABLE expense_records_bak_20261008 AS SELECT * FROM expense_records;


-- ------------------------------------------------------------
-- 步驟 3：執行修復 —— 兩條語句，按序
-- ------------------------------------------------------------

-- 3a. 擴展欄位長度（此時無全文索引，零風險）
ALTER TABLE expense_records
  MODIFY COLUMN sales_order_id VARCHAR(255) NULL DEFAULT NULL;

-- 3b. 重建全文索引（恢復搜索功能）
CREATE FULLTEXT INDEX ft_expense_record
  ON expense_records (shipping_number, sales_order_id);


-- ------------------------------------------------------------
-- 步驟 4：驗證
-- ------------------------------------------------------------
-- 4a. 欄位應為 varchar(255)
SELECT COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='mc_sport' AND TABLE_NAME='expense_records'
  AND COLUMN_NAME='sales_order_id';

-- 4b. 全文索引應已恢復
SELECT INDEX_NAME, INDEX_TYPE, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS cols
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA='mc_sport' AND TABLE_NAME='expense_records'
  AND INDEX_TYPE='FULLTEXT'
GROUP BY INDEX_NAME, INDEX_TYPE;

-- 4c. 搜索煙霧測試：換成一個確實存在的運單號或銷售單號
-- SELECT id, sales_order_id, shipping_number FROM expense_records
--  WHERE MATCH(shipping_number, sales_order_id) AGAINST ('<已存在的單號>');

-- 4d. 長單號寫入煙霧測試（應不再報 1406）
-- INSERT INTO expense_records (sales_order_id, shipping_number, company_name,
--   expense_type, expense_amount, handler, recorder, status, expense_date,
--   created_date, updated_date)
-- VALUES ('<一個超過50字符的單號>','SF-TEST','McSport','速遞費',1.00,
--   <handler_id>,<recorder_id>,'pending',NOW(6),NOW(6),NOW(6));
-- 測完請 DELETE 掉該筆測試資料。


-- ============================================================
-- 【搜索品質提醒】內建 InnoDB 全文索引與 Mroonga 行為不同
-- ------------------------------------------------------------
-- 應用層查詢為 MATCH(...) AGAINST(:text)，未加 IN BOOLEAN MODE，
-- 走的是自然語言模式，有兩個已知限制（實測確認）：
--
--   1. ft_min_word_len = 4：長度 < 4 的 token 會被忽略。
--      例：單號 'SO-2026-001' 切出的 'so' 不參與匹配。
--
--   2. 50% 閾值：出現在超過一半行數中的 token 會被直接丟棄。
--      例：若全表多數單號都含 '2026'，該 token 不參與匹配，
--          可能導致搜索結果為空或漏掉本該命中的記錄。
--
-- 綜合效果：搜索為「分詞後的多候選模糊匹配」，而非精確子串查找。
-- 例：搜 'SO-2026-001' 可能同時返回 SO-2026-002 等相近記錄。
-- 若業務上需要精確按單號查找，建議後續把查詢改為
--   ... WHERE sales_order_id = :t OR shipping_number = :t
--   ... WHERE sales_order_id LIKE :t OR shipping_number LIKE :t
-- 這屬於另一個需求，本次遷移未一併改動。
-- ============================================================