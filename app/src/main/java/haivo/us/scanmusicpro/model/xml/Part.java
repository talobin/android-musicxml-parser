package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;
import java.util.List;

@Xml
public class Part {

    @Attribute
    String id;

    @Element(name = "measure")
    List<Measure> measureList;

    public List<Measure> getMeasureList() {
        return measureList;
    }

    public String getId() {
        return id;
    }
}
