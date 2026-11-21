package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Service
public class ImportacionInventarioService {
    private static final List<String> CAMPOS=List.of("codigo","nombre","tipo_producto","registro_invima","descripcion","presentacion","fecha_compra","fecha_vence","precio_compra","precio_venta","stock_minimo");
    private final MedicamentoRepository repo;
    public ImportacionInventarioService(MedicamentoRepository repo){this.repo=repo;}
    public record Fila(int numero, Map<String,String> datos, List<String> errores) {}
    public record VistaPrevia(int total, int validas, int invalidas, List<Fila> filas) {}

    public VistaPrevia validar(byte[] bytes) {
        if(bytes==null || bytes.length==0 || bytes.length>10_000_000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"CSV vacio o demasiado grande");
        try(Reader reader=new InputStreamReader(new ByteArrayInputStream(bytes),StandardCharsets.UTF_8);
            CSVParser parser=CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreSurroundingSpaces(true).get().parse(reader)) {
            if(!parser.getHeaderMap().keySet().containsAll(CAMPOS)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Encabezados requeridos: "+CAMPOS);
            Set<String> codigos=new HashSet<>(), nombres=new HashSet<>();
            List<Fila> filas=new ArrayList<>();
            for(CSVRecord r:parser){
                if(filas.size()>=10000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Maximo 10000 registros");
                Map<String,String> datos=new LinkedHashMap<>();
                for(String c:CAMPOS)datos.put(c,r.get(c).trim());
                List<String> errores=new ArrayList<>();
                String codigo=datos.get("codigo"), nombre=datos.get("nombre"), tipo=datos.get("tipo_producto");
                if(!codigo.matches("[A-Za-z0-9]{8}")) errores.add("Codigo debe tener 8 caracteres alfanumericos");
                if(nombre.isBlank() || nombre.length()>100) errores.add("Nombre invalido");
                if(tipo.isBlank() || tipo.length()>20) errores.add("Tipo invalido");
                if("MEDICAMENTO".equalsIgnoreCase(tipo) && datos.get("registro_invima").isBlank()) errores.add("INVIMA obligatorio");
                if(!codigos.add(codigo.toLowerCase(Locale.ROOT))) errores.add("Codigo repetido en archivo");
                if(!nombres.add(nombre.toLowerCase(Locale.ROOT))) errores.add("Nombre repetido en archivo");
                if(repo.existsByCodigo(codigo)) errores.add("Codigo ya existe en BD");
                if(repo.existsByNombre(nombre)) errores.add("Nombre ya existe en BD");
                try {
                    LocalDate compra=LocalDate.parse(datos.get("fecha_compra")), vence=LocalDate.parse(datos.get("fecha_vence"));
                    if(vence.isBefore(compra)) errores.add("Vencimiento anterior a compra");
                } catch(Exception e){errores.add("Fechas invalidas (AAAA-MM-DD)");}
                for(String precio:List.of("precio_compra","precio_venta")) {
                    try {if(new BigDecimal(datos.get(precio)).signum()<0) errores.add(precio+" negativo");}
                    catch(Exception e){errores.add(precio+" invalido");}
                }
                try {if(Integer.parseInt(datos.get("stock_minimo"))<0) errores.add("Stock minimo negativo");}
                catch(Exception e){errores.add("Stock minimo invalido");}
                filas.add(new Fila((int)r.getRecordNumber()+1,datos,errores));
            }
            int validas=(int)filas.stream().filter(f->f.errores().isEmpty()).count();
            return new VistaPrevia(filas.size(),validas,filas.size()-validas,filas);
        }catch(ResponseStatusException e){throw e;}
         catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No se pudo interpretar CSV: "+e.getMessage());}
    }

    @Transactional
    public VistaPrevia confirmar(byte[] bytes){
        VistaPrevia previa=validar(bytes);
        if(previa.total()==0 || previa.invalidas()>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Importacion cancelada: hay filas invalidas o archivo vacio");
        for(Fila f:previa.filas()){
            Map<String,String> d=f.datos();
            Medicamento m=new Medicamento();
            m.setCodigo(d.get("codigo"));m.setNombre(d.get("nombre"));m.setTipoProducto(d.get("tipo_producto"));
            m.setRegistroInvima(d.get("registro_invima").isBlank()?null:d.get("registro_invima"));
            m.setRequiereInvima("MEDICAMENTO".equalsIgnoreCase(d.get("tipo_producto")));
            m.setDescripcion(d.get("descripcion"));m.setPresentacion(d.get("presentacion"));
            m.setFechaCompra(LocalDate.parse(d.get("fecha_compra")));m.setFechaVence(LocalDate.parse(d.get("fecha_vence")));
            m.setPrecioCompra(new BigDecimal(d.get("precio_compra")));m.setPrecioVenta(new BigDecimal(d.get("precio_venta")));
            m.setStockMinimo(Integer.valueOf(d.get("stock_minimo")));m.setStockActual(0);
            repo.save(m);
        }
        repo.flush();
        return previa;
    }
}
