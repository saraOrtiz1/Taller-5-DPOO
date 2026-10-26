package uniandes.dpoo.hamburguesas.tests;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import java.lang.reflect.Field;
import java.util.ArrayList;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
import uniandes.dpoo.hamburguesas.mundo.Ingrediente;
import uniandes.dpoo.hamburguesas.mundo.ProductoAjustado;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;
 
public class ProductoAjustadoTest
{
    private ProductoMenu base;
    private ProductoAjustado ajustado;
    private Ingrediente queso;
    private Ingrediente tocineta;
    private Ingrediente tomate;
 
    @BeforeEach
    void setUp( )
    {
        base = new ProductoMenu( "corral", 14000 );
        ajustado = new ProductoAjustado( base );
        queso = new Ingrediente( "queso americano", 2500 );
        tocineta = new Ingrediente( "tocineta express", 2500 );
        tomate = new Ingrediente( "tomate", 1000 );
    }
 
    /**
     * ProductoAjustado no tiene métodos para agregar/quitar ingredientes, así que se
     * llenan las listas privadas usando reflexión.
     */
    @SuppressWarnings( "unchecked" )
    private void meter( String campo, Ingrediente ing ) throws Exception
    {
        Field f = ProductoAjustado.class.getDeclaredField( campo );
        f.setAccessible( true );
        ( (ArrayList<Ingrediente>) f.get( ajustado ) ).add( ing );
    }
 
    @Test
    void testGetNombre( )
    {
        assertEquals( "corral", ajustado.getNombre( ) );
    }
 
    @Test
    void testPrecioSinAjustes( )
    {
        assertEquals( 14000, ajustado.getPrecio( ) );
    }
 
    @Test
    void testPrecioConAgregados( ) throws Exception
    {
        meter( "agregados", queso );
        meter( "agregados", tocineta );
        assertEquals( 14000 + 2500 + 2500, ajustado.getPrecio( ) );
    }
 
    @Test
    void testPrecioIngredienteAgregadoRepetido( ) throws Exception
    {
        meter( "agregados", queso );
        meter( "agregados", queso );
        assertEquals( 14000 + 2500 * 2, ajustado.getPrecio( ) );
    }
 
    @Test
    void testPrecioEliminadosNoReducen( ) throws Exception
    {
        meter( "eliminados", tomate );
        assertEquals( 14000, ajustado.getPrecio( ) );
    }
 
    @Test
    void testPrecioAgregadosYEliminados( ) throws Exception
    {
        meter( "agregados", queso );
        meter( "eliminados", tomate );
        assertEquals( 16500, ajustado.getPrecio( ) );
    }
 
    @Test
    void testFacturaSinAjustes( )
    {
        String texto = ajustado.generarTextoFactura( );
        assertTrue( texto.contains( "corral" ) );
        assertTrue( texto.contains( "14000" ) );
        assertFalse( texto.contains( "@" ), "No debe imprimirse la referencia del objeto" );
        assertFalse( texto.contains( "+" ) );
        assertFalse( texto.contains( "-" ) );
    }
 
    @Test
    void testFacturaConAjustes( ) throws Exception
    {
        meter( "agregados", queso );
        meter( "eliminados", tomate );
        String texto = ajustado.generarTextoFactura( );
 
        assertTrue( texto.contains( "corral" ) );
        assertTrue( texto.contains( "+queso americano" ) );
        assertTrue( texto.contains( "2500" ) );
        assertTrue( texto.contains( "-tomate" ) );
        assertTrue( texto.contains( "16500" ) );
        assertFalse( texto.contains( "@" ) );
 
        // Cada ingrediente debe quedar en su propia línea
        assertFalse( texto.contains( "2500    -tomate" ), "Faltan saltos de línea entre ingredientes" );
    }
}
 
