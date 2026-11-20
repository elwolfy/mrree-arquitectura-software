-- ==============================================================================
-- V2__semilla_oficinas.sql — 6 Oficinas consulares semilla del contrato global
-- ==============================================================================

INSERT INTO citas.oficina_consular (id, nombre, pais, ciudad, zona_horaria) VALUES
('MAD', 'Consulado General del Perú en Madrid (laboratorio)', 'ES', 'Madrid', 'Europe/Madrid'),
('BUE', 'Consulado General del Perú en Buenos Aires (laboratorio)', 'AR', 'Buenos Aires', 'America/Argentina/Buenos_Aires'),
('SCL', 'Consulado General del Perú en Santiago (laboratorio)', 'CL', 'Santiago', 'America/Santiago'),
('MIA', 'Consulado General del Perú en Miami (laboratorio)', 'US', 'Miami', 'America/New_York'),
('MIL', 'Consulado General del Perú en Milán (laboratorio)', 'IT', 'Milán', 'Europe/Rome'),
('TYO', 'Consulado General del Perú en Tokio (laboratorio)', 'JP', 'Tokio', 'Asia/Tokyo')
ON CONFLICT (id) DO NOTHING;

