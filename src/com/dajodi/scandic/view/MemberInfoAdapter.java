package com.dajodi.scandic.view;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dajodi.scandic.R;
import com.dajodi.scandic.model.ScandicStay;

public class MemberInfoAdapter extends BaseAdapter {
	
	private List<ScandicStay> searchArrayList;
	private LayoutInflater mInflater;

	public MemberInfoAdapter(Context context, List<ScandicStay> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return searchArrayList.size();
	}

	public Object getItem(int position) {
		return searchArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.custom_row, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.name);
			holder.points = (TextView) convertView.findViewById(R.id.pointsTxt);
			holder.date = (TextView) convertView.findViewById(R.id.dateRange);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ScandicStay stay = searchArrayList.get(position);

		holder.txtName.setText(stay.getHotelName());
		holder.points.setText(stay.getNumPoints() + " points");

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String dateTxt = String.format("%s - %s (%d nights)",
				format.format(stay.getFromDate()),
				format.format(stay.getToDate()), stay.getNumNights());

		holder.date.setText(dateTxt);

		return convertView;
	}

	static class ViewHolder {
		TextView txtName;
		TextView points;
		TextView date;
	}
}
