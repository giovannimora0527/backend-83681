ALTER TABLE cita ADD COLUMN duracion_minutos INT NOT NULL DEFAULT 30,
    ADD CONSTRAINT ck_cita_duracion CHECK (duracion_minutos BETWEEN 10 AND 240);

CREATE TABLE horario_medico (
    id BIGINT NOT NULL AUTO_INCREMENT,
    medico_id BIGINT NOT NULL,
    dia_semana TINYINT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activo BIT NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    KEY ix_horario_medico_dia (medico_id, dia_semana, activo),
    CONSTRAINT ck_horario_dia CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT ck_horario_rango CHECK (hora_fin > hora_inicio),
    CONSTRAINT fk_horario_medico FOREIGN KEY (medico_id) REFERENCES medico (id)
) ENGINE=InnoDB;
