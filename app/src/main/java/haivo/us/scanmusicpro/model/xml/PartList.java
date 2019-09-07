package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;
import java.util.List;

@Xml(name = "part-list")
public class PartList {
    @Element(name = "part-group")
    List<PartGroup> partGroupList;
    @Element(name = "score-part")
    List<ScorePart> scorePartList;

    public List<PartGroup> getPartGroupList() {
        return partGroupList;
    }
    public List<ScorePart> getScorePartList() {
        return scorePartList;
    }
}