package org.cerion.stockcharts;


import org.cerion.stocks.core.model.Position;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class Temp {

    public static List<Position> getPositions() {

        List<Position> list = new ArrayList<>();
        list.add(new Position("XLE", 73, 67.92, new GregorianCalendar(2015, 7, 3).getTime(), false));
        list.add(new Position("XLE", 65, 76.30, new GregorianCalendar(2015, 0, 22).getTime(), false));
        list.add(new Position("OHI", 125, 40.5999, new GregorianCalendar(2015, 1, 17).getTime(), false));
        return list;

        //StockDB db = StockDB.getInstance(context);
        //for(Position p : list)
        //    db.addPosition(p);
    }
}
