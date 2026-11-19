# Matriz de Optimización de Consultas — Módulo 3 (`m3-datos`)

> **Laboratorio de la Sesión 5 (Contenido 3.4 — Optimización de Consultas, Índices y Paginación)**
> Ejecute primero `.\m3-datos\scripts\cargar-datos-masivos.ps1` (1 000 000 de filas en `tramites.vista_tramite`) y luego `docs/pruebas/sql/consultas-optimizacion.sql` para completar la siguiente tabla con los valores observados en su equipo.

---

## 1. Registro Experimental (`EXPLAIN ANALYZE` sobre 1 000 000 de filas)

| Escenario | Consulta / Operación | Plan de Ejecución (`Seq Scan` / `Index Scan`) | Buffers Leídos (`shared hit / read`) | Tiempo de Ejecución (`Execution Time` ms) | Observaciones |
| --- | --- | --- | --- | --- | --- |
| **1. Consulta por DNI sin índice** | `WHERE dni = '40000001' ORDER BY actualizado_en DESC` | | | | |
| **2. Consulta por DNI con índice** (`idx_vista_tramite_dni`) | `WHERE dni = '40000001' ORDER BY actualizado_en DESC` | | | | |
| **3a. Paginación profunda con `OFFSET`** | `ORDER BY actualizado_en DESC, tramite_id DESC LIMIT 20 OFFSET 900000` | | | | |
| **3b. Paginación por clave (*Keyset Pagination*)** | `WHERE (actualizado_en, tramite_id) < (...) ORDER BY actualizado_en DESC, tramite_id DESC LIMIT 20` | | | | |

---

## 2. Conclusiones del Participante

1. **Impacto del índice B-Tree (`idx_vista_tramite_dni`) en lecturas vs. escrituras**:
   - *(Complete aquí su análisis sobre la ganancia en lecturas de la proyección CQRS frente al costo de mantenimiento del índice en cada inserción/actualización).*

2. **Por qué `OFFSET` degrada linealmente $O(N)$ mientras que *Keyset Pagination* se mantiene constante $O(\log N)$**:
   - *(Complete aquí su análisis comparando la cantidad de filas leídas y descartadas en el `EXPLAIN ANALYZE` de ambos enfoques).*

3. **Cuándo aplicar Particionamiento Declarativo (`PARTITION BY RANGE`)**:
   - *(Complete aquí en qué escenarios de retención histórica y *Partition Pruning* resulta ventajoso particionar `vista_tramite`).*

