ALTER TABLE otp_token MODIFY codigo VARCHAR(64) NOT NULL;
UPDATE otp_token
SET codigo = LOWER(SHA2(codigo, 256))
WHERE CHAR_LENGTH(codigo) = 6;

UPDATE `session`
SET token = LOWER(SHA2(token, 256))
WHERE CHAR_LENGTH(token) > 64 OR token NOT REGEXP '^[a-f0-9]{64}$';
ALTER TABLE `session` MODIFY token CHAR(64) NOT NULL;
