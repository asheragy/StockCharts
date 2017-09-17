package org.cerion.stockcharts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.CachedDataAPI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PortfolioValueActivity extends AppCompatActivity {

    private LineData lineData;
    private Date[] dates;
    private static DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, PortfolioValueActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_value);

        setChart();

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                loadData();
            }

            @Override
            public void onFinish() {
                setChart();
            }
        });

        task.execute();
    }

    private void loadData() {
        PositionRepository repo = new PositionRepository(this);
        CachedDataAPI api = Injection.getAPI(this);

        this.dates = new Date[0];
        List<Position> list = repo.getAll();
        for(Position p : list) {

            p.addDividends( api.getDividends(p.getSymbol()) );

            PriceList pl = api.getPrices(p.getSymbol(), Interval.DAILY, 500);
            if (pl.getDates().length > dates.length)
                dates=  pl.getDates();

            p.setPriceHistory(pl);

            // Get most recent quote
            if(p.getCurrPrice() == 0) {
                Quote q = api.getQuote(p.getSymbol());
                p.setQuote(q);
            }
        }

        List<Double> data = new ArrayList<>();
        // Add to chart v
        for(Position p : list) {
            List<Double> values = p.getValueHistory();
            for(int i = 0; i < values.size(); i++) {
                if (i == data.size())
                    data.add(0.0);

                double val = data.get(i) + values.get(values.size() - 1 - i);
                data.set(i, val);
            }
        }

        dates = Arrays.copyOfRange(dates, dates.length - data.size(), dates.length - 1);

        ArrayList<Entry> entries = new ArrayList<>();
        for(int i = 0; i < data.size(); i++)
            entries.add(new Entry(i, data.get(data.size() - i - 1).floatValue()));

        LineDataSet lineDataSet = new LineDataSet(entries, "Label");

        this.lineData = new LineData(lineDataSet);
    }

    private void setChart() {
        LineChart chart = new LineChart(this);
        // TODO add custom view for all this
        chart.setDescription(new Description());
        chart.setMinimumHeight(800);

        final FrameLayout frame = (FrameLayout) findViewById(R.id.chart_frame);
        frame.removeAllViews();

        if (lineData != null) {
            chart.setData(lineData);

            XAxis xaxis = chart.getXAxis();
            xaxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    int v = (int)value;
                    return mDateFormat.format(dates[v]);
                }
            });
        }

        frame.addView(chart);
    }
}
