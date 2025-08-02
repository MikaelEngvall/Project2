-- ========================================
-- DFRM - Add timestamp columns to apartments
-- Version: 2.0
-- Created: 2024-12-19
-- ========================================

-- Create function for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Add created_at and updated_at columns to apartments table
ALTER TABLE apartments 
ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Create trigger for updated_at
CREATE TRIGGER update_apartments_updated_at 
    BEFORE UPDATE ON apartments 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 