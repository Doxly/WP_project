package ru.pva33.whereparking;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import ru.pva33.whereparking.db.ParkingPoint;

/**
 * Adapter for {@link ParkingPoint}. Provide content for views.
 * Contain list of {@link ParkingPoint} and support list manipulations.
 * Created by pva on 04.02.16.
 */
//public class PPArrayAdapter extends ArrayAdapter<String> {
public class PPArrayAdapter extends ArrayAdapter<ParkingPoint> {
    private static final String TAG = "PVA_DEBUG";

    private LayoutInflater inflater;
    private List records;

    public PPArrayAdapter(Context context, int resource,
                          List objects) {
        super(context, resource, objects);
        this.records = objects;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pp_list_item, parent, false);
        }
        ParkingPoint pp = (ParkingPoint) records.get(position);
        // pp name
        ((TextView) convertView.findViewById(R.id.ppListItemName)).setText(pp.getName());
        // icon of sound attachment
        ImageButton sb = ((ImageButton) convertView.findViewById(R.id.ppListItemSoundButton));
        // if we don't do this in code element of list wouldn't fired click events
        sb.setFocusable(false);
        int imageId = pp.getSoundPath() == null || pp.getSoundPath().isEmpty() ?
            R.drawable.ic_lock_silent_mode : R.drawable.ic_lock_silent_mode_off;
        sb.setBackgroundResource(imageId);
        return convertView;
    }

    /**
     * Find {@link ParkingPoint} in the inner list by its id and replace it if found or
     * append new if not found.
     *
     * @param pp ParkingPoint to find
     * @see #findPP(Long)
     */
    public void update(ParkingPoint pp) {
        // find pp by id. If found - replace it. if Not append.
        if (pp == null) {
            return;
        }
        int oldId = findPP(pp.get_id());
        Log.d(TAG, "PPAdapger find row for pp" + pp + " find pos=" + oldId);
        if (oldId >= 0) {
            records.set(oldId, pp);
        } else {
            this.add(pp);
        }
        notifyDataSetChanged();
    }

    /**
     * Find parking point in list by its id.
     *
     * @param id Parking point ID
     * @return position in inner list or -1 if not found
     */
    private int findPP(Long id) {
        for (int i = records.size(); i > 0; i--) {
            ParkingPoint pp = (ParkingPoint) records.get(i - 1);
            if (pp.get_id().equals(id)) {
                return i - 1;
            }
        }
        return -1;
    }

//    public

}
