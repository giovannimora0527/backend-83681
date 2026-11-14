package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "despacho", uniqueConstraints = {
        @UniqueConstraint(name = "uk_numero_despacho", columnNames = "numero_despacho"),
        @UniqueConstraint(name = "uk_numero_comprobante", columnNames = "numero_comprobante")
})
@Data
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_despacho", nullable = false, length = 20)
    private String numeroDespacho;

    @Column(name = "numero_comprobante", length = 30)
    private String numeroComprobante;

    @Column(name = "url_comprobante_pdf", length = 500)
    private String urlComprobantePdf;

    @Column(name = "fecha_generacion_comprobante")
    private LocalDateTime fechaGeneracionComprobante;

    @Column(name = "formula_id", nullable = false)
    private Long formulaId;

    @Column(name = "cita_id")
    private Long citaId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "mascota_id")
    private Long mascotaId;

    @ManyToOne
    @JoinColumn(name = "usuario_despacho", nullable = false)
    private Usuario usuarioDespacho;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @OneToMany(mappedBy = "despacho")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DespachoDetalle> detalles;

    @OneToMany(mappedBy = "despacho")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DespachoHistorialEstado> historialEstados;
}
