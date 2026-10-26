package uniandes.dpoo.hamburguesas.tests;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;
 
public class ProductoMenuTest
{
    private ProductoMenu producto;
 
    @BeforeEach
    void setUp( )
    {
        producto = new ProductoMenu( "corral", 14000 );
    }
 
    @Test
    void testGetNombre( )
    {
        assertEquals( "corral", producto.getNombre( ) );
    }
 
    @Test
    void testGetPrecio( )
    {
        assertEquals( 14000, producto.getPrecio( ) );
    }
 
    @Test
    void testGenerarTextoFactura( )
    {
        String esperado = "corral\n" + "            14000\n";
        assertEquals( esperado, producto.generarTextoFactura( ) );
    }
}
 
