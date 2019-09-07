package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Identification {

    @Element
    Encoding encoding;

    @PropertyElement
    String creator;

    @Attribute(name = "type")
    String creatorType;

    public Encoding getEncoding() {
        return encoding;
    }
    public String getCreator() {
        return creator;
    }
    public String getCreatorType() {
        return creatorType;
    }
}
