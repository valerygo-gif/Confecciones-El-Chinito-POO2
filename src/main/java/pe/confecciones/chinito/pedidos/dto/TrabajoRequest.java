package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.EtapaProduccion;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TrabajoRequest(Long idOrdenDetalle, Long idTrabajador, EtapaProduccion etapa,
                             LocalDate fechaTrabajo, Integer cantidad, BigDecimal tarifaPorUnidad,
                             String observaciones) { }
