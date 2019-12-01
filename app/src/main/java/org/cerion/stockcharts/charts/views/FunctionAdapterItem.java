package org.cerion.stockcharts.charts.views;


import android.support.annotation.NonNull;

import org.cerion.stocks.core.functions.IFunction;
import org.cerion.stocks.core.functions.types.IFunctionEnum;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for getting IFunction classes from enums with proper display name and sorting
 */
class FunctionAdapterItem implements Comparable<FunctionAdapterItem> {

    IFunction function;

    private FunctionAdapterItem(IFunction function) {
        this.function = function;
    }

    static List<FunctionAdapterItem> getList(IFunctionEnum[] values) {
        List<FunctionAdapterItem> result = new ArrayList<>();

        for(IFunctionEnum e : values)
            result.add(new FunctionAdapterItem(e.getInstance()));

        Collections.sort(result);
        return result;
    }

    static int indexOf(List<FunctionAdapterItem> list, IFunctionEnum function) {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).function.getId() == function)
                return i;
        }

        return 0;
    }

    /*
    static List<FunctionAdapterItem> getList(List<IFunction> functions) {
        List<FunctionAdapterItem> result = new ArrayList<>();
        for(IFunction o : functions)
            result.add(new FunctionAdapterItem(o));

        Collections.sort(result);
        return result;
    }
    */

    @Override
    public String toString() {
        return function.getName();
    }

    @Override
    public int compareTo(@NonNull FunctionAdapterItem o) {
        return this.toString().compareTo(o.toString());
    }

}
