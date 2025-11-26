DELETE FROM inventory_batches;
DELETE FROM products;

INSERT INTO products (id, name) VALUES
(1, 'Paracetamol 500mg'),
(2, 'Vitamin C Tablets'),
(3, 'Cough Syrup'),
(4, 'Hand Sanitizer');

INSERT INTO inventory_batches (batch_number, quantity, expiry_date, product_id) VALUES
('P-001', 50, '2025-02-01', 1),
('P-002', 120, '2025-01-15', 1),
('P-003', 80, '2024-12-10', 1),
('V-001', 200, '2025-05-20', 2),
('V-002', 150, '2025-03-18', 2),
('C-001', 40, '2024-11-11', 3),
('C-002', 70, '2025-01-05', 3),
('H-001', 300, '2026-12-31', 4);
