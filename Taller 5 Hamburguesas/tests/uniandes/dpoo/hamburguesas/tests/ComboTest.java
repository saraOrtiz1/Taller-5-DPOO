package uniandes.dpoo.hamburguesas.tests;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import java.util.ArrayList;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
import uniandes.dpoo.hamburguesas.mundo.Combo;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;
 
public class ComboTest
{
    private ArrayList<ProductoMenu> items;
    private Combo combo;
 
    @BeforeEach
    void setUp( )
    {
        items = new ArrayList<ProductoMenu>( );
        items.add( new ProductoMenu( "corral", 14000 ) );
        items.add( new ProductoMenu( "papas medianas", 5500 ) );
        items.add( new ProductoMenu( "gaseosa", 5000 ) );
        // Suma de los productos: 24500. Con 10% de descuento: 22050
        combo = new Combo( "combo corral", 0.1, items );
    }
 
    @Test
    void testGetNombre( )
    {
        assertEquals( "combo corral", combo.getNombre( ) );
    }
 
    @Test
    void testPrecioConDescuento( )
    {
        assertEquals( 22050, combo.getPrecio( ) );
    }
 
    @Test
    void testPrecioSinDescuento( )
    {
        Combo sinDescuento = new Combo( "combo", 0.0, items );
        assertEquals( 24500, sinDescuento.getPrecio( ) );
    }
 
    @Test
    void testPrecioMitad( )
    {
        Combo mitad = new Combo( "combo", 0.5, items );
        assertEquals( 12250, mitad.getPrecio( ) );
    }
 
    @Test
    void testPrecioComboVacio( )
    {
        Combo vacio = new Combo( "vacio", 0.1, new ArrayList<ProductoMenu>( ) );
        assertEquals( 0, vacio.getPrecio( ) );
    }
 
    @Test
    void testComboNoDependeDeLaListaOriginal( )
    {
        items.add( new ProductoMenu( "extra", 10000 ) );
        assertEquals( 22050, combo.getPrecio( ) );
    }
 
    @Test
    void testGenerarTextoFactura( )
    {
        String texto = combo.generarTextoFactura( );
        assertTrue( texto.contains( "Combo combo corral" ) );
        assertTrue( texto.contains( "Descuento: 0.1" ) );
        assertTrue( texto.contains( "22050" ) );
    }
}
