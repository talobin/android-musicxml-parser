package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Slur {
    @Attribute
    String number;

    @Attribute
    String orientation;
    //or placement

    @Attribute
    String type;
    public String getNumber() {
        return number;
    }
    public String getOrientation() {
        return orientation;
    }
    public String getType() {
        return type;
    }
}
