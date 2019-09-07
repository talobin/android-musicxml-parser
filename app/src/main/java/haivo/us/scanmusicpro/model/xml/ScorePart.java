package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml(name = "score-part")
public class ScorePart {
    @Attribute
    String id;
    @PropertyElement(name = "part-name")
    String partname;

    public String getId() {
        return id;
    }
    public String getPartname() {
        return partname;
    }
}
