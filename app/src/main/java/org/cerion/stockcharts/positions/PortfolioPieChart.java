package org.cerion.stockcharts.positions;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class PortfolioPieChart extends PieChart {

    //PieChart chart;
    public PortfolioPieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        getLegend().setEnabled(false);
        setDescription(null);
        setEntryLabelColor(Color.BLACK);
    }

    public void setAllocations(List<Pair<String, Float>> allocations) {
        float total = 0;
        for(int i = 0; i < allocations.size(); i++){
            Pair<String,Float> a = allocations.get(i);
            total += a.second;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for(int i = 0; i < allocations.size(); i++){
            Pair<String,Float> a = allocations.get(i);
            entries.add(new PieEntry(100 * a.second / total, a.first));
        }

        // Data set
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2);
        dataSet.setValueTextSize(12);
        dataSet.setValueFormatter(new PercentFormatter());

        //Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        setData(pieData);
        invalidate();
    }
}
