-- Migration för keys-tabellen
-- Skapar tabellen för nyckelhantering

CREATE TABLE keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series VARCHAR(50) NOT NULL,
    number VARCHAR(50) NOT NULL,
    copy VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'APARTMENT',
    apartment_id UUID REFERENCES apartments(id),
    tenant_id UUID REFERENCES tenants(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Unik kombination av serie, nummer och kopia
    UNIQUE(series, number, copy)
);

-- Skapa index för bättre prestanda
CREATE INDEX idx_keys_series ON keys(series);
CREATE INDEX idx_keys_number ON keys(number);
CREATE INDEX idx_keys_copy ON keys(copy);
CREATE INDEX idx_keys_type ON keys(type);
CREATE INDEX idx_keys_apartment_id ON keys(apartment_id);
CREATE INDEX idx_keys_tenant_id ON keys(tenant_id);
CREATE INDEX idx_keys_is_active ON keys(is_active);
CREATE INDEX idx_keys_created_at ON keys(created_at);
CREATE INDEX idx_keys_updated_at ON keys(updated_at);

-- Skapa composite index för vanliga sökningar
CREATE INDEX idx_keys_series_number_copy ON keys(series, number, copy);
CREATE INDEX idx_keys_apartment_type ON keys(apartment_id, type);
CREATE INDEX idx_keys_tenant_type ON keys(tenant_id, type);

-- Skapa trigger för att uppdatera updated_at automatiskt
CREATE OR REPLACE FUNCTION update_keys_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_keys_updated_at_column 
    BEFORE UPDATE ON keys 
    FOR EACH ROW 
    EXECUTE FUNCTION update_keys_updated_at_column();

-- Lägg till kommentarer för dokumentation
COMMENT ON TABLE keys IS 'Tabell för nyckelhantering i DFRM-systemet';
COMMENT ON COLUMN keys.id IS 'Unikt ID för nyckeln';
COMMENT ON COLUMN keys.series IS 'Nyckelserienummer';
COMMENT ON COLUMN keys.number IS 'Nyckelnummer';
COMMENT ON COLUMN keys.copy IS 'Kopianummer';
COMMENT ON COLUMN keys.type IS 'Typ av nyckel (APARTMENT, MASTER, GARAGE, STORAGE, LAUNDRY, MAILBOX, OTHER)';
COMMENT ON COLUMN keys.apartment_id IS 'Referens till lägenhet (kan vara NULL för huvudnycklar)';
COMMENT ON COLUMN keys.tenant_id IS 'Referens till hyresgäst som har nyckeln utlånad (NULL om inte utlånad)';
COMMENT ON COLUMN keys.is_active IS 'Om nyckeln är aktiv';
COMMENT ON COLUMN keys.notes IS 'Anteckningar om nyckeln';
COMMENT ON COLUMN keys.created_at IS 'När nyckeln skapades';
COMMENT ON COLUMN keys.updated_at IS 'När nyckeln senast uppdaterades'; 