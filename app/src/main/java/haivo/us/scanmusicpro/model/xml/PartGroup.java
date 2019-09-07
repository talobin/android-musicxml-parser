package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml(name = "part-group")
public class PartGroup {

    @Attribute
    String number;
    @Attribute
    String type;
    @PropertyElement(name = "group-symbol")
    String groupSymbol;

    public String getNumber() {
        return number;
    }
    public String getType() {
        return type;
    }
    public String getGroupSymbol() {
        return groupSymbol;
    }
}
