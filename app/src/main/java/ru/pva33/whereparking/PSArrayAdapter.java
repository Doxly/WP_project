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
 * Adapter for {@link ParkingSide}. Provide content for views.
 * Contain list of {@link ParkingSide} and support list manipulations.
 *
 * Created by pva on 04.02.16.
 */
public class PSArrayAdapter extends ArrayAdapter<ParkingSide> {
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
        // if we don't do this in code element of list wouldn't fired onClick events
        sb.setFocusable(false);
        int imageId = ps.getSoundPath() == null || ps.getSoundPath().isEmpty() ? R.drawable.ic_lock_silent_mode : R.drawable.ic_lock_silent_mode_off;
        sb.setBackgroundResource(imageId);

        return convertView;
    }

    /**
     * Find {@link ParkingSide} in inner list by its id,
     * and replace it if found or create new if not found.
     * @param ps
     */
    public void update(ParkingSide ps){
        if (ps == null) {
            return;
        }
        int oldID = findPS(ps.get_id());
        if (oldID >= 0){
            records.set(oldID, ps);
        }else{
            records.add(ps);
        }
        notifyDataSetChanged();
    }

    private int findPS(Long id) {
        for (int i = this.records.size(); i > 0 ; i--) {
            ParkingSide ps = (ParkingSide) this.records.get(i - 1);
            if(ps.get_id().equals(id)){
                return i-1;
            }
        }
        return -1;
    }
}
