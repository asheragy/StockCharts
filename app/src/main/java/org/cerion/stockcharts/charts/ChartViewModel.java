package org.cerion.stockcharts.charts;

import android.databinding.Observable;
import android.databinding.ObservableField;

import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.charts.VolumeChart;
import org.cerion.stocklist.functions.IFunction;
import org.cerion.stocklist.functions.IFunctionEnum;
import org.cerion.stocklist.functions.IIndicator;
import org.cerion.stocklist.functions.Indicator;
import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartViewModel {

    private StockChart mChart;
    private ChartsViewModel parent;
    private ChartType mType;
    private IChartView view;

    // Fixed values after loading
    public boolean showLineCheckbox;
    public boolean showLogScale;
    public boolean showFunctions;
    public List<String> functions = new ArrayList<>();

    private Map<String,IFunction> functionMap = new HashMap<>();
    private OnFunctionChangeListener mFunctionListener;

    public ObservableField<Boolean> showAddOverlay = new ObservableField<>(true);
    public ObservableField<Integer> functionIndex = new ObservableField<>();
    public ObservableField<Boolean> logScale = new ObservableField<>();
    public ObservableField<Boolean> lineChart = new ObservableField<>();

    public interface OnFunctionChangeListener {
        void onFunctionChanged();
    }

    private enum ChartType {
        Price,
        Volume,
        Indicator
    }

    public ChartViewModel(ChartsViewModel parent, StockChart chart) {
        this.parent = parent;
        mChart = chart;

        if (chart instanceof VolumeChart) {
            VolumeChart c = (VolumeChart)chart;
            mType = ChartType.Volume;

            logScale.set(c.logScale);
        }
        else if (chart instanceof PriceChart) {
            PriceChart c = (PriceChart)chart;
            mType = ChartType.Price;

            logScale.set(c.logScale);
            lineChart.set(!c.candleData);
        }
        else {
            IndicatorChart c = (IndicatorChart)chart;
            mType = ChartType.Indicator;
        }

        showLogScale     = mType != ChartType.Indicator;
        showLineCheckbox = mType == ChartType.Price;
        showFunctions    = mType == ChartType.Indicator;

        initFunctions();

        parent.interval.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (view != null)
                    view.reload();
            }
        });
    }

    public void setView(IChartView view) {
        this.view = view;
    }

    public StockChart getChart() {
        return mChart;
    }

    public ChartsViewModel getParent() {
        return parent;
    }

    public Interval getInterval() {
        return parent.interval.get();
    }

    public void setFunctionListener(OnFunctionChangeListener listener) {
        mFunctionListener = listener;
    }

    public IIndicator getFunction() {
        String name = functions.get(functionIndex.get());
        IIndicator i = (IIndicator)functionMap.get(name);

        return i;
    }

    private void initFunctions() {
        if (mType == ChartType.Indicator) {

            Indicator[] values = Indicator.values();
            for(IFunctionEnum e : values) {
                IFunction f = e.getInstance();
                functionMap.put(f.getName(), f);
                functions.add(f.getName());
            }

            Collections.sort(functions);

            // TODO only do this if chart doesnt have one selected already
            String macd = Indicator.MACD.getInstance().getName();
            for(int i = 0; i < functionMap.size(); i++) {
                String s = functions.get(i);
                if (s.contentEquals(macd))
                    functionIndex.set(i);
            }

        }

        functionIndex.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                showAddOverlay.set(functionAllowOverlays());
                mFunctionListener.onFunctionChanged();
            }
        });
    }

    private boolean functionAllowOverlays() {
        IIndicator ii = getFunction();
        if (ii.getResultType() == FloatArray.class)
            return true;

        return false;
    }
}
