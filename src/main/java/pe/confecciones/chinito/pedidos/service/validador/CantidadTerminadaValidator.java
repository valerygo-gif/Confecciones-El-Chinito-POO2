package pe.confecciones.chinito.pedidos.service.validador;

import org.springframework.stereotype.Component;

// Regla ck_opd_cantidades: lo terminado nunca supera lo planificado.
@Component
public class CantidadTerminadaValidator {
    public void validar(int cantidadTerminada, int cantidadAProducir) {
        if (cantidadTerminada > cantidadAProducir) {
            throw new IllegalArgumentException("La cantidad terminada (" + cantidadTerminada
                    + ") supera la cantidad a producir (" + cantidadAProducir + ").");
        }
    }
}
