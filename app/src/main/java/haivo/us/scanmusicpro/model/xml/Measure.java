package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;
import java.util.List;

@Xml
public class Measure {
    @Attribute
    String number;
    @Attribute
    String width;

    @Element(name = "note")
    List<Note> noteList;

    @Element(name = "attributes")
    List<Attributes> attributesList;

    public List<Note> getNoteList() {
        return noteList;
    }

    public String getNumber() {
        return number;
    }
    public String getWidth() {
        return width;
    }
    public List<Attributes> getAttributesList() {
        return attributesList;
    }
}
