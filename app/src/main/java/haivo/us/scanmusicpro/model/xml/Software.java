package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.TextContent;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Software {
    @TextContent
    String description;

    public String getDescription() {
        return description;
    }
}
