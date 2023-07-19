package org.pedidos;

import java.sql.*;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestSqlDao {

    //Mejora uso de logger
    private final Logger logger = Logger.getLogger(TestSqlDao.class.getName());
    private TestSqlDao() {
        logger.addHandler(new ConsoleHandler());
    }

    //Mejora: Se utiliza esta clase para trabajar con un patron sigleton
    //Que no requiera sincronizacion, en un ambiente multihilos(Bill Pugh's Singleton design)
    private static class TestSqlDaoHolder {
        private static TestSqlDao instance = new TestSqlDao();
    }

    //-----------------------------------------------------------
    //Mejora: Cambio a metodo publico para completar el patron singleton
    public  static TestSqlDao getInstance() {

        return TestSqlDaoHolder.instance;
    }

    /**
     * Obtiene el ID del último pedido para cada usuario
     * Mejora: los valores tipo Id se reciben como long pero en el result set se trabajan como int, deberian trabajarse
     * en el formato de mas capacidad que es long (sin emabargo se asume que no se tiene la opcion de modificar la BD
     * por este motivo se decide cambiar el tipo a int
     * Mejora: Se hace un simple manejo de excepciones, se podria mejorar con la creacion de una excepcion personalizada
     * en vez del uso de la generica Exception.
     * Mejora: El uso de hashtable la ser una clase thread-safe podria ocacionar problemas de desempeño en esecenarios
     * de alta concucurrencia en especial debido a que es una query (no genera efectos secundarios).
     */
    public Hashtable<Integer, Integer> getMaxUserOrderId(int idTienda) throws Exception {

        //Mejora: Reduce el scope de esta variable a solo el metodo donde se usa, lo que es una buena practica
        var maxOrderUser = new Hashtable<Integer, Integer>();

        //Mejora con el uso de sentencia MAX se obtiene el ultimo pedido realizado por el cliente
        //y con la sentencia "group by" se agrupa  por usuario, evitando asi el procesamiento en memoria
        //del programa
        var query = "SELECT ID_USUARIO, MAX(ID_PEDIDO) AS ID_PEDIDO FROM PEDIDOS WHERE ID_TIENDA = ? GROUP BY ID_USUARIO";

        // Mejora uso de try with resources, que tiene la opcion de ser auto-close para la conexion
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

           //Mejora Con este uso del prepare statement sin el uso de la concatenacion directa se
           //previenen ataques de SQL injection

            stmt.setInt(1,  idTienda);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int idPedido = rs.getInt("ID_PEDIDO");
                int idUsuario = rs.getInt("ID_USUARIO");
                //Mejora: Gracias a el ajuste en la consulta SQL se evita el uso de la sentencia if
                maxOrderUser.put(idUsuario, idPedido);
            }

        } catch (SQLException ex) {
            //Mejora: Traza de error
            logger.log( Level.SEVERE, ex.toString(), ex );
            throw new Exception(ex.getCause());
        }

        return maxOrderUser;
    }



    /**
     * Copia todos los pedidos de un usuario a otro
     * Mejora: Verificar si se realizo la copia | roll back
     */
    public void copyUserOrders(long idUserOri, long idUserDes) throws Exception {
        //*Mejora: Faltan atributos  de la tabla pedidos basados en los campos que se utilizaron en la otras consultas
        //ID_TIENDA, ID_PEDIDO, asumiendo que el ID de pedido no es unico se encuentra formando una llave compuesta con el id del usuario
        //(de lo contrario estaria bien omitirlo)
        //*Mejora: Se usa la estrategia de pre-compiled statement para evitar ataques de SQL injection
        String readQuery = "SELECT FECHA, TOTAL, SUBTOTAL, DIRECCION, ID_TIENDA, SUBTOTAL, ID_PEDIDO FROM PEDIDOS WHERE ID_USUARIO = ?";
        //*Mejora: Uso de Text blocks para mejorar la legibilidad
        String insertQuery = """ 
                  INSERT INTO PEDIDOS (ID_PEDIDO, ID_TIENDA, ID_USUARIO, FECHA, SUBTOTAL, TOTAL, DIRECCION) 
                  VALUES (?, ?, ?, ?, ?, ?, ?) 
                """;

        //Uso de try-with resurces con el fin de manejar el beneficio de auto-close en la conexion
        try (Connection connection = getConnection();
             PreparedStatement readStatement = connection.prepareStatement(readQuery)) {

            readStatement.setLong(1, idUserOri);
            ResultSet rs = readStatement.executeQuery();

            Connection connection2 = getConnection();

            try (PreparedStatement stmt2 = connection2.prepareStatement(insertQuery)) {


                while (rs.next()) {

                    long idPedido = rs.getLong("ID_PEDIDO");
                    long idTienda = rs.getLong("ID_TIENDA");
                    double subtotal = rs.getDouble("SUBTOTAL");
                    double total = rs.getDouble("TOTAL");
                    Timestamp fecha = rs.getTimestamp("FECHA");
                    String direccion = rs.getString("DIRECCION");

                    stmt2.setLong(1, idPedido);
                    stmt2.setLong(2, idTienda);
                    stmt2.setLong(3, idUserDes);
                    stmt2.setTimestamp(4, fecha);
                    stmt2.setDouble(5, subtotal);
                    stmt2.setDouble(6, total);
                    stmt2.setString(7, direccion);

                    //Desactiva el auto commit para trabajar en modo trasaccional
                    connection2.setAutoCommit(false);
                    stmt2.executeUpdate();
                    connection2.commit();


                }

            } catch (SQLException ex) {
                //Mejora: Traza de error
                logger.log( Level.SEVERE, ex.toString(), ex );
                //Mejora: Si ocurre un error retorna la transaccion
                connection2.rollback();

            } finally {

                //Activa la funcion de autoCommit y cierra la conexion
                connection2.setAutoCommit(true);
                connection2.close();

            }

        }


    }

    /**
     * Obtiene los datos del usuario y pedido con el pedido de mayor importe(Mayor valor = P.TOTAL) para la tienda dada
     * <p>
     * Mejora: Entrega un DTO con los datos del usuario y pedido con el pedido de mayor importe, no es una
     * buena practica utilizar un metodo void como consulta  y pasar los objetos por referencia es
     * aun peor debido a que abre la opcion  mutacion del estado en diferentes puntos, lo mejor es entregar un nuevo objeto con
     * estado inmutable
     */
    public PedidoUsuario getUserMaxOrder(int idTienda) throws Exception {


        // Mejora: Se utiliza un join para traer los datos asociados de usuario y pedido de mayor importe y asociarlos con la
        // tabla pedidos a su vez en caso de que existan dos pedidos con el mismo valor se selecciona
        // el que tiene el identificador de pedido mas alto, se utilizo la estrategia de JOIN pora optimizar la consulta
        // en vez del uso de una subconsulta
        // Mejora: Si el maximo total es cero se retorna un result set vacio evitando asi la comparacion con cero en el
        // programa

        // Uso de Text blocks para mejorar la legibilidad (habilitados por defecto en java 17)
        String query = """
                          SELECT
                          U.ID_USUARIO,
                          P.ID_PEDIDO,
                          P.TOTAL,
                          U.NOMBRE,
                          U.DIRECCION
                          FROM PEDIDOS AS P
                          INNER JOIN USUARIOS AS U ON P.ID_USUARIO = U.ID_USUARIO
                          INNER JOIN  (
                             SELECT
                              MAX(P.TOTAL) AS TOTAL  
                            FROM PEDIDOS AS P
                            WHERE P.ID_TIENDA = ?
                          ) AS A
                          ON P.TOTAL = A.TOTAL
                          WHERE  P.ID_TIENDA = ?
                          AND A.TOTAL > 0
                          ORDER BY P.ID_PEDIDO DESC
                          LIMIT 1;
                """;
        ;
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, idTienda);
            stmt.setInt(2, idTienda);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return new PedidoUsuario(
                        rs.getLong("TOTAL"),
                        rs.getInt("ID_USUARIO"),
                        rs.getInt("ID_PEDIDO"),
                        rs.getString("NOMBRE"),
                        rs.getString("DIRECCION")
                );

            }
            return new PedidoUsuario();

        }catch(SQLException ex){
            //Mejora: Traza de error
            logger.log( Level.SEVERE, ex.toString(), ex );
            throw new Exception(ex.getCause());
        }


    }


    /**
     * Crea la conexion, en la version base no estaba creada
     *
     * @return Sql Conexion
     * @throws ClassNotFoundException
     */
    private Connection getConnection() throws ClassNotFoundException {

        // return JDBC connection
        Class.forName("com.mysql.cj.jdbc.Driver");
        String connectionUrl = "jdbc:mysql://localhost:3306/test_db";

        try {
            var conn = DriverManager.getConnection(connectionUrl, "root", "root");
            return conn;
        } catch (SQLException ex) {
            //Mejora: Traza de error
            logger.log( Level.SEVERE, ex.toString(), ex );
            throw new RuntimeException(ex.getCause());
        }
    }

    class PedidoUsuario {

        long total;
        long userId;
        long orderId;
        String name;
        String address;

        public PedidoUsuario() {
        }

        public PedidoUsuario(long total, long userId, long orderId, String name, String address) {
            this.total = total;
            this.userId = userId;
            this.orderId = orderId;
            this.name = name;
            this.address = address;
        }

        public long getUserId() {
            return userId;
        }

        public long getOrderId() {
            return orderId;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return "PedidoUsuario{" +
                    "total=" + total +
                    ", userId=" + userId +
                    ", orderId=" + orderId +
                    ", name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    }


}
