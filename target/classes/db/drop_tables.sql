-- Drop tables in correct order to handle foreign key constraints
DROP TABLE IF EXISTS expense_splits;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS settlements;
DROP TABLE IF EXISTS user_group;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users; 