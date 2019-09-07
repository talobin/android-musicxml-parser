package haivo.us.scanmusicpro.model.xml

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class Slur(
        @Attribute
        val number: String?,

        @Attribute
        val orientation: String?,

        //or placement
        @Attribute
        val type: String?
)
