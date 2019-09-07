package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.TextContent;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Beam {
    @Attribute
    String number;

    @TextContent
    String description;
    //    begin, continue, end

    public String getNumber() {
        return number;
    }
    public String getDescription() {
        return description;
    }
}
