package com.app.rakubaru.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.rakubaru.R;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.main.SavedHistoryActivity;
import com.app.rakubaru.main.RouteActivity;
import com.app.rakubaru.models.Route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.app.rakubaru.base.BaseActivity.df;

public class RouteListAdapter extends BaseAdapter {

    private SavedHistoryActivity _context;
    private ArrayList<Route> _datas = new ArrayList<>();
    private ArrayList<Route> _alldatas = new ArrayList<>();

    public RouteListAdapter(SavedHistoryActivity context){
        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Route> datas) {
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
            convertView = inflater.inflate(R.layout.item_route, parent, false);

            holder.nameBox = (TextView) convertView.findViewById(R.id.nameBox);
            holder.timeBox = (TextView) convertView.findViewById(R.id.timeBox);
            holder.durationBox = (TextView) convertView.findViewById(R.id.durationBox);
            holder.distanceBox = (TextView) convertView.findViewById(R.id.distanceBox);
            holder.speedBox = (TextView) convertView.findViewById(R.id.speedBox);
            holder.statusBox = (TextView) convertView.findViewById(R.id.statusBox);
            holder.descriptionBox = (TextView) convertView.findViewById(R.id.descriptionBox);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Route entity = (Route) _datas.get(position);
        holder.nameBox.setText(entity.getName());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
        String startedTime = dateFormat.format(new Date(Long.parseLong(entity.getStart_time())));
        String endedTime = dateFormat.format(new Date(Long.parseLong(entity.getEnd_time())));
        holder.timeBox.setText(startedTime + " ~ " + endedTime);
        holder.durationBox.setText(getTimeStr(entity.getDuration()));
        holder.distanceBox.setText(df.format(entity.getDistance()) + "km");
        holder.speedBox.setText(df.format(entity.getSpeed()) + "km/h");

        if(entity.getStatus().length() > 0){
            holder.statusBox.setVisibility(View.VISIBLE);
        }else holder.statusBox.setVisibility(View.GONE);

        if(entity.getDescription().length() > 0){
            holder.descriptionBox.setVisibility(View.VISIBLE);
            holder.descriptionBox.setText(entity.getDescription());
        }else holder.descriptionBox.setVisibility(View.GONE);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.route = entity;
                _context.getRouteDetails(entity);
            }
        });

        convertView.setLongClickable(true);
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                _context.showAlertDialogForReportButtons(entity);
                return false;
            }
        });

        return convertView;
    }

    private String getTimeStr(long timeDiff){
        String timeStr = "";
        int seconds = (int) (timeDiff / 1000) % 60 ;
        int minutes = (int) ((timeDiff / (1000*60)) % 60);
        int hours   = (int) ((timeDiff / (1000*60*60)) % 24);
        timeStr = String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        return timeStr;
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {
            for (Route route : _alldatas){
                if (route != null) {
                    String value = route.getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(route);
                    }else {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm");
                        String startedTime = dateFormat.format(new Date(Long.parseLong(route.getStart_time())));
                        String endedTime = dateFormat.format(new Date(Long.parseLong(route.getEnd_time())));
                        if (startedTime.contains(charText) || endedTime.contains(charText)) {
                            _datas.add(route);
                        }else {
                            value = String.valueOf(route.getSpeed());
                            if (value.contains(charText)) {
                                _datas.add(route);
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        TextView nameBox, timeBox, durationBox, distanceBox, speedBox, statusBox, descriptionBox;
    }
}











