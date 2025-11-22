-- Flyway migration: add branch_id to detalle_compra table
-- This allows tracking which branch a purchase item came from, enabling multi-provider purchases

-- Add branch_id column as nullable to not break existing data
ALTER TABLE detalle_compra
  ADD COLUMN branch_id BIGINT NULL AFTER producto_id;

-- Add foreign key constraint to branch table
ALTER TABLE detalle_compra
  ADD CONSTRAINT fk_detalle_compra_branch 
  FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_detalle_compra_branch_id ON detalle_compra(branch_id);

-- Optional: Update existing records to set branch_id from compra.branch_id if available
-- This is safe because it only updates records where compra has a branch_id
UPDATE detalle_compra dc
INNER JOIN compra c ON dc.compra_id = c.id
SET dc.branch_id = c.branch_id
WHERE c.branch_id IS NOT NULL AND dc.branch_id IS NULL;
