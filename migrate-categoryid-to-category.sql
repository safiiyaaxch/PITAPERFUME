-- Migration: Rename categoryid column to category in product table
-- This migration renames the existing categoryid column to category to match the updated Java entity

-- Step 1: Rename the column
ALTER TABLE product CHANGE COLUMN categoryid category VARCHAR(100) NOT NULL;

-- Step 2: Verify the change
SHOW COLUMNS FROM product LIKE 'category%';
