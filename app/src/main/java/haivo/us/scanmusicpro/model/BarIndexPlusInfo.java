package haivo.us.scanmusicpro.model;

import uk.co.dolphin_com.rscore.BarInfo;
import uk.co.dolphin_com.rscore.RScore;

public class BarIndexPlusInfo {
    public BarInfo barInfo;
    int barIndex;

    BarIndexPlusInfo(int index, BarInfo info) {
        this.barIndex = index;
        this.barInfo = info;
    }

    public static BarIndexPlusInfo forBeat(int currentBeat, int totalBars, RScore rscore) {
        for (int barIndex2 = totalBars - 1; barIndex2 >= 0; barIndex2--) {
            BarInfo barInfo2 = rscore.getBarInfo(barIndex2);
            if (barInfo2 != null && barInfo2.startBeat <= currentBeat) {
                return new BarIndexPlusInfo(barIndex2, barInfo2);
            }
        }
        return null;
    }
}
