package cn.rrg.rdv.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

public abstract class AbsResIdArrayAdapter<T> extends ArrayAdapter<T> {

    protected int resourceId;

    public AbsResIdArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public AbsResIdArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.resourceId = resource;
    }

    public AbsResIdArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        this.resourceId = resource;
    }

    public AbsResIdArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public AbsResIdArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
        this.resourceId = resource;
    }

    public AbsResIdArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
        this.resourceId = resource;
    }
}
