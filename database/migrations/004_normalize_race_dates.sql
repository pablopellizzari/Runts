-- Normaliza datas antigas gravadas como DD/MM/AAAA para o formato ISO usado nas consultas.
UPDATE race_events
SET date = to_char(to_date(date, 'DD/MM/YYYY'), 'YYYY-MM-DD')
WHERE date ~ '^[0-9]{2}/[0-9]{2}/[0-9]{4}$';
