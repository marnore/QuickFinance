package lt.marius.charts;

import android.util.SparseArray;

import java.util.List;

/**
 * Created by marius on 8/10/14.
 */
public class CombinedSeries2D extends Series2D {

//    private List<Series2D> series;
    private SparseArray<Series2D> series;


    public CombinedSeries2D(List<Series2D> series) {
        setPointsX(new float[]{});
        setPointsY(new float[]{});
        this.series = new SparseArray<Series2D>();
        for (Series2D s : series) {
            this.series.append(s.getGroupId(), s);
        }
        if (series.size() > 0) {
            setPointsX(series.get(0).getPointsX());
            setPaint(series.get(0).getPaint());
            recalculate();
        }
     }

    public void setVisibility(int groupId, boolean visible) {
        Series2D s = series.get(groupId);
        if (s != null) {
            s.setVisible(visible);
            recalculate();
        }
    }

    private void recalculate() {
        float[] points = new float[getPointsX().length];
        if (series.size() != 0) {
            for (int i = 0, n = series.size(); i < n; i++) {
                Series2D s = series.valueAt(i);
                if (s.isVisible()) {
                    float[] pts = s.getPointsY();
                    for (int j = 0; j < pts.length; j++) {
                        points[j] += pts[j];
                    }
                }
            }
        }
        setPointsY(points);
    }

    public int[] getGroupsIds() {
        if (series == null) return new int[]{};
        int[] ids = new int[series.size()];
        for (int i = 0, n = series.size(); i < n; i++) {
            ids[i] = series.keyAt(i);
        }
        return  ids;
    }

    public boolean[] getGroupsVisibility() {
        if (series == null) return new boolean[]{};
        boolean[] ids = new boolean[series.size()];
        for (int i = 0, n = series.size(); i < n; i++) {
            ids[i] = series.valueAt(i).isVisible();
        }
        return ids;
    }

}
