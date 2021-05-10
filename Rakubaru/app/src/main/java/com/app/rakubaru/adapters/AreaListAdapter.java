package com.app.rakubaru.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.rakubaru.R;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.main.AreasActivity;
import com.app.rakubaru.main.MyReportsActivity;
import com.app.rakubaru.models.Area;
import com.app.rakubaru.models.Route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.app.rakubaru.base.BaseActivity.df;

public class AreaListAdapter  extends BaseAdapter {

    private AreasActivity _context;
    private ArrayList<Area> _datas = new ArrayList<>();
    private ArrayList<Area> _alldatas = new ArrayList<>();

    public AreaListAdapter(AreasActivity context){
        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Area> datas) {
        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_area, parent, false);

            holder.areaNameBox = (TextView) convertView.findViewById(R.id.areaNameBox);
            holder.timeBox = (TextView) convertView.findViewById(R.id.timeBox);
            holder.durationBox = (TextView) convertView.findViewById(R.id.durationBox);
            holder.distanceBox = (TextView) convertView.findViewById(R.id.distanceBox);
            holder.titleBox = (TextView) convertView.findViewById(R.id.titleBox);
            holder.distributionBox = (TextView) convertView.findViewById(R.id.distributionBox);
            holder.copiesBox = (TextView) convertView.findViewById(R.id.copiesBox);
            holder.priceBox = (TextView) convertView.findViewById(R.id.priceBox);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Area entity = (Area) _datas.get(position);
        holder.areaNameBox.setText(entity.getAreaName());
        holder.titleBox.setText(entity.getTitle());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        String startedTime = dateFormat.format(new Date(entity.getStartTime()));
        String endedTime = dateFormat.format(new Date(entity.getEndTime()));
        holder.timeBox.setText(startedTime + " ~ " + endedTime);
        holder.durationBox.setText(getDurationStr(entity.getEndTime() - entity.getStartTime()));
        holder.distanceBox.setText("距離: " + df.format(entity.getDistance()) + " km");
        holder.distributionBox.setText("配布物: " + entity.getDistribution());
        holder.priceBox.setText("金額: " + BaseActivity.getFormattedStr((long) entity.getAmount()) + " 円");
        holder.copiesBox.setText("部数: " + BaseActivity.getFormattedStr((long) entity.getCopies()));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.area = entity;
                _context.getAreaLocations(entity);
            }
        });

        convertView.setLongClickable(true);
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                _context.showAlertDialogForAreaButtons(entity);
                return false;
            }
        });

        return convertView;
    }

    private String getDurationStr(long timeDiff){
        int days   = (int) timeDiff / (1000*60*60*24);
        return String.valueOf(days) + " 日";
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {
            for (Area area : _alldatas){
                if (area != null) {
                    String value = area.getAreaName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(area);
                    }else {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
                        String startedTime = dateFormat.format(new Date(area.getStartTime()));
                        String endedTime = dateFormat.format(new Date(area.getEndTime()));
                        if (startedTime.contains(charText) || endedTime.contains(charText)) {
                            _datas.add(area);
                        }else {
                            value = String.valueOf(area.getDistribution());
                            if (value.contains(charText)) {
                                _datas.add(area);
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        TextView areaNameBox, titleBox, distributionBox, timeBox, durationBox, distanceBox, copiesBox, priceBox;
    }
}









