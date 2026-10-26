package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uniandes.dpoo.hamburguesas.mundo.Combo;
import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;

class PedidoTest
{
    private Pedido pedido;
    private ProductoMenu corral;
    private ProductoMenu papas;

    @BeforeEach
    void setUp( )
    {
        pedido = new Pedido( "Ana", "Calle 1 # 2-3" );
        corral = new ProductoMenu( "corral", 14000 );
        papas = new ProductoMenu( "papas", 5000 );
    }

    @Test
    void testIdsIncrementales( )
    {
        Pedido otro = new Pedido( "Luis", "Carrera 5" );
        assertEquals( pedido.getIdPedido( ) + 1, otro.getIdPedido( ) );
        Pedido otro2 = new Pedido( "Eva", "Carrera 6" );
        assertEquals( otro.getIdPedido( ) + 1, otro2.getIdPedido( ) );
    }

    @Test
    void testGetNombreCliente( )
    {
        assertEquals( "Ana", pedido.getNombreCliente( ) );
    }

    @Test
    void testPedidoVacio( )
    {
        assertEquals( 0, pedido.getPrecioTotalPedido( ) );

        String esperado = "Cliente: Ana\n" + "Dirección: Calle 1 # 2-3\n" + "----------------\n" + "----------------\n"
                + "Precio Neto:  0\n" + "IVA:          0\n" + "Precio Total: 0\n";
        assertEquals( esperado, pedido.generarTextoFactura( ) );
    }

    @Test
    void testPrecioTotalConProductos( )
    {
        pedido.agregarProducto( corral );
        pedido.agregarProducto( papas );
        // neto 19000, IVA 3610, total 22610
        assertEquals( 22610, pedido.getPrecioTotalPedido( ) );
    }

    @Test
    void testPrecioTotalConIVATruncado( )
    {
        // neto 1001 -> IVA 190.19 -> 190 -> total 1191
        pedido.agregarProducto( new ProductoMenu( "raro", 1001 ) );
        assertEquals( 1191, pedido.getPrecioTotalPedido( ) );
    }

    @Test
    void testPrecioConCombo( )
    {
        ArrayList<ProductoMenu> items = new ArrayList<ProductoMenu>( );
        items.add( corral );
        items.add( papas );
        Combo combo = new Combo( "combo", 0.5, items ); // 9500
        pedido.agregarProducto( combo );
        // neto 9500, IVA 1805, total 11305
        assertEquals( 11305, pedido.getPrecioTotalPedido( ) );
    }

    @Test
    void testGenerarTextoFactura( )
    {
        pedido.agregarProducto( corral );
        pedido.agregarProducto( papas );
        String texto = pedido.generarTextoFactura( );

        assertTrue( texto.contains( "Cliente: Ana" ) );
        assertTrue( texto.contains( "Dirección: Calle 1 # 2-3" ) );
        assertTrue( texto.contains( corral.generarTextoFactura( ) ) );
        assertTrue( texto.contains( papas.generarTextoFactura( ) ) );
        assertTrue( texto.contains( "Precio Neto:  19000" ) );
        assertTrue( texto.contains( "IVA:          3610" ) );
        assertTrue( texto.contains( "Precio Total: 22610" ) );
    }

    @Test
    void testGuardarFactura( @TempDir Path dir ) throws IOException
    {
        pedido.agregarProducto( corral );
        pedido.agregarProducto( papas );
        File archivo = dir.resolve( "factura.txt" ).toFile( );

        pedido.guardarFactura( archivo );

        assertTrue( archivo.exists( ) );
        // PrintWriter usa el charset por defecto, así que se lee con el mismo
        String contenido = Files.readString( archivo.toPath( ), Charset.defaultCharset( ) );
        assertEquals( pedido.generarTextoFactura( ), contenido );
    }

    @Test
    void testGuardarFacturaEnCarpetaInexistente( @TempDir Path dir )
    {
        File archivo = new File( dir.toFile( ), "noExiste/factura.txt" );
        
        assertThrows( IOException.class, ( ) -> {
            pedido.guardarFactura( archivo );
        }, "Debería lanzar IOException si la carpeta no existe" );
    }
}