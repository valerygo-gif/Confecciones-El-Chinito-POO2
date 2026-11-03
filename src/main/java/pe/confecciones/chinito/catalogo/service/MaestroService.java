package pe.confecciones.chinito.catalogo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.catalogo.modelo.*;
import pe.confecciones.chinito.catalogo.repository.*;
import java.util.List;

@Service
public class MaestroService {
    private final CategoriaProductoRepository categorias;
    private final TallaRepository tallas;
    private final DanzaRepository danzas;
    private final RegionRepository regiones;

    public MaestroService(CategoriaProductoRepository categorias, TallaRepository tallas,
                          DanzaRepository danzas, RegionRepository regiones) {
        this.categorias = categorias;
        this.tallas = tallas;
        this.danzas = danzas;
        this.regiones = regiones;
    }

    @Transactional(readOnly = true)
    public List<CategoriaProducto> categorias() {
        return categorias.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Talla> tallas() {
        return tallas.findAllByOrderByOrdenVisualAsc();
    }

    @Transactional(readOnly = true)
    public List<Danza> danzas() {
        return danzas.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Region> regiones() {
        return regiones.findAllByOrderByNombreAsc();
    }

    @Transactional
    public void crearCategoria(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("La categoría necesita un nombre.");
        CategoriaProducto categoria = new CategoriaProducto();
        categoria.setNombre(nombre.trim());
        categorias.save(categoria);
    }

    @Transactional
    public void crearTalla(String etiqueta, int orden) {
        if (etiqueta == null || etiqueta.isBlank())
            throw new IllegalArgumentException("La talla necesita una etiqueta.");
        Talla talla = new Talla();
        talla.setEtiqueta(etiqueta.trim());
        talla.setOrdenVisual(orden);
        tallas.save(talla);
    }

    @Transactional
    public void crearRegion(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Escribe el nombre de la región.");
        nombre = nombre.trim();
        if (nombre.length() > 60)
            throw new IllegalArgumentException("La región puede tener hasta 60 caracteres.");
        if (regiones.existsByNombreIgnoreCase(nombre))
            throw new IllegalArgumentException("Esa región ya está registrada.");

        Region region = new Region();
        region.setNombre(nombre);
        regiones.save(region);
    }

    @Transactional
    public void crearDanza(String nombre, Long regionId) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Escribe el nombre de la danza.");
        nombre = nombre.trim();
        if (nombre.length() > 120)
            throw new IllegalArgumentException("La danza puede tener hasta 120 caracteres.");
        if (regionId == null)
            throw new IllegalArgumentException("Selecciona una región para la danza.");

        Region region = regiones.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("La región seleccionada no existe."));

        // Una danza solo se registra una vez dentro de la misma región.
        if (danzas.existsByNombreIgnoreCaseAndRegion_Id(nombre, regionId))
            throw new IllegalArgumentException("Esta danza ya existe en esa región.");

        Danza danza = new Danza();
        danza.setNombre(nombre);
        danza.setRegion(region);
        danzas.save(danza);
    }
}
