package com.example.arfib.Symptoms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import java.util.List;

public class SymptomTimelineList extends RecyclerView.Adapter<LineChartAdapter.LineChartViewHolder> {

    private Context context;
    private List<LineData> lineDataList;

    public LineChartAdapter(Context context, List<LineData> lineDataList) {
        this.context = context;
        this.lineDataList = lineDataList;
    }

    @Override
    public LineChartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_line_chart, parent, false);
        return new LineChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LineChartViewHolder holder, int position) {
        // Get the data for the current position
        LineData lineData = lineDataList.get(position);

        // Set the data to the chart
        holder.lineChart.setData(lineData);
        holder.lineChart.invalidate();  // Refresh the chart
    }

    @Override
    public int getItemCount() {
        return lineDataList.size();
    }

    public static class LineChartViewHolder extends RecyclerView.ViewHolder {

        LineChart lineChart;

        public LineChartViewHolder(View itemView) {
            super(itemView);
            lineChart = itemView.findViewById(R.id.lineChart);
        }
    }
}
