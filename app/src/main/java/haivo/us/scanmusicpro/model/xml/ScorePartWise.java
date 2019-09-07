package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;
import java.util.List;

@Xml
public class ScorePartWise {
    @Attribute
    String version;

    @Element
    Identification identification;

    @Element
    PartList partList;

    @Element
    List<Part> parts;
    public String getVersion() {
        return version;
    }
    public Identification getIdentification() {
        return identification;
    }
    public PartList getPartList() {
        return partList;
    }
    public List<Part> getParts() {
        return parts;
    }
}
