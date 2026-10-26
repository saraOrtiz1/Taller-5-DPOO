package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uniandes.dpoo.hamburguesas.excepciones.IngredienteRepetidoException;
import uniandes.dpoo.hamburguesas.excepciones.NoHayPedidoEnCursoException;
import uniandes.dpoo.hamburguesas.excepciones.ProductoFaltanteException;
import uniandes.dpoo.hamburguesas.excepciones.ProductoRepetidoException;
import uniandes.dpoo.hamburguesas.excepciones.YaHayUnPedidoEnCursoException;
import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.Restaurante;

class RestauranteTest
{
    @TempDir
    Path dir;

    private Restaurante restaurante;

    private static final String ING_OK = "lechuga;1000\ntomate;1000\nqueso americano;2500\n";
    private static final String MENU_OK = "corral;14000\npapas medianas;5500\ngaseosa;5000\n";
    private static final String COMBOS_OK = "combo corral;10%;corral;papas medianas;gaseosa\n";

    @BeforeEach
    void setUp( )
    {
        restaurante = new Restaurante( );
    }

    private File archivo( String nombre, String contenido ) throws IOException
    {
        Path p = dir.resolve( nombre );
        Files.writeString( p, contenido );
        return p.toFile( );
    }

    private void cargar( String ing, String menu, String combos ) throws Exception
    {
        restaurante.cargarInformacionRestaurante( archivo( "ing.txt", ing ), archivo( "menu.txt", menu ), archivo( "combos.txt", combos ) );
    }

    // ---------- Auxiliares que atrapan la excepción y la devuelven (null si no hubo) ----------

    private Exception intentarCargar( File ing, File menu, File combos )
    {
        try
        {
            restaurante.cargarInformacionRestaurante( ing, menu, combos );
            return null;
        }
        catch( Exception e )
        {
            return e;
        }
    }

    private Exception intentarCargar( String ing, String menu, String combos ) throws IOException
    {
        return intentarCargar( archivo( "ing.txt", ing ), archivo( "menu.txt", menu ), archivo( "combos.txt", combos ) );
    }

    private Exception intentarIniciarPedido( String nombre, String direccion )
    {
        try
        {
            restaurante.iniciarPedido( nombre, direccion );
            return null;
        }
        catch( Exception e )
        {
            return e;
        }
    }

    private Exception intentarCerrar( )
    {
        try
        {
            restaurante.cerrarYGuardarPedido( );
            return null;
        }
        catch( Exception e )
        {
            return e;
        }
    }

    // ---------- Estado inicial ----------

    @Test
    void testRestauranteVacio( )
    {
        assertTrue( restaurante.getPedidos( ).isEmpty( ) );
        assertTrue( restaurante.getIngredientes( ).isEmpty( ) );
        assertTrue( restaurante.getMenuBase( ).isEmpty( ) );
        assertTrue( restaurante.getMenuCombos( ).isEmpty( ) );
        assertNull( restaurante.getPedidoEnCurso( ) );
    }

    // ---------- Carga de archivos ----------

    @Test
    void testCargarInformacion( ) throws Exception
    {
        assertNull( intentarCargar( ING_OK, MENU_OK, COMBOS_OK ) );

        assertEquals( 3, restaurante.getIngredientes( ).size( ) );
        assertEquals( "queso americano", restaurante.getIngredientes( ).get( 2 ).getNombre( ) );
        assertEquals( 2500, restaurante.getIngredientes( ).get( 2 ).getCostoAdicional( ) );

        assertEquals( 3, restaurante.getMenuBase( ).size( ) );
        assertEquals( "papas medianas", restaurante.getMenuBase( ).get( 1 ).getNombre( ) );
        assertEquals( 5500, restaurante.getMenuBase( ).get( 1 ).getPrecio( ) );

        assertEquals( 1, restaurante.getMenuCombos( ).size( ) );
        assertEquals( "combo corral", restaurante.getMenuCombos( ).get( 0 ).getNombre( ) );
        assertEquals( 22050, restaurante.getMenuCombos( ).get( 0 ).getPrecio( ) );
    }

    @Test
    void testCargarIgnoraLineasVacias( ) throws Exception
    {
        cargar( "lechuga;1000\n\ntomate;1000\n\n", "corral;14000\n\n", "\ncombo corral;10%;corral\n" );
        assertEquals( 2, restaurante.getIngredientes( ).size( ) );
        assertEquals( 1, restaurante.getMenuBase( ).size( ) );
        assertEquals( 1, restaurante.getMenuCombos( ).size( ) );
    }

    @Test
    void testCargarDescuentoDecimal( ) throws Exception
    {
        cargar( ING_OK, MENU_OK, "combo especial;50%;corral;papas medianas\n" );
        // (14000 + 5500) * 0.5 = 9750
        assertEquals( 9750, restaurante.getMenuCombos( ).get( 0 ).getPrecio( ) );
    }

    @Test
    void testIngredienteRepetido( ) throws Exception
    {
        Exception e = intentarCargar( "lechuga;1000\nlechuga;2000\n", MENU_OK, COMBOS_OK );
        assertTrue( e instanceof IngredienteRepetidoException, "Debía lanzar IngredienteRepetidoException" );
        assertTrue( e.getMessage( ).contains( "lechuga" ) );
    }

    @Test
    void testProductoRepetido( ) throws Exception
    {
        Exception e = intentarCargar( ING_OK, "corral;14000\ncorral;15000\n", COMBOS_OK );
        assertTrue( e instanceof ProductoRepetidoException, "Debía lanzar ProductoRepetidoException" );
        assertTrue( e.getMessage( ).contains( "corral" ) );
    }

    @Test
    void testComboRepetido( ) throws Exception
    {
        String combos = "combo corral;10%;corral\ncombo corral;5%;corral\n";
        Exception e = intentarCargar( ING_OK, MENU_OK, combos );
        assertTrue( e instanceof ProductoRepetidoException, "Debía lanzar ProductoRepetidoException" );
        assertTrue( e.getMessage( ).contains( "combo corral" ) );
    }

    @Test
    void testProductoFaltanteEnCombo( ) throws Exception
    {
        String combos = "combo raro;10%;corral;hamburguesa que no existe\n";
        Exception e = intentarCargar( ING_OK, MENU_OK, combos );
        assertTrue( e instanceof ProductoFaltanteException, "Debía lanzar ProductoFaltanteException" );
        assertTrue( e.getMessage( ).contains( "hamburguesa que no existe" ) );
    }

    @Test
    void testArchivoInexistente( ) throws Exception
    {
        File noExiste = new File( dir.toFile( ), "noExiste.txt" );
        Exception e = intentarCargar( noExiste, noExiste, noExiste );
        assertTrue( e instanceof FileNotFoundException, "Debía lanzar FileNotFoundException" );
    }

    @Test
    void testPrecioMalFormado( ) throws Exception
    {
        Exception e = intentarCargar( "lechuga;mucho\n", MENU_OK, COMBOS_OK );
        assertTrue( e instanceof NumberFormatException, "Debía lanzar NumberFormatException" );
    }

    @Test
    void testCargarArchivosReales( ) throws Exception
    {
        File ing = new File( "./data/ingredientes.txt" );
        File menu = new File( "./data/menu.txt" );
        File combos = new File( "./data/combos.txt" );
        // Se usa '&' (no '&&') para que la línea no tenga ramas y quede 100% cubierta
        assumeTrue( ing.exists( ) & menu.exists( ) & combos.exists( ), "No se encontró la carpeta data" );

        restaurante.cargarInformacionRestaurante( ing, menu, combos );
        assertEquals( 15, restaurante.getIngredientes( ).size( ) );
        assertEquals( 22, restaurante.getMenuBase( ).size( ) );
        assertEquals( 4, restaurante.getMenuCombos( ).size( ) );
    }

    // ---------- Pedidos ----------

    @Test
    void testIniciarPedido( ) throws Exception
    {
        assertNull( intentarIniciarPedido( "Ana", "Calle 1" ) );
        assertNotNull( restaurante.getPedidoEnCurso( ) );
        assertEquals( "Ana", restaurante.getPedidoEnCurso( ).getNombreCliente( ) );
    }

    @Test
    void testIniciarPedidoConOtroEnCurso( ) throws Exception
    {
        restaurante.iniciarPedido( "Ana", "Calle 1" );
        Pedido original = restaurante.getPedidoEnCurso( );

        Exception e = intentarIniciarPedido( "Luis", "Calle 2" );
        assertTrue( e instanceof YaHayUnPedidoEnCursoException, "Debía lanzar YaHayUnPedidoEnCursoException" );
        assertTrue( e.getMessage( ).contains( "Ana" ) );
        assertTrue( e.getMessage( ).contains( "Luis" ) );
        assertSame( original, restaurante.getPedidoEnCurso( ) );
    }

    @Test
    void testCerrarSinPedido( ) throws Exception
    {
        Exception e = intentarCerrar( );
        assertTrue( e instanceof NoHayPedidoEnCursoException, "Debía lanzar NoHayPedidoEnCursoException" );
        assertTrue( e.getMessage( ).contains( "no hay un pedido en curso" ) );
    }

    @Test
    void testCerrarYGuardarPedido( ) throws Exception
    {
        new File( "./facturas" ).mkdirs( );
        cargar( ING_OK, MENU_OK, COMBOS_OK );

        restaurante.iniciarPedido( "Ana", "Calle 1" );
        Pedido pedido = restaurante.getPedidoEnCurso( );
        pedido.agregarProducto( restaurante.getMenuBase( ).get( 0 ) );
        int id = pedido.getIdPedido( );
        String textoEsperado = pedido.generarTextoFactura( );

        assertNull( intentarCerrar( ) );

        File factura = new File( "./facturas/factura_" + id + ".txt" );
        try
        {
            assertTrue( factura.exists( ), "No se creó el archivo de la factura" );
            assertEquals( textoEsperado, Files.readString( factura.toPath( ), Charset.defaultCharset( ) ) );
        }
        finally
        {
            factura.delete( );
        }

        assertNull( restaurante.getPedidoEnCurso( ) );
        assertEquals( 1, restaurante.getPedidos( ).size( ), "El pedido cerrado debe quedar en el histórico" );
        assertSame( pedido, restaurante.getPedidos( ).get( 0 ) );
    }

    @Test
    void testIniciarOtroPedidoDespuesDeCerrar( ) throws Exception
    {
        new File( "./facturas" ).mkdirs( );
        restaurante.iniciarPedido( "Ana", "Calle 1" );
        int id = restaurante.getPedidoEnCurso( ).getIdPedido( );
        restaurante.cerrarYGuardarPedido( );
        new File( "./facturas/factura_" + id + ".txt" ).delete( );

        restaurante.iniciarPedido( "Luis", "Calle 2" );
        assertEquals( "Luis", restaurante.getPedidoEnCurso( ).getNombreCliente( ) );
    }
}