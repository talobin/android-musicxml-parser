package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;
import java.util.List;

@Xml
public class Encoding {

    @Element(name = "software")
    List<Software> softwareList;

    @PropertyElement(name = "encoding-date")
    String encodingDate;

    @PropertyElement
    String Supports;

    public List<Software> getSoftwareList() {
        return softwareList;
    }
    public String getEncodingDate() {
        return encodingDate;
    }
    public String getSupports() {
        return Supports;
    }
}
