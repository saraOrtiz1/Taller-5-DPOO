package uniandes.dpoo.hamburguesas.tests;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
 
public class RestauranteTest
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
        cargar( ING_OK, MENU_OK, COMBOS_OK );
 
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
        assertEquals( 9750, restaurante.getMenuCombos( ).get( 0 ).getPrecio( ) );
    }
 
    @Test
    void testIngredienteRepetido( )
    {
        IngredienteRepetidoException e = assertThrows( IngredienteRepetidoException.class,
                ( ) -> cargar( "lechuga;1000\nlechuga;2000\n", MENU_OK, COMBOS_OK ) );
        assertTrue( e.getMessage( ).contains( "lechuga" ) );
    }
 
    @Test
    void testProductoRepetido( )
    {
        ProductoRepetidoException e = assertThrows( ProductoRepetidoException.class,
                ( ) -> cargar( ING_OK, "corral;14000\ncorral;15000\n", COMBOS_OK ) );
        assertTrue( e.getMessage( ).contains( "corral" ) );
    }
 
    @Test
    void testComboRepetido( )
    {
        String combos = "combo corral;10%;corral\ncombo corral;5%;corral\n";
        ProductoRepetidoException e = assertThrows( ProductoRepetidoException.class,
                ( ) -> cargar( ING_OK, MENU_OK, combos ) );
        assertTrue( e.getMessage( ).contains( "combo corral" ) );
    }
 
    @Test
    void testProductoFaltanteEnCombo( )
    {
        String combos = "combo raro;10%;corral;hamburguesa que no existe\n";
        ProductoFaltanteException e = assertThrows( ProductoFaltanteException.class,
                ( ) -> cargar( ING_OK, MENU_OK, combos ) );
        assertTrue( e.getMessage( ).contains( "hamburguesa que no existe" ) );
    }
 
    @Test
    void testArchivoInexistente( )
    {
        File noExiste = new File( dir.toFile( ), "noExiste.txt" );
        assertThrows( FileNotFoundException.class,
                ( ) -> restaurante.cargarInformacionRestaurante( noExiste, noExiste, noExiste ) );
    }
 
    @Test
    void testPrecioMalFormado( )
    {
        assertThrows( NumberFormatException.class,
                ( ) -> cargar( "lechuga;mucho\n", MENU_OK, COMBOS_OK ) );
    }
 
    @Test
    void testCargarArchivosReales( ) throws Exception
    {
        File ing = new File( "./data/ingredientes.txt" );
        File menu = new File( "./data/menu.txt" );
        File combos = new File( "./data/combos.txt" );
        assumeTrue( ing.exists( ) && menu.exists( ) && combos.exists( ), "No se encontró la carpeta data" );
 
        restaurante.cargarInformacionRestaurante( ing, menu, combos );
        assertEquals( 15, restaurante.getIngredientes( ).size( ) );
        assertEquals( 22, restaurante.getMenuBase( ).size( ) );
        assertEquals( 4, restaurante.getMenuCombos( ).size( ) );
    }
 
    // ---------- Gestión de Pedidos ----------
 
    @Test
    void testIniciarPedido( ) throws Exception
    {
        restaurante.iniciarPedido( "Ana", "Calle 1" );
        assertNotNull( restaurante.getPedidoEnCurso( ) );
        assertEquals( "Ana", restaurante.getPedidoEnCurso( ).getNombreCliente( ) );
    }
 
    @Test
    void testIniciarPedidoConOtroEnCurso( ) throws Exception
    {
        restaurante.iniciarPedido( "Ana", "Calle 1" );
        Pedido original = restaurante.getPedidoEnCurso( );
 
        YaHayUnPedidoEnCursoException e = assertThrows( YaHayUnPedidoEnCursoException.class,
                ( ) -> restaurante.iniciarPedido( "Luis", "Calle 2" ) );
        assertTrue( e.getMessage( ).contains( "Ana" ) );
        assertTrue( e.getMessage( ).contains( "Luis" ) );
        assertSame( original, restaurante.getPedidoEnCurso( ) );
    }
 
    @Test
    void testCerrarSinPedido( )
    {
        NoHayPedidoEnCursoException e = assertThrows( NoHayPedidoEnCursoException.class,
                ( ) -> restaurante.cerrarYGuardarPedido( ) );
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
 
        restaurante.cerrarYGuardarPedido( );
 
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
        
        File factura = new File( "./facturas/factura_" + id + ".txt" );
        if( factura.exists( ) )
        {
            factura.delete( );
        }
 
        restaurante.iniciarPedido( "Luis", "Calle 2" );
        assertEquals( "Luis", restaurante.getPedidoEnCurso( ).getNombreCliente( ) );
    }
}