package pe.confecciones.chinito.pedidos.service.validador;

import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import org.springframework.stereotype.Component;

// Regla ck_pd_conjunto: CONJUNTO exige id_conjunto; los demás tipos deben dejarlo nulo.
@Component
public class PedidoDetalleConjuntoValidator {
    public void validar(TipoItemPedido tipoItem, Long idConjunto) {
        if (tipoItem == null) {
            throw new IllegalArgumentException("El tipo de ítem es obligatorio.");
        }
        boolean esConjunto = tipoItem == TipoItemPedido.CONJUNTO;
        if (esConjunto && idConjunto == null) {
            throw new IllegalArgumentException("Un ítem de tipo CONJUNTO requiere idConjunto.");
        }
        if (!esConjunto && idConjunto != null) {
            throw new IllegalArgumentException("Solo un ítem de tipo CONJUNTO puede indicar idConjunto.");
        }
    }
}
