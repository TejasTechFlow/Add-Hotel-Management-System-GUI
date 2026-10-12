INSERT INTO users (username, password, role, full_name, email, phone, is_active)
VALUES (
  'tejas123',
  SHA2('1234', 256),
  'ADMIN',
  'Tejas Yadav',
  'tejas123@gmail.com',
  '9999999999',
  1
);