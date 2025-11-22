-- Flyway migration: add branch_id to detalle_venta table
-- This allows tracking which branch a sale item came from, similar to detalle_compra

-- Add branch_id column as nullable to not break existing data
ALTER TABLE detalle_venta
  ADD COLUMN branch_id BIGINT NULL AFTER producto_id;

-- Add foreign key constraint to branch table
ALTER TABLE detalle_venta
  ADD CONSTRAINT fk_detalle_venta_branch 
  FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_detalle_venta_branch_id ON detalle_venta(branch_id);

-- Optional: Update existing records to set branch_id from venta.branch_id if available
-- This is safe because it only updates records where venta has a branch_id
UPDATE detalle_venta dv
INNER JOIN venta v ON dv.venta_id = v.id
SET dv.branch_id = v.branch_id
WHERE v.branch_id IS NOT NULL AND dv.branch_id IS NULL;
