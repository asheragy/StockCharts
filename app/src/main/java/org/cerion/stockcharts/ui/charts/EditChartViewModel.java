package org.cerion.stockcharts.ui.charts;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import org.cerion.stocks.core.arrays.FloatArray;
import org.cerion.stocks.core.charts.IndicatorChart;
import org.cerion.stocks.core.charts.PriceChart;
import org.cerion.stocks.core.charts.StockChart;
import org.cerion.stocks.core.charts.VolumeChart;
import org.cerion.stocks.core.functions.IFunction;
import org.cerion.stocks.core.functions.IIndicator;
import org.cerion.stocks.core.functions.types.IFunctionEnum;
import org.cerion.stocks.core.functions.types.Indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditChartViewModel {

    private StockChart mChart;
    private StockChart originalChart;
    private EditChartViewModel.ChartType mType;

    // Fixed values after loading
    public boolean showLineCheckbox;
    public boolean showLogScale;
    public boolean showFunctions;
    public List<String> functions = new ArrayList<>();

    private Map<String,IFunction> functionMap = new HashMap<>();
    private EditChartViewModel.OnFunctionChangeListener mFunctionListener;

    public ObservableField<Boolean> showAddOverlay = new ObservableField<>(true);
    public ObservableField<Integer> functionIndex = new ObservableField<>();
    public ObservableField<Boolean> logScale = new ObservableField<>();
    public ObservableField<Boolean> lineChart = new ObservableField<>();

    private enum ChartType {
        Price,
        Volume,
        Indicator
    }

    public StockChart getOriginalChart() {
        return originalChart;
    }

    public interface OnFunctionChangeListener {
        void onFunctionChanged();
    }

    public EditChartViewModel(StockChart chart) {
        originalChart = chart;

        // Create copy of chart that is the one to be edited
        try {
            mChart = (StockChart)chart.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (chart instanceof VolumeChart) {
            VolumeChart c = (VolumeChart)chart;
            mType = EditChartViewModel.ChartType.Volume;

            logScale.set(c.getLogScale());
        }
        else if (chart instanceof PriceChart) {
            PriceChart c = (PriceChart)chart;
            mType = EditChartViewModel.ChartType.Price;

            logScale.set(c.getLogScale());
            lineChart.set(!c.getCandleData());
        }
        else {
            mType = EditChartViewModel.ChartType.Indicator;
        }

        showLogScale     = mType != EditChartViewModel.ChartType.Indicator;
        showLineCheckbox = mType == EditChartViewModel.ChartType.Price;
        showFunctions    = mType == EditChartViewModel.ChartType.Indicator;

        initFunctions();
    }

    public StockChart getChart() {
        if (mChart instanceof PriceChart) {
            ((PriceChart) mChart).setLogScale( logScale.get() );
            ((PriceChart) mChart).setCandleData( !lineChart.get() );
        } else if (mChart instanceof VolumeChart) {
            ((VolumeChart) mChart).setLogScale( logScale.get() );
        } else if (mChart instanceof IndicatorChart) {
            // TODO this clears overlays, need to have those in VM too
            //final IIndicator instance = viewModel.getFunction();
            //instance.setParams( getParametersControl().getParameters() );
            //indicatorChart().setIndicator(instance);
        }

        return mChart;
    }

    public void setFunctionListener(EditChartViewModel.OnFunctionChangeListener listener) {
        mFunctionListener = listener;
    }

    public IIndicator getFunction() {
        String name = functions.get(functionIndex.get());
        IIndicator i = (IIndicator)functionMap.get(name);

        return i;
    }

    private void initFunctions() {
        functionIndex.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                showAddOverlay.set(functionAllowOverlays());
                if (mFunctionListener != null)
                    mFunctionListener.onFunctionChanged();
            }
        });

        if (mType == EditChartViewModel.ChartType.Indicator) {

            Indicator[] values = Indicator.values();
            for(IFunctionEnum e : values) {
                IFunction f = e.getInstance();
                functionMap.put(f.getName(), f);
                functions.add(f.getName());
            }

            Collections.sort(functions);
            IndicatorChart chart = (IndicatorChart)mChart;

            for(int i = 0; i < functionMap.size(); i++) {
                String s = functions.get(i);
                IFunction f = functionMap.get(s);

                if (f.getId() == chart.getId())
                    functionIndex.set(i);
            }

        }

    }

    private boolean functionAllowOverlays() {
        IIndicator ii = getFunction();
        if (ii.getResultType() == FloatArray.class)
            return true;

        return false;
    }
}