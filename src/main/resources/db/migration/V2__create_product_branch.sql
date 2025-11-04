-- Flyway migration: create product_branch table
CREATE TABLE IF NOT EXISTS product_branch (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  stock INT DEFAULT 0,
  stock_minimo INT DEFAULT 0,
  precio DOUBLE,
  activo BOOLEAN DEFAULT TRUE,
  ubicacion VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_product_branch UNIQUE (producto_id, branch_id)
) ENGINE=InnoDB;

-- Add foreign keys if the target tables exist (producto and branch)
ALTER TABLE product_branch
  ADD CONSTRAINT fk_pb_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE;

ALTER TABLE product_branch
  ADD CONSTRAINT fk_pb_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE;
