package ru.pva33.whereparking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import java.util.List;

import ru.pva33.whereparking.db.ParkingSide;

/**
 * Created by pva on 04.02.16.
 */
public class PSArrayAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private List records;
    private Dao<ParkingSide, Integer> parkingSideDao;

    public PSArrayAdapter(Context context, int resource,
                          List objects, Dao<ParkingSide, Integer> parkingSideDao) {
        super(context, resource, objects);
        this.records = objects;
        this.parkingSideDao = parkingSideDao;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ps_list_item, parent, false);
        }
        ParkingSide ps = (ParkingSide) records.get(position);
        // ps name
        ((TextView) convertView.findViewById(R.id.psName)).setText(ps.getName());
        // icon of sound attachment
        ImageButton sb = ((ImageButton) convertView.findViewById(R.id.ppListItemSoundButton));
        // if we don't do this in code element of list wouldn't fired click events
        sb.setFocusable(false);
        int imageId = ps.getAllowSoundPath() == null ? R.drawable.ic_lock_silent_mode : R.drawable.ic_lock_silent_mode_off;
        sb.setBackgroundResource(imageId);

        return convertView;
    }
}
