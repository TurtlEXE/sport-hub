-- Migration script for adding approval columns to Facility table
-- Author: Antigravity

ALTER TABLE Facility ADD approved_by INT NULL;
ALTER TABLE Facility ADD approved_at DATETIME NULL;
ALTER TABLE Facility ADD rejection_reason NVARCHAR(500) NULL;

ALTER TABLE Facility ADD CONSTRAINT FK_Facility_ApprovedBy 
    FOREIGN KEY (approved_by) REFERENCES Account(account_id);
