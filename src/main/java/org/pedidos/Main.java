package org.pedidos;

import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {

        var dao = TestSqlDao.getInstance();
        var table = dao.getMaxUserOrderId(53);
        Set<Integer> setOfKeys = table.keySet();
        for (Integer key : setOfKeys) {
            // Print and display the Rank and Name
            System.out.println("Rank : " + key
                    + "\t\t Name : "
                    + table.get(key));
        }

        //dao.copyUserOrders(1000, 1001);
        var userInfo =  dao.getUserMaxOrder(53);
        System.out.printf("user info: "+userInfo);
        System.out.println("End");

    }
}