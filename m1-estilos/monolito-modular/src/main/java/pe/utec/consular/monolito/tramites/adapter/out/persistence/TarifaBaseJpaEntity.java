package pe.utec.consular.monolito.tramites.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Entidad JPA para la tabla tramites.tarifa_base.
 */
@Entity
@Table(name = "tarifa_base", schema = "tramites")
public class TarifaBaseJpaEntity {

    @Id
    @Column(name = "tipo_tramite", length = 20)
    private String tipoTramite;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String moneda;

    public TarifaBaseJpaEntity() {
    }

    public TarifaBaseJpaEntity(String tipoTramite, BigDecimal monto, String moneda) {
        this.tipoTramite = tipoTramite;
        this.monto = monto;
        this.moneda = moneda;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
