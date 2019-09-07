package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Clef {
    @Attribute
    String number;

    @PropertyElement
    String sign;

    @PropertyElement
    String line;

    public String getNumber() {
        return number;
    }
    public String getSign() {
        return sign;
    }
    public String getLine() {
        return line;
    }
}
