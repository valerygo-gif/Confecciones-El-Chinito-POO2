package pe.confecciones.chinito.pedidos.controller;

import pe.confecciones.chinito.pedidos.service.RecursoNoEncontradoException;
import pe.confecciones.chinito.pedidos.service.TransicionInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Solo aplica a los controladores de este módulo para no interferir con los de Catálogo.
@RestControllerAdvice(basePackages = "pe.confecciones.chinito.pedidos.controller")
public class PedidosExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ProblemDetail conflicto(TransicionInvalidaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail datosInvalidos(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
