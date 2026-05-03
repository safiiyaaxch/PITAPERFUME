-- Insert test users for customers
INSERT INTO users (username, email, password, role, created_date, updated_date) 
VALUES ('member_customer', 'member@test.com', 'password123', 'customer', NOW(), NOW());

INSERT INTO users (username, email, password, role, created_date, updated_date) 
VALUES ('non_member_customer', 'nonmember@test.com', 'password123', 'customer', NOW(), NOW());

-- Insert test customers
-- Customer with membership (can access vouchers)
INSERT INTO customer (userid, fullname, phone, address, city, country, preferred_scent_type, loyalty_points, is_member, created_date, updated_date) 
VALUES (
  (SELECT userid FROM users WHERE username = 'member_customer'),
  'John Member',
  '0123456789',
  '123 Member Street',
  'Kuala Lumpur',
  'Malaysia',
  'Floral',
  500,
  true,
  NOW(),
  NOW()
);

-- Customer without membership (cannot access vouchers)
INSERT INTO customer (userid, fullname, phone, address, city, country, preferred_scent_type, loyalty_points, is_member, created_date, updated_date) 
VALUES (
  (SELECT userid FROM users WHERE username = 'non_member_customer'),
  'Jane Non-Member',
  '9876543210',
  '456 Regular Avenue',
  'Selangor',
  'Malaysia',
  'Oriental',
  0,
  false,
  NOW(),
  NOW()
);

-- Verify insertions
SELECT 
  u.userid,
  u.username,
  u.email,
  c.fullname,
  c.is_member,
  c.loyalty_points
FROM users u
LEFT JOIN customer c ON u.userid = c.userid
WHERE u.role = 'customer' AND u.username LIKE '%_customer'
ORDER BY u.userid DESC;
