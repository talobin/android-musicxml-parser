package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Attributes {
    @PropertyElement
    String divisions;

    @PropertyElement
    String staves;

    @Element
    Time time;

    @Element
    Clef clef;

    public String getDivisions() {
        return divisions;
    }
    public String getStaves() {
        return staves;
    }
    public Time getTime() {
        return time;
    }
    public Clef getClef() {
        return clef;
    }
}
