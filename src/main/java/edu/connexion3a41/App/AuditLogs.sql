-- First, modify the existing table to add the needed columns
ALTER TABLE audit_log
ADD COLUMN ip_address VARCHAR(45),
ADD COLUMN status VARCHAR(20) DEFAULT 'SUCCESS',
ADD COLUMN user_agent TEXT;

-- Add indexes for better performance
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_user_email ON audit_log(user_email);
CREATE INDEX idx_audit_log_date ON audit_log(date);

-- Keep all your existing triggers exactly as they were
DELIMITER //

CREATE TRIGGER after_user_insert
AFTER INSERT ON user
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, user_email, date, details)
    VALUES ('USER_ADDED', NEW.email, NOW(), CONCAT('New user added: ', NEW.username));
END;
//

CREATE TRIGGER after_user_update
AFTER UPDATE ON user
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, user_email, date, details)
    VALUES ('USER_UPDATED', NEW.email, NOW(), CONCAT('User updated: ', NEW.username));
END;
//

CREATE TRIGGER after_user_delete
AFTER DELETE ON user
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, user_email, date, details)
    VALUES ('USER_DELETED', OLD.email, NOW(), CONCAT('User deleted: ', OLD.username));
END;
//

-- Add new trigger specifically for login tracking
CREATE TRIGGER after_login_track
AFTER UPDATE ON user
FOR EACH ROW
BEGIN
    -- Check if this is a login update (assuming your login process updates last_login)
    IF OLD.last_login IS NULL OR OLD.last_login != NEW.last_login THEN
        INSERT INTO audit_log (action, user_email, date, details, ip_address, status)
        VALUES ('LOGIN', NEW.email, NOW(),
               CONCAT('User logged in from IP: ', NEW.last_login_ip),
               NEW.last_login_ip, 'SUCCESS');
    END IF;
END;
//

-- Add procedure for failed login attempts
CREATE PROCEDURE log_failed_login(IN p_email VARCHAR(255), IN p_ip VARCHAR(45), IN p_reason VARCHAR(255))
BEGIN
    INSERT INTO audit_log (action, user_email, date, details, ip_address, status)
    VALUES ('LOGIN_FAILED', p_email, NOW(),
           CONCAT('Failed login attempt - Reason: ', p_reason),
           p_ip, 'FAILED');
END;
//

DELIMITER ;